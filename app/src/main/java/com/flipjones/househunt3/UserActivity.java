package com.flipjones.househunt3;

import android.app.*;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    TextView mUser;
    String username;
    String userid;
    private RequestQueue mQueue;
    private TextView mTextView;
    public static final String REQUEST_TAG = "UserActivity";
    ArrayList<String> addressList;
    String[] neighborhoodnames;

    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_user);
        mUser = (TextView) findViewById(R.id.showUser);
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();


        Intent intent = getIntent();
        username = intent.getExtras().getString("username");
        neighborhoodnames = intent.getExtras().getStringArray("nnames");
        String greeting = "HouseHunt Account For " + username;
        mUser.setText(greeting);

        String IDurl = "http://househuntapi.appspot.com/userid/" + username;
        JsonObjectRequest IDRequest = new JsonObjectRequest(Request.Method.GET, IDurl, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                JSONObject jobject = (JSONObject) response;
                JSONArray jnames = null;
                try {
                    jnames = jobject.getJSONArray("keys");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] stringArray = new String[jnames.length()];
                for (int i = 0; i < jnames.length(); i++) {
                    try {
                        userid = jnames.getString(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String homesurl = "http://househuntapi.appspot.com/userhouses/" + userid;
                JsonObjectRequest addressRequest = new JsonObjectRequest(Request.Method.GET, homesurl, null, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject jobject = (JSONObject) response;
                        JSONArray jnames = null;
                        try {
                            jnames = jobject.getJSONArray("addresses");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] stringArray = new String[jnames.length()];
                        for (int i = 0; i < jnames.length(); i++) {
                            try {
                                stringArray[i] = jnames.getString(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        addressList = new ArrayList<String>(Arrays.asList(stringArray));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse (VolleyError error){
                        mUser.setText(error.getMessage());
                    }
                });

                addressRequest.setTag(REQUEST_TAG);
                mQueue.add(addressRequest);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){
                mUser.setText(error.getMessage());
            }
        });

        IDRequest.setTag(REQUEST_TAG);
        mQueue.add(IDRequest);

    }

    public void seeHouses(View view)
    {
        System.out.println("username: " + username);
        System.out.println("user id: " + userid);
        System.out.println("addressList" + addressList.toString());

        Intent listIntent=new Intent(getApplicationContext(), ListActivity.class);
        listIntent.putExtra("username", username);
        listIntent.putExtra("userid", userid);
        listIntent.putExtra("houselist", addressList);
        listIntent.putExtra("nnames", neighborhoodnames);
        startActivity(listIntent);
    }

    public void newHouse(View view)
    {
        Intent buildIntent=new Intent(getApplicationContext(), BuildActivity.class);
        buildIntent.putExtra("username", username);
        buildIntent.putExtra("userid", userid);
        buildIntent.putExtra("nnames", neighborhoodnames);
        startActivity(buildIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

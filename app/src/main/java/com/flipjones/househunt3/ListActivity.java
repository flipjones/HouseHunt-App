package com.flipjones.househunt3;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ListView houseList;
    TextView mUser;
    ArrayList<String> addressList;
    String username;
    String userid;
    String houseid;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "ListActivity";
    String availability;
    String neighborhood;
    String nname;
    String price;
    String[] optionArray;
    String[] neighborhoodnames;
    String houseurl;
    String neighborurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        houseList = (ListView) findViewById(R.id.viewHouses);
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
        mUser = (TextView) findViewById(R.id.showUser);

        Intent userIntent = getIntent();
        username = userIntent.getExtras().getString("username");
        userid = userIntent.getExtras().getString("userid");
        addressList = userIntent.getExtras().getStringArrayList("houselist");
        neighborhoodnames = userIntent.getExtras().getStringArray("nnames");

        String greeting = "HouseHunt Account For " + username;
        mUser.setText(greeting);

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addressList);
        // Set The Adapter
        houseList.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        houseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                final String selectedHouse = addressList.get(position);
                String requestURL = null;

                requestURL = ("http://househuntapi.appspot.com/houseid?address=" + selectedHouse);
                requestURL = requestURL.replaceAll(" ", "%20");

                final String idurl = requestURL;
                JsonObjectRequest addressRequest = new JsonObjectRequest(Request.Method.GET, idurl, null, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject jobject = (JSONObject) response;
                        JSONArray jnames = null;
                        try {
                            jnames = jobject.getJSONArray("keys");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        final String[] stringArray = new String[jnames.length()];
                        for (int i = 0; i < jnames.length(); i++) {
                            try {
                                houseid = jnames.getString(0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        houseurl = "http://househuntapi.appspot.com/house/" + houseid;
                        JsonObjectRequest addressRequest = new JsonObjectRequest(Request.Method.GET, houseurl, null, new Response.Listener<JSONObject>(){

                            @Override
                            public void onResponse(JSONObject response) {
                                JSONObject hObject = (JSONObject) response;
                                JSONArray options = null;
                                try {
                                    price = hObject.getString("price");
                                    availability = hObject.getString("available");
                                    neighborhood = hObject.getString("neighborhood");
                                    options = hObject.getJSONArray("options");
                                    optionArray = new String[options.length()];
                                    for (int i = 0; i < options.length(); i++) {
                                        try {
                                            optionArray[i] = options.getString(i);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                neighborurl = "http://househuntapi.appspot.com/neighborhood/" + neighborhood;
                                JsonObjectRequest addressRequest = new JsonObjectRequest(Request.Method.GET, neighborurl, null, new Response.Listener<JSONObject>(){

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        JSONObject hObject = (JSONObject) response;
                                        JSONArray options = null;
                                        try {
                                            nname = hObject.getString("name");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Intent houseIntent=new Intent(getApplicationContext(), HouseActivity.class);
                                        houseIntent.putExtra("username", username);
                                        houseIntent.putExtra("userid", userid);
                                        houseIntent.putExtra("housekey", houseid);
                                        houseIntent.putExtra("isavail", availability);
                                        houseIntent.putExtra("price", price);
                                        houseIntent.putExtra("address", selectedHouse);
                                        houseIntent.putExtra("options", optionArray);
                                        houseIntent.putExtra("houselist", addressList);
                                        houseIntent.putExtra("neighborhood", nname);
                                        houseIntent.putExtra("nnames", neighborhoodnames);

                                        startActivity(houseIntent);

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

                        addressRequest.setTag(REQUEST_TAG);
                        mQueue.add(addressRequest);

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
        });
    }
}

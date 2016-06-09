package com.flipjones.househunt3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class HouseActivity extends AppCompatActivity {

    TextView tView;
    EditText eView;
    RadioButton yesView;
    RadioButton noView;
    Spinner dropdown;
    String houseid;
    String availability;
    String price;
    String address;
    String username;
    String userid;
    String neighborhood;
    String neighborhoodid;
    String[] options;
    String[] neighborhoodnames;
    ArrayList<String> addressList;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "HouseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house);

        Intent intent = getIntent();
        houseid = intent.getExtras().getString("housekey");
        availability = intent.getExtras().getString("isavail");
        price = intent.getExtras().getString("price");
        address = intent.getExtras().getString("address");
        options = intent.getExtras().getStringArray("options");
        addressList = intent.getExtras().getStringArrayList("houselist");
        neighborhoodnames = intent.getExtras().getStringArray("nnames");
        username = intent.getExtras().getString("username");
        userid = intent.getExtras().getString("userid");
        neighborhood = intent.getExtras().getString("neighborhood");

        tView = (TextView) findViewById(R.id.addressview);
        tView.setText(address);
        eView = (EditText) findViewById(R.id.editPrice);
        eView.setText(price);
        yesView = (RadioButton) findViewById(R.id.radio_available);
        noView = (RadioButton) findViewById(R.id.radio_unavailable);
        if (availability.equals("true"))
        {
            yesView.setChecked(true);
        }
        else
        {
            noView.setChecked(true);
        }
        tView = (TextView) findViewById(R.id.optionview);
        StringBuilder builder = new StringBuilder();
        for (String s : options) {
            builder.append(s + "\n");
            tView.setText(builder.toString());
        }
        setSpinner();
    }

    private void setSpinner() {
        dropdown = (Spinner) findViewById(R.id.nspinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, neighborhoodnames);
        dropdown.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(neighborhood);
        dropdown.setSelection(spinnerPosition);
    }

    public void goToMap(View view)
    {
        openLocationInMap(address);
    }

    private void openLocationInMap(String zip)
    {
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", zip)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
        else
        {
            Log.e("Error: ", "Couldn't call " + zip + ", no receiving apps installed!");
        }
    }

    public void updateHouse(View View) {

        neighborhood = dropdown.getSelectedItem().toString();
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();

        String requestURL = ("http://househuntapi.appspot.com/neighborhoodid?name=" + neighborhood);
        requestURL = requestURL.replaceAll(" ", "%20");
        JsonObjectRequest IDRequest = new JsonObjectRequest(Request.Method.GET, requestURL, null, new Response.Listener<JSONObject>(){

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
                        neighborhoodid = jnames.getString(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                makePutCall();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

            }
        });

        IDRequest.setTag(REQUEST_TAG);
        mQueue.add(IDRequest);
    }

    private void makePutCall() {
        new PutClass(this).execute();
    }

    private class PutClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PutClass(Context c){

            this.context = c;
        }

        public String makeParams()
        {
            neighborhood = dropdown.getSelectedItem().toString();
            String parameters;

            eView = (EditText)findViewById(R.id.editPrice);
            price = eView.getText().toString();

            if (yesView.isChecked()) {
                availability = "True";
            }
            else{
                availability = "False";
            }



            parameters = "price=" + price + "&available=" + availability + "&neighborhood=" + neighborhoodid;
            return parameters;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                String putURL = ("http://househuntapi.appspot.com/houseupdate/" + houseid);
                URL url = new URL(putURL);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                String urlParameters = makeParams();

                connection.setRequestMethod("PUT");
                connection.setRequestProperty("USER-AGENT", "Android 6.0.1");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'PUT' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "PUT");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";

                StringBuilder responseOutput = new StringBuilder();
                System.out.println("output===============" + br);
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

//        protected void onPostExecute() {
//            progress.dismiss();
//       }

    }

    public void deleteHouse(View view)
    {
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
        String homesurl = "http://househuntapi.appspot.com/house/" + houseid;
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, homesurl, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                for ( int i = 0;  i < addressList.size(); i++)
                {
                    String tempAdd = addressList.get(i);
                    if(tempAdd.equals(address))
                    {
                        addressList.remove(i);
                    }
                }
                Intent buildIntent=new Intent(getApplicationContext(), ListActivity.class);
                buildIntent.putExtra("username", username);
                buildIntent.putExtra("userid", userid);
                buildIntent.putExtra("houselist", addressList);
                startActivity(buildIntent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

            }
        });
        deleteRequest.setTag(REQUEST_TAG);
        mQueue.add(deleteRequest);
    }
}

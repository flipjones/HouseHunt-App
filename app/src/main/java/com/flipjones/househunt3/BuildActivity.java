// Tutorial followed from https://www.numetriclabz.com/android-post-and-get-request-using-httpurlconnection/
// to build the majority of this app.
// Remainder of code was learned through Udacity's Developing Android Apps course, course ud853.
// Small amounts of code from Android Developer resource were modified and used.

package com.flipjones.househunt3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
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


public class BuildActivity extends AppCompatActivity {

    private ProgressDialog progress;
    EditText mAddress;
    EditText mPrice;
    EditText mZip;
    EditText mNeighborhood;
    String neighborhood;
    String address;
    String avail;
    String userid;
    Number price;
    Boolean available;
    TextView mUser;
    Spinner dropdown;
    String username;
    String[] neighborhoodnames;
    String neighborhoodid;
    private ArrayAdapter<String> mAddressAdapter;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "BuildActivity";



    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_available:
                if (checked)
                    available = true;
                break;
            case R.id.radio_unavailable:
                if (checked)
                    available = false;
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);
        mUser = (TextView) findViewById(R.id.showUser);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String getName = intent.getExtras().getString("username");
        userid = intent.getExtras().getString("userid");
        neighborhoodnames = intent.getExtras().getStringArray("nnames");
        username = "HouseHunt Account For " + getName;
        mUser.setText(username);
        setSpinner();
    }

    private void setSpinner() {
        dropdown = (Spinner) findViewById(R.id.nspinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, neighborhoodnames);
        dropdown.setAdapter(adapter);
    }

    public void sendPostRequest(View View) {
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
                makePostCall();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

            }
        });

        IDRequest.setTag(REQUEST_TAG);
        mQueue.add(IDRequest);
    }


    private void makePostCall() {
        new PostClass(this).execute();
    }

    private class PostClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PostClass(Context c){

            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        public String makeParams()
        {
            String parameters;
            mAddress = (EditText)findViewById(R.id.address_view);
            String address = mAddress.getText().toString();

            mPrice = (EditText)findViewById(R.id.price_view);
            String price = mPrice.getText().toString();

            if (available == true)
                avail = "True";
            else if (available == false)
                avail = "False";

            String neighborhood = neighborhoodid.toString();

            parameters = "address=" + address + "&available=" + avail  + "&price=" + price + "&neighborhood=" + neighborhood + "&user=" + userid;
            return parameters;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                final TextView outputView = (TextView) findViewById(R.id.showOutput);
                URL url = new URL("http://househuntapi.appspot.com/house");

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                String urlParameters = makeParams();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Android 6.0.1");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";

                StringBuilder responseOutput = new StringBuilder();
                System.out.println("output===============" + br);
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                BuildActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        outputView.setText(output);
                        progress.dismiss();
                    }
                });


            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            Intent builtIntent=new Intent(getApplicationContext(), UserActivity.class);
            builtIntent.putExtra("username", username);
            builtIntent.putExtra("userid", userid);
            startActivity(builtIntent);
        }
    }
}
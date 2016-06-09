package com.flipjones.househunt3;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.appindexing.Action;

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


public class LoginActivity extends AppCompatActivity implements Response.Listener,
        Response.ErrorListener {
    String[] usernames = {"premade", "bill", "jim"};
    String[] neighborhoodnames;
    String currentUser;
    EditText mName;
    String newname;
    Spinner dropdown;
    public static final String REQUEST_TAG = "LoginActivity";
    private TextView mTextView;
    private Button mButton;
    private RequestQueue mQueue;
    private ProgressDialog progress;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTextView = (TextView) findViewById(R.id.txtdisplay);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    public void login(View view) {
        Intent intent = new Intent(this, UserActivity.class);
        currentUser = dropdown.getSelectedItem().toString();
        intent.putExtra("username", currentUser);
        intent.putExtra("nnames", neighborhoodnames);
        startActivity(intent);
    }

    public void create(View view) {

        new PostClass(this).execute();
        Intent intent = new Intent(this, UserActivity.class);
        mName = (EditText)findViewById(R.id.newuser);
        newname = mName.getText().toString();
        intent.putExtra("username", newname);
        intent.putExtra("nnames", neighborhoodnames);
        startActivity(intent);
    }

    private void setSpinner() {
        dropdown = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, usernames);
        dropdown.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext())
                .getRequestQueue();
        String url = "http://househuntapi.appspot.com/username";
        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .GET, url,
                new JSONObject(), this, this);
        jsonRequest.setTag(REQUEST_TAG);
        mQueue.add(jsonRequest);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.flipjones.househunt3/http/host/path")
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.flipjones.househunt3/http/host/path")
        );
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mTextView.setText(error.getMessage());
    }

    @Override
    public void onResponse(Object response)
    {
        JSONObject jobject = (JSONObject) response;
        JSONArray jnames = null;
        try {
            jnames = jobject.getJSONArray("names");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] stringArray = new String[jnames.length()];
        for (int i = 0; i < jnames.length(); i++) {
            try {
                stringArray[i]= jnames.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        usernames = stringArray;
        setSpinner();
///
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
        String nurl = "http://househuntapi.appspot.com/neighborhoodnames";
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.GET, nurl, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                JSONObject jobject = (JSONObject) response;
                JSONArray jnames = null;
                try {
                    jnames = jobject.getJSONArray("names");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] stringArray = new String[jnames.length()];
                for (int i = 0; i < jnames.length(); i++) {
                    try {
                        stringArray[i]= jnames.getString(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                neighborhoodnames = stringArray;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

            }
        });
        deleteRequest.setTag(REQUEST_TAG);
        mQueue.add(deleteRequest);
///
    }

    public void sendPostRequest(View View) {
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

            mName = (EditText)findViewById(R.id.newuser);
            newname = mName.getText().toString();

            parameters = "name=" + newname;
            return parameters;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                URL url = new URL("http://househuntapi.appspot.com/user");

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

                LoginActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
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
            progress.dismiss();
        }

    }

}

package com.example.counteverything;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by stylesuxx on 10/14/13.
 */
public class Api extends AsyncTask<String, Void, String> {
    private Exception exception;
    private final Context mContext;
    private final static String TAG = "API";

    public Api(Context context) {
        this.mContext = context;
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch(java.net.UnknownHostException ex){
            throw new IOException("Check URL.");
        } catch(Exception ex){
            throw new IOException("Check API token.");
        }
        return in;
    }

    @Override
    protected String doInBackground(String... strings) {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        String str = "";
        String URL = strings[0];

        // Try to connect to the server
        try {
            in = OpenHttpConnection(URL);
            if(in != null){
                InputStreamReader isr = new InputStreamReader(in);
                int charRead;
                str = "";
                char[] inputBuffer = new char[BUFFER_SIZE];
                try {
                    while ((charRead = isr.read(inputBuffer))>0)
                    {
                        //---convert the chars to a String---
                        String readString = String.copyValueOf(inputBuffer, 0, charRead);
                        str += readString;
                        inputBuffer = new char[BUFFER_SIZE];
                    }
                    in.close();
                } catch (IOException e) {
                    //Toast.makeText(this.mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    this.exception = e;
                    //e.printStackTrace();
                    return "Error";
                }
            }
            else{
                this.exception = new IOException("Check url.");
                return "Error";
            }
        } catch (IOException e) {
            //Toast.makeText(this.mContext, e1.getMessage(), Toast.LENGTH_SHORT).show();
            //e1.printStackTrace();
            this.exception = e;
            return "Error";
        }

        return str;
    }

    protected void onPostExecute(String str) {
        if(str.equals("Error")) {
            Log.v(TAG, "onPostExecute >> " + this.exception.getMessage());
            Toast.makeText(this.mContext, this.exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else if (!str.isEmpty()) {
            Toast.makeText(this.mContext, "RESPONSE: " + str, Toast.LENGTH_SHORT).show();
        }
    }
}

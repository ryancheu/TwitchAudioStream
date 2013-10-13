package com.idkjava.twitchaudio.networking;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;


public class GetPortTask extends AsyncTask <String, Void, Integer> {
    private String mUsername;
    private static final String TAG = "GetPortTask";

    public interface OnPortFoundListener {
        void onPortFound(int port);
    }

    private OnPortFoundListener mListener;
    public GetPortTask(String username, OnPortFoundListener listener) {
        mUsername = username;
        mListener = listener;
    }
       
    protected Integer doInBackground(String... urls) {
    	int port = -1;
    	try {
    		port = StreamingServerConnectionManager.getPortForUsername(mUsername, urls[0]);
    	} catch (ClientProtocolException e) {
            Log.e(TAG, "Couldn't connect to streaming server \n" 
                    ,e);

          } catch (IOException e) {
              Log.e(TAG, "Couldn't connect to streaming server \n"
                   ,e);			
          } catch (JSONException e) {
              Log.e(TAG, "Couldn't parse json from streaming server \n"
                    ,e);						
          }
    	
    	
        return port;
    }
    
    protected void onPostExecute(Integer port) {
        mListener.onPortFound(port);
    }
}
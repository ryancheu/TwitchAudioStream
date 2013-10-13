package com.idkjava.twitchaudio.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class StreamingServerConnectionManager {
	
	private static final String URL_USERNAME_PARAM="u";
	private static final String PORT_PARAM="port";
	
	public static int getPortForUsername(String username, String serverUrl) 
			throws ClientProtocolException, IOException, JSONException {

		Log.d("Streaming try", "json: try ");
		DefaultHttpClient defaultClient = new DefaultHttpClient();
		String url = "http://" + serverUrl+"/?" + URL_USERNAME_PARAM +"="+Uri.encode(username);
		Log.d("Streaming","url: " + url);
		HttpGet httpGetRequest = new HttpGet(url);		
		HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
		String json = reader.readLine();
		Log.d("Streaming", "json: " + json);

		JSONObject jsonResponse = new JSONObject(json);
		if ( jsonResponse.has(PORT_PARAM)) {
			return jsonResponse.getInt(PORT_PARAM);
		} else {
			return -1;
		}		
	}	
}

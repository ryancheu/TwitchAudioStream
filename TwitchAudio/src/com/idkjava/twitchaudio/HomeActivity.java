package com.idkjava.twitchaudio;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.idkjava.twitchaudio.networking.StreamingServerConnectionManager;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class HomeActivity extends Activity {
	
	private EditText mServerAddr;
	private EditText mTwitchUser;
	private Button mStartStreamButton;
	private Button mStopAudioButton;
	
	private MediaPlayer mPlayer;
	
	private static final String TAG ="HomeActivity";
	private static final boolean DEBUG=true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        mServerAddr = (EditText)findViewById(R.id.serverAddress_editText);
        mTwitchUser = (EditText)findViewById(R.id.twitchUsername_editText);
        
        mStartStreamButton = (Button)findViewById(R.id.home_go_button);
    	mStopAudioButton = (Button)findViewById(R.id.home_stop_audio_button);
        
        initButtons();        
    }
    
    private void initButtons() {
        mStartStreamButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startStream(mTwitchUser.getText().toString(), 
						mServerAddr.getText().toString());				
			}
		});
        
        mStopAudioButton = (Button)findViewById(R.id.home_stop_audio_button);
        
        mStopAudioButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopStream();
			}
		});
        
        
    }
    
    private void startStream(String twitchUsername, String serverAddress) {
    	int port = -1;
		try {
			port = StreamingServerConnectionManager.
					getPortForUsername(twitchUsername, serverAddress);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Couldn't connect to streaming server \n" 
					+ e.getStackTrace());

		} catch (IOException e) {
			Log.e(TAG, "Couldn't connect to streaming server \n"
					+ e.getStackTrace());			
		} catch (JSONException e) {
			Log.e(TAG, "Couldn't parse json from streaming server \n"
					+ e.getStackTrace());						
		}
		
    	if ( port != -1) {    		
    		mPlayer = MediaPlayer.create(this, Uri.parse(serverAddress +":port"));
        	mPlayer.start();
        	if (DEBUG) {
        		Log.d(TAG, "starting playing stream from server");
        	}        	
    	}
    }
    
    private void stopStream() {
    	if (mPlayer != null) {
    		mPlayer.stop();
    	}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }
    
}

package ryancheu.twitchaudio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AudioStream {
    private String mUsername;
    private int mPort;
    private Process mProcess;
    private long lastRequest = 0;

    public AudioStream(String username, int port) {
        mUsername = username;
        mPort = port;
    }

    
    /**
     * Starts streaming specified twitch username on given port
     */
    public void beginStreaming() {
        lastRequest = System.currentTimeMillis();
        StreamData sd = null;
        try {
			sd = getStreamData(mUsername);
		} catch (IOException e) {
			System.out.println("ERROR: Could not fetch stream metadata");
			e.printStackTrace();
			return;
		} catch (JSONException e) {
			System.out.println("ERROR: Stream Json not formatted correctly");
			e.printStackTrace();
			return;
		}
        
        try {
        	if ( sd != null) {
        		mProcess = startStreamingProcess(buildCommand(sd, mPort));
        	}
		} catch (IOException e) {
			System.out.println("Successfully started stream");
			e.printStackTrace();
			return;
		}
        
        System.out.println("Streaming start success");
    }


    /**
     * Stops audio streaming by killing the vlc/rtmpdump processes
     */
    public void killAudio() {
        if ( mProcess != null ) {
        	mProcess.destroy();
        }
    }
    
    public static void testStreamData() {
    	AudioStream stream = new AudioStream("dreamhacktv", 8080);
    	stream.testGetStreamData();
    }
    
    public void testGetStreamData() {
    	try {
    		StreamData sd = getStreamData(mUsername);
			System.out.println(sd.toString());
			System.out.println(buildCommand(sd,mPort));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private Process startStreamingProcess(String command) throws IOException {
    	ProcessBuilder builder = new ProcessBuilder(command);    	
    	return builder.start();
    }

    private StreamData getStreamData(String username) throws IOException, JSONException {
        String url = "http://usher.justin.tv/find/" + username + ".json?type=any";
        String jsonRaw = readUrl(url);
        JsonParser parser = new JsonParser();
        JsonArray json = (JsonArray)parser.parse(jsonRaw);
        JsonObject jo;
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m;
        
        //Find the lowest quality stream avail
        int lowestIndex = 0;
        int lowestQuality = 2024;
        int testQual;
        for (int i = json.size(); --i >= 0; ) {
    		jo = (JsonObject) json.get(i);
    		m = p.matcher(jo.get("display").toString());
    		m.find();
    		testQual = Integer.parseInt(m.group());
    		if ( testQual < lowestQuality ) {
    			lowestQuality = testQual;
    			lowestIndex = i;
    		}
        }
        
        JsonObject lowestQualityStreamJson = (JsonObject) json.get(lowestIndex);
        
        String token = lowestQualityStreamJson.get("token").toString();
        String connect = lowestQualityStreamJson.get("connect").toString();
        String play = lowestQualityStreamJson.get("play").toString();
        
        //get rid of the escape characters infront of the quotes"
        token = token.replace("\\", "");
        
        return new StreamData(token, connect, play);
        
    }

    private String buildCommand(StreamData data, int port) {
        StringBuilder s = new StringBuilder();
        s.append("rtmpdump --live -r \'");
        s.append(data.getConnect());
        s.append("\' -W \'http://www-cdn.jtvnw.net/widgets/live_site_player.swf\'");
        s.append(" -p \'http://www.twitch.tv/\' --jtv \'");
        s.append(data.getToken());
        s.append("\' --playpath \'");
        s.append(data.getPlay());
        s.append("\' --quiet --flv \'-\'");
        s.append("| vlc --intf=dummy --rc-fake-tty -vvv - --sout \'" +
        		"#transcode{vcodec=h264,vb=800k,acodec=aac,ab=72k}:standard{access=http,mux=ts,dst=:");
        s.append("" + port);
        s.append("}\'");
                       
        return s.toString();
    }

    private class StreamData {
        private String mToken;
        private String mConnect;
        private String mPlay;
        public StreamData( String token, String connect, String play) { 
            mToken = token;
            mConnect = connect;
            mPlay = play;
        }
        
        public String getToken() {
            return mToken;
        }

        public String getConnect() {
            return mConnect;
        }

        public String getPlay() {
            return mPlay;
        }
        
        public String toString() {
			return "Token: " + mToken + 
					"\nConnect: " + mConnect +
					"\nPlay: " + mPlay;
        }
    }
    
    
    private static String readUrl(String urlString) throws IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read); 

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }

    }
    
    public static void main(String[] args) {
    	AudioStream at = new AudioStream("dreamhacktv", 8080);
    	at.beginStreaming();
    }
}

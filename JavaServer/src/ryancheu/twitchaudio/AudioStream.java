package ryancheu.twitchaudio;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioStream {
    private String mUsername;
    private long lastRequest = 0;

    public AudioStream(String username) {
        mUsername = username;
    }

    public void beginStreaming() {
        lastRequest = System.currentTimeMillis();
    }


    public void killAudio() {
        
    }

    private void getStreamData(String username) throws IOException, JSONException {
        String url = "http://usher.justin.tv/find/" + username + ".json?type=any";
        JSONObject json = JsonReader.readJsonFromUrl(url);
    }


    private String buildCommand(StreamData data) {
        StringBuilder s = new StringBuilder();
        s.append("rtmpdump --live -r \'");
    }

    private class StreamData {
        private String mToken;
        private String mConnect;
        private String mPlay;
        public SteamData( String token, String connect, String play) { 
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
    }
}

package ryancheu.twitchaudio;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.google.gson.JsonObject;

public class AudioServer implements Container {

    /** The port to start streaming audio on */
    private static final int START_PORT = 6000;

    /** The number of ports to use for streams */
    private static final int MAX_PORTS = 1000;

    /** The port to respond to username requests on */
    private static final int QUERY_PORT = 8080;

    /** Hashmap mapping from username to AudioStream object */
    private HashMap<String, AudioStream> mStreams;

    /** Queue of ports currently open */
    private Queue<Integer> mOpenPorts;

    private static Connection mServerConnection;

    /**
     * Creates a new instance of AudioServer
     */
    public AudioServer() {
        mStreams = new HashMap<String, AudioStream>();
        mOpenPorts = new LinkedBlockingQueue<Integer>();

        for (int i = MAX_PORTS; --i >= 0;) {
            mOpenPorts.add(START_PORT + i);
        }
    }

    /**
     * Returns the audiostream object for a specified username
     * 
     * @param username the twitch username to look up
     * @return a new AudioStream object if the username had not been requested
     *         before or the existing AudioSteam for the username
     */
    private AudioStream getStreamForUsername(String username) {
        AudioStream as;
        if ((as = mStreams.get(username)) == null) {
            as = new AudioStream(username, mOpenPorts.remove());
            try {
                System.out.println("u:" + username + "|");
                as.beginStreaming();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mStreams.put(username, as);
        }

        return as;

    }

    /**
     * Starts the server on port QUERY PORT
     * 
     * @param list
     */
    public static void main(String[] list) {
        Container container = new AudioServer();
        try {
            Server server = new ContainerServer(container);
            mServerConnection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(QUERY_PORT);
            mServerConnection.connect(address);
        } catch (IOException e) {
            System.out.println("Initializing server failed, is port"
                    + QUERY_PORT + "already being used?");
            e.printStackTrace();
        }
    }

    /**
     * Handles a request made to the server
     */
    public void handle(Request request, Response response) {
        try {
            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            Query query = request.getQuery();

            // Accept requests for either GET or POST
            String username = query.get("username");
            if (username == null || username.length() == 0
                    || username.equals("null")) {
                username = request.getParameter("u");

                // If neither request has no data for username, return
                if (username == null) {
                    System.out.println("Error: No username received");
                    return;
                }
            }

            // Create a response and send back the port in json format
            response.setValue("Content-Type", "text/plain");
            response.setValue("Server", "TwitchAudio/1.0 (Simple 4.0)");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            if (username != null && username.length() != 0
                    && !username.equals("null")) {
                AudioStream as = getStreamForUsername(username);
                JsonObject reply = new JsonObject();
                reply.addProperty("port", as.getPort());
                body.println(reply.toString());
            }

            body.close();
        } catch (Exception e) {
            System.out.println("Error responding to request");
            e.printStackTrace();
        }

    }

}

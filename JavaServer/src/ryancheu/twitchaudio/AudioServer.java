package ryancheu.twitchaudio;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;



public class AudioServer implements Container{
	private static final int MAX_PORTS = 1000;
	private static final int START_PORT = 6000;

	private HashMap<String, AudioStream> mStreams;
	private Queue<Integer> mOpenPorts;

	public AudioServer() {
		mStreams = new HashMap<String, AudioStream>();
		mOpenPorts = new LinkedBlockingQueue<Integer>();

		for (int i = MAX_PORTS; --i >= 0;) {
			mOpenPorts.add(START_PORT + i);
		}
	}

	private AudioStream getSteamForUsername(String username) {
		AudioStream as;
		if ((as = mStreams.get(username)) != null) {
			;
		} else {
			as = new AudioStream(username, mOpenPorts.remove());
			mStreams.put(username, as);
		}

		return as;

	}

	 public static void main(String[] list) throws Exception {
	      Container container = new AudioServer();
	      Server server = new ContainerServer(container);
	      Connection connection = new SocketConnection(server);
	      SocketAddress address = new InetSocketAddress(8080);

	      connection.connect(address);
	   }

	public void handle(Request request, Response response) {
		try {
	         PrintStream body = response.getPrintStream();
	         long time = System.currentTimeMillis();
	   
	         response.setValue("Content-Type", "text/plain");
	         response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
	         response.setDate("Date", time);
	         response.setDate("Last-Modified", time);
	   
	         body.println("Hello World");
	         body.close();
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
		
	}

}

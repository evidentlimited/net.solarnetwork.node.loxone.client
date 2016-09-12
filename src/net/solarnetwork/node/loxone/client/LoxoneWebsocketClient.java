
package net.solarnetwork.node.loxone.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class LoxoneWebsocketClient {

	private Session session = null;

	private String host;
	private String username;
	private String password;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private synchronized void initiate() {
		javax.websocket.WebSocketContainer container = org.glassfish.tyrus.client.ClientManager
				.createClient(org.glassfish.tyrus.container.jdk.client.JdkClientContainer.class.getName());
		session = null;
		try {
			log.debug("Opening Loxone websocket connection to ws://" + host);
			session = container.connectToServer(getClass(), new URI("ws://" + host));
		} catch ( DeploymentException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( URISyntaxException e ) {
			e.printStackTrace();
		}
	}

//	OPEN
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		log.debug("WebSocket opened: " + session.getId());
		log.debug("OPENED WITH VALUES: " + this.host + ", " + this.username + ", " + this.password);
		
		try {
			log.debug("Sending getkey request...");
			session.getBasicRemote().sendText("jdev/sys/getkey");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

//	MESSAGE BINARY
	@OnMessage
	public void onMessage(ByteBuffer byteBuffer) {
		log.debug("Binary message recieved: " + byteBuffer.toString());
	}
	
	public void test() {
		log.debug("TEST1: " + this.host + ", " + this.username + ", " + this.password);
		log.debug("TEST2: " + host + ", " + username + ", " + password);
	}

//	MESSAGE STRING
	@OnMessage
	public void onMessage(String message) {
		
		log.debug("Message recieved: " + message);
		
		log.debug("parsing JSON");
		
		JSONObject response = new JSONObject(message);
		
		log.debug(this.toString());

		String responseControl = response.getJSONObject("LL").getString("control");
		String responseCode = response.getJSONObject("LL").getString("Code");
		
		test();
		
		if(!responseCode.equals("200")){
			log.debug("Error: bad response(" + responseCode + ")\n" + message);
			return;
		}
		
		log.debug("Control: " + responseControl + ", Code: " + responseCode);
		if (responseControl.equals("jdev/sys/getkey")) {
			
			log.debug("Got authentication key");
			
			String key = response.getJSONObject("LL").getString("value");
			key = Crypto.hexToString(key);
			
			log.debug("Decoded key: " + key);
			
			String authString = this.username + ":" + this.password;
			String hash = Crypto.createHmacSha1Hash(authString, key);
			
			log.debug("Auth string: " + authString);
			
			log.debug("Hash: " + hash);
			
			log.debug("Authenticating ...");
			
			try {
				session.getBasicRemote().sendText("authenticate/" + hash);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else if (responseControl.equals("Auth")) {
			
			log.debug("Authenticated");
			log.debug("Checking LoxAPP...");
			
			try {
				session.getBasicRemote().sendText("jdev/sps/LoxAPPversion3");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

//	CLOSE
	@OnClose
	public void onClose(CloseReason reason) {
		log.debug("Closing a WebSocket due to " + reason.getReasonPhrase());
	}

	public synchronized void open(String host, String username, String password) {
		// be smart about property updates: if they haven't changed, don't reset
		boolean changed = false;
		if( this.host == null || !this.host.equalsIgnoreCase(host) ) {
			changed = true;
		}else if(this.username == null || !this.username.equals(username)) {
			changed = true;
		}else if(this.password == null || !this.password.equals(password)) {
			changed = true;
		}
		if(!changed) {
			return;
		}

		close();

		this.host = host;
		this.username = username;
		this.password = password;
		
		log.debug("Opening connection with values: " + this.host + ", " + this.username + ", " + this.password);

		initiate();
	}

	public synchronized void close() {
		log.debug("Requested close connection");
		if ( session != null && session.isOpen() == true) {
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "All done."));
			} catch ( IOException e ) {
				log.debug("IOException closing websocket Session", e);
			} finally {
				session = null;
			}
		} else {
			session = null;
		}
		log.debug("Connection closed");
	}
}

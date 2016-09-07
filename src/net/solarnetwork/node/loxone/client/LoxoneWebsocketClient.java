package net.solarnetwork.node.loxone.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class LoxoneWebsocketClient implements Runnable {

	private Session session = null;
	
	private Thread sessionThread = null;
	
	private String host;
	private String username;
	private String password;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	public void initiate() {
		log.debug("Starting client thread...");
		Thread t = new Thread(this);
		t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		t.start();
		sessionThread = t;
	}

	@Override
	public void run() {
		javax.websocket.WebSocketContainer container = org.glassfish.tyrus.client.ClientManager.createClient(
	      org.glassfish.tyrus.container.jdk.client.JdkClientContainer.class.getName());
		session = null;
		try {
			log.debug("Opening Loxone websocket connection to ws://" + host);
			session = container.connectToServer(getClass(), new URI("ws://" + host));
			log.debug("Sending greeting...");
			session.getBasicRemote().sendText("Hello, Loxone.");
			synchronized ( session ) {
				Boolean done = (Boolean) session.getUserProperties().get("done");
				while (done == null || !done.booleanValue()) {
					System.out.println("Waiting for response...");
					session.wait(4000);
					done = (Boolean) session.getUserProperties().get("done");
				}
			}
		} catch (DeploymentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				try {
					log.debug("Closing websocket connection...");
					session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "All done."));
				} catch (IOException e) {
					// ignore this
				}
			}
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		log.debug("WebSocket opened: " + session.getId());
	}

	@OnMessage
	public void onMessage(String txt) {
		log.debug("WebSocket received message: " + txt);
//		synchronized ( session ) {
//			session.getUserProperties().put("done", Boolean.TRUE);
//			session.notifyAll();
//		}
	}

	@OnClose
	public void onClose(CloseReason reason) {
		log.debug("Closing a WebSocket due to " + reason.getReasonPhrase());
	}

	public void open(String host, String username, String password) {

		close();
		
		this.host = host;
		this.username = username;
		this.password = password;
		
		initiate();
		
	}
	
	public void close() {
		log.debug("Requested close connection");
		if ( sessionThread != null ) {
			if ( session != null ) {
				synchronized ( session ) {
					session.getUserProperties().put("done", Boolean.TRUE);
					session.notifyAll();
				}
			}else{
				log.debug("No session to close");
			}
			try {
				log.debug("Waiting for client thread to finish...");
				sessionThread.join();
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
			sessionThread = null;
			session = null;
			log.debug("Closed");
		}else{
			log.debug("No session thread to close");
		}
	}
}



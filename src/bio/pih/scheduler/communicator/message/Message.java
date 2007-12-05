package bio.pih.scheduler.communicator.message;

import java.io.Serializable;

/**
 * The <code>Message</code> that will be send by server and workers.
 * 
 * @author albrecht (felipe.albrecht@gmail.com)
 * @date 30/11/2007
 */
public abstract class Message implements Serializable {

	private static final long serialVersionUID = 7147783864567219200L;

	/**
	 * The possibles kinds of a message
	 */
	public enum MessageKind {
		/**
		 * A request login message, sending my a client
		 */
		LOGIN,
		/**
		 * A welcome message sending by the server to the client informing that he is connected.
		 */
		WELCOME,
		/**
		 * A search request seding by the server requesting a search to the clients.
		 */
		REQUEST;
	}

	/**
	 * Get the kind of the message. The kind can be:
	 * <ul>
	 * <li> LOGIN: a worker sends to server for initialize the connection. </li>
	 * <li> WELCOME: the server sends to worker to response the <code>LOGIN</code> message and to say that he is connected. </li>
	 * <li> REQUEST: the server sends to worker requesting a processing.
	 * </ul>
	 * 
	 * <p>
	 * Each class that implements <code>Message</code> must to add your kind at <code>MessageKind</code> enumeration and overwrite this method.
	 * 
	 * @return <code>MessageKind</code> of the message.
	 */
	public abstract MessageKind getKind();

	/**
	 * Process the message at the reciver side.
	 * 
	 * @return <code>true</code> if is necessary to send a response, otherwise returns <code>false</code>.
	 */
	public abstract boolean process();
}

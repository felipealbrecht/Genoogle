package bio.pih.scheduler.communicator;

import java.io.IOException;

import bio.pih.scheduler.communicator.message.Message;

/**
 * Defines a simple interface for communication between Server and workers.
 * 
 * <p> If the sender is the server, the receiver is the client and otherwise is true.
 * @author albrecht
 * @date 02/12/2007
 */
public interface Communicator {

	
	/**
	 *  Timeout of <code>accept()</code> and another blocking socket operations.
	 */
	public static int SOCKET_TIMEOUT = 1000;
	
	/**
	 * Send a message for the other side.
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(Message message) throws IOException;
	
	/**
	 * Receive the first message at the messages received pool and remove its from the pool.
	 * @return <code>Message</code> or <code>null</code> if is not messages.
	 * @throws IOException, ClassNotFoundException
	 * @throws ClassNotFoundException 
	 */
	public Message receiveMessage() throws IOException, ClassNotFoundException;
	
	/**
	 * Start the communicator.
	 * <p>After the start, the user has to check <code>isReady</code> for to any operation on the {@link Communicator}.
	 * Start the communicator
	 * @throws IOException 
	 */
	public void start() throws IOException;
	
	/**
	 * Stop the communicator
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void stop() throws IOException;
	
	/**
	 * Inform if this communicator is ready
	 * @return <code>true</code> if the communicator is ready
	 */
	public boolean isReady();
	
}

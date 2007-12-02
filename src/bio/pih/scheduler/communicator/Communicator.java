package bio.pih.scheduler.communicator;

import bio.pih.scheduler.communicator.message.Message;

/**
 * Defines a simple interface for comunication between Server and workers.
 * 
 * <p> If the sender is the server, the reciver is the client and otherwise is true.
 * @author albrecht
 * @date 02/12/2007
 */
public interface Communicator {

	/**
	 * Send a message for the other side.
	 * @param message
	 */
	public void sendMessage(Message message);
	
	/**
	 * Receive the first message at the messages received pool and remove its from the pool.
	 * @return <code>Message</code> or <code>null</code> if is not messages.
	 */
	public Message reciveMessage();
}

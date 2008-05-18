package bio.pih.scheduler.communicator.message;

/**
 * Welcome message sending by the server to the client informing that he is connected.
 * @author albrecht
 *
 */
public class WelcomeMessage extends Message {
	private static final long serialVersionUID = -3820362100677558158L;
	
	private int id;
	
	/**
	 * @param id
	 */
	public WelcomeMessage(int id) {
		this.id = id;
	}
		
	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return id of the receiver. 
	 */
	public int getId() {
		return id;
	}

	@Override
	public MessageKind getKind() {
		return MessageKind.WELCOME;
	}
}

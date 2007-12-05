package bio.pih.scheduler.communicator.message;

/**
 * A message sending by the Worker to the Server for establish a connection.
 * @author albrecht
 * 
 * TODO: send information about databases, like "NR: sign 0x1234 part 2 of 4".
 */
public class LoginMessage extends Message {
	private static final long serialVersionUID = 6771930867999223207L;
	private int availableProcessors;

	/**
	 * @param availableProcessors
	 */
	public LoginMessage(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}
	
	/**
	 * @param availableProcessors
	 */
	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}
	
	/**
	 * @return availableProcessors
	 */
	public int getAvailableProcessors() {
		return availableProcessors;
	}

	@Override
	public MessageKind getKind() {
		return MessageKind.LOGIN;
	}

	@Override
	public boolean process() {
		return true;
	}
}

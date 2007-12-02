package bio.pih.scheduler.communicator.message;

public class LoginMessage extends Message {
	private static final long serialVersionUID = 6771930867999223207L;
	int availableProcessors;

	public LoginMessage(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}
	
	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}
	
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

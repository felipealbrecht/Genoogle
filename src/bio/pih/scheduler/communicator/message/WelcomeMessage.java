package bio.pih.scheduler.communicator.message;

public class WelcomeMessage extends Message {
	private static final long serialVersionUID = -3820362100677558158L;
	
	int id;
	
	public WelcomeMessage(int id) {
		this.id = id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public MessageKind getKind() {
		return MessageKind.WELCOME;
	}
	
	@Override
	public boolean process() {
		return false;
	};
}

package bio.pih.scheduler.communicator.message;


public class RequestMessage extends Message {
	private static final long serialVersionUID = 7129366659656051697L;

	@Override
	public MessageKind getKind() {
		return MessageKind.REQUEST;
	}

	@Override
	public boolean process() {	
		return true;
	}	
}

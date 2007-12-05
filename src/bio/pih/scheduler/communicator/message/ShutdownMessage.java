package bio.pih.scheduler.communicator.message;

/**
 * Represents a shutdown message.
 */
public class ShutdownMessage extends Message {

	private static final long serialVersionUID = 1L;

	/**
	 * Singleton for ShutdownMessage
	 */
	public static ShutdownMessage SHUTDOWN_MESSAGE = new ShutdownMessage();

	/**
	 * For access the a ShutdowMessage instance, uses the singleton <code>SHUTDOWN_MESSAGE</code>
	 */
	private ShutdownMessage() {
	}

	@Override
	public MessageKind getKind() {
		return MessageKind.SHUTDOWN;
	}

}

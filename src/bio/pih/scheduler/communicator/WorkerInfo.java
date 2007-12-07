package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import bio.pih.scheduler.Scheduler;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;

/**
 * Worker informations at the server side
 * 
 * @author albrecht
 * 
 */
public class WorkerInfo implements Communicator {
	volatile boolean running;
	Scheduler scheduler;
	int identifier;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	Socket socket;
	int searchesRunning;
	int availableProcessors;

	/**
	 * @param scheduler
	 * @param identifier
	 * @param availableProcessors
	 * @param input
	 * @param output
	 * @param socket
	 */
	public WorkerInfo(Scheduler scheduler, int identifier, int availableProcessors, ObjectInputStream input, ObjectOutputStream output, Socket socket) {
		this.scheduler = scheduler;
		this.identifier = identifier;
		this.ois = input;
		this.oos = output;
		this.socket = socket;
		this.availableProcessors = availableProcessors;
		this.searchesRunning = 0;
	}

	@Override
	public void start() {
		Runnable thread = new Runnable() {
			public void run() {
				try {
					running = true;
					while (running) {
						receiveMessage();
					}
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					running = false;
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					running = false;
				}

			}
		};
		new Thread(thread, "WorkerInfo - " + socket.getPort()).start();
	}

	@Override
	public Message receiveMessage() throws IOException, ClassNotFoundException {
		Message m = (Message) this.ois.readObject();
		switch (m.getKind()) {
		case SHUTDOWN:
			// Received a "shutdown ack" message, so shutdown.
			this.stop();
			break;

		default:
			processIncomingMessage(m);
			break;
		}
		return m;
	}

	private void processIncomingMessage(final Message m) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				scheduler.processMessage(identifier, m);
			}
		};
		new Thread(r, "Processing messaga " + m).start();
	}

	/**
	 * @param request
	 * @throws IOException
	 */
	public void request(RequestMessage request) throws IOException {
		sendMessage(request);
	}

	/**
	 * @param m
	 * @throws IOException
	 */
	public synchronized void sendMessage(Message m) throws IOException {
		try {
			this.oos.writeObject(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Client ");
		sb.append(identifier);
		sb.append(" with ");
		sb.append(availableProcessors);
		sb.append(" processors running ");
		sb.append(searchesRunning);
		sb.append(" threads.");

		return sb.toString();
	}

	@Override
	public void stop() throws IOException {
		socket.close();
		this.running = false;
	}

	@Override
	public boolean isReady() {
		return running && socket.isConnected();
	}

}

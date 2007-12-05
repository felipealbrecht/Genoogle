package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
	int identifier;
	ObjectInputStream input;
	ObjectOutputStream output;
	Socket socket;
	int searchesRunning;
	int availableProcessors;

	/**
	 * @param identifier
	 * @param availableProcessors
	 * @param input
	 * @param output
	 * @param socket
	 */
	public WorkerInfo(int identifier, int availableProcessors, ObjectInputStream input, ObjectOutputStream output, Socket socket) {
		this.identifier = identifier;
		this.input = input;
		this.output = output;
		this.socket = socket;
		this.availableProcessors = availableProcessors;
		this.searchesRunning = 0;
	}

	@Override
	public void start() {
		Runnable thread = new Runnable() {
			public void run() {
				try {
					while (running) {
						if (incomingFromWorker()) {
							putputToWorker();
						}
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
		new Thread(thread).run();
	}

	@Override
	public Message reciveMessage() throws IOException, ClassNotFoundException {
		Message m = (Message) this.input.readObject();
		return m;
	}

	private boolean incomingFromWorker() throws IOException, ClassNotFoundException {
		Message m;
		boolean data = false;
		while ((m = reciveMessage()) != null) {
			data |= processIncomingMessage(m);
		}
		return data;
	}

	private boolean processIncomingMessage(Message m) {
		System.out.println("Processando incoming from worker: " + m);
		return true;
	}

	private boolean putputToWorker() {
		return false;
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
	public void sendMessage(Message m) throws IOException {
		try {
			//System.out.println("vai enviar " + m);
			this.output.writeObject(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

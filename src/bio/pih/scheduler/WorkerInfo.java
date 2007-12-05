package bio.pih.scheduler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

public class WorkerInfo {
	volatile boolean running;
	int identifier;
	ObjectInputStream input;
	ObjectOutputStream output;
	Socket socket;
	int searchesRunning;
	int availableProcessors;
	Runnable thread;
	List<RequestMessage> waitingList;
	List<Message> messages;

	public WorkerInfo(int identifier, int availableProcessors, ObjectInputStream input, ObjectOutputStream output, Socket socket) {
		this.identifier = identifier;
		this.input = input;
		this.output = output;
		this.socket = socket;
		this.availableProcessors = availableProcessors;
		this.searchesRunning = 0;
		this.waitingList = new LinkedList<RequestMessage>();
		this.messages = new LinkedList<Message>();
	}

	public void disconect() throws IOException {
		socket.close();
		running = false;
	}

	public void startThread() {
		thread = new Runnable() {
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
	}

	private boolean incomingFromWorker() throws IOException, ClassNotFoundException {
		Message m;
		boolean data = false;
		while (((m = (Message) this.input.readObject()) != null)) {
			data |= processMessage(m);
		}
		return data;
	}

	private boolean processMessage(Message m) {
		return m.process();
	}

	private boolean putputToWorker() {
		return false;
	}

	public List<RequestMessage> getWaitingList() {
		return waitingList;
	}

	public void request(RequestMessage request) throws IOException {
		if (waitingList.size() == 0) {
			sendMessage(request);
		} else {
			waitingList.add(request);
		}
	}

	public void sendMessage(Message m) throws IOException {
		try {
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
}

package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import bio.pih.scheduler.Worker;
import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * The worker connector to the scheduler
 * @author albrecht
 *
 */
public class WorkerCommunicator implements Communicator {

	int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

	ObjectInputStream ois;
	ObjectOutputStream oos;
	Worker worker;
	Socket socket;

	int port;

	volatile boolean running;
	Thread t = null;

	/**
	 * @param worker the linked {@link Worker} 
	 * @param port that will be running
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public WorkerCommunicator(Worker worker, int port) throws IOException, ClassNotFoundException {
		this.worker = worker;
		this.port = port;
	}

	/**
	 * Return how many processors are avaliable.
	 * Note: HyperThreading(HT) processor counts as 2 
	 * @return avaliable processors
	 */
	public int getAvailableprocessors() {
		return availableProcessors;
	}

	@Override
	public Message reciveMessage() throws IOException, ClassNotFoundException {
		return (Message) ois.readObject();
	}

	@Override
	public void sendMessage(Message message) throws IOException {
		oos.writeObject(message);
	}

	/**
	 * Start the client thread.
	 */
	public void start() {
		Runnable r = new Runnable() {
			public void run() {
				Message m;				
				try {
					ServerSocket ss = new ServerSocket(port);
					running = true;
					socket = ss.accept();

					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());

					oos.writeObject(new LoginMessage(getAvailableprocessors()));
					
					WelcomeMessage ret = (WelcomeMessage) ois.readObject();					
					System.out.println("Client Recebeu: " + ret + " id: " + ret.getId());
					
					while (running) {
						m = (Message) ois.readObject();
						processMessage(m);
					}
					socket.close();
				} catch (IOException e) {
					System.out.println(e);
					running = false;
				} catch (ClassNotFoundException e) {
					System.out.println(e);
					running = false;
				}
			}
		};
		t = new Thread(r);
		t.start();
	}

	private boolean processMessage(Message m) {
		System.out.println("processing " + m);
		switch (m.getKind()) {
		case SHUTDOWN:
			this.stop();
			break;
			
		default:
			worker.processMessage(m);
			break;
		}
		return false;
	}

	/**
	 * Stop the client thread.
	 */
	public void stop() {
		running = false;
		t = null;
	}
	
	@Override
	public boolean isReady() {
		return running;
	}

	/**
	 * @return the {@link Worker} associate with this communicator 
	 */
	public Worker getWorker() {
		return worker;
	}
	
	/**
	 * @return the port that this {@link Communicator} is running
	 */
	public int getPort() {
		if (socket == null) {
			return -1;
		}
		return socket.getLocalPort();
	}
	
	/**
	 * @return InetAddress of this {@link Communicator}
	 */
	public InetAddress getInetAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getInetAddress();
	}
	
}

package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;

import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * The worker connector to the scheduler
 * @author albrecht
 *
 */
public class Client implements Communicator {

	int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

	ObjectInputStream ois;
	ObjectOutputStream oos;

	InetAddress serverAddress;
	int port;

	volatile boolean running;
	Thread t = null;

	/**
	 * @param serverAddress
	 * @param port
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Client(InetAddress serverAddress, int port) throws IOException, ClassNotFoundException {
		this.serverAddress = serverAddress;
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
				running = true;
				Message m;				
				try {
					Socket socket = new Socket(serverAddress, port);
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
		m.process();
		return false;
	}

	/**
	 * Stop the client thread.
	 */
	public void stop() {
		running = false;
		t = null;
	}

}

package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.ShutdownMessage;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * The worker connector to the scheduler
 * 
 * @author albrecht
 * 
 */
public class WorkerCommunicator implements Communicator {

	
	ObjectInputStream ois;
	ObjectOutputStream oos;
	AbstractWorker worker;
	Socket socket;

	int port;

	volatile boolean running;

	/**
	 * @param worker
	 *            the linked {@link AbstractWorker}
	 * @param port
	 *            that will be running
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public WorkerCommunicator(AbstractWorker worker, int port) throws IOException, ClassNotFoundException {
		this.worker = worker;
		this.port = port;
	}

	@Override
	public Message receiveMessage() throws IOException, ClassNotFoundException {
		return (Message) ois.readObject();
	}

	@Override
	public synchronized void sendMessage(Message message) throws IOException {
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

					oos.writeObject(new LoginMessage(worker.getAvailableprocessors()));

					WelcomeMessage ret = (WelcomeMessage) ois.readObject();
					worker.setIdentifier(ret.getId());

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
		new Thread(r, "Worker Communicator - " + port).start();
	}

	private boolean processMessage(Message m) throws IOException {
		switch (m.getKind()) {
		case SHUTDOWN:
			// reply shutdown "ack"
			this.sendMessage(ShutdownMessage.SHUTDOWN_MESSAGE);
			// save the internal status and shutdown.
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
	}

	@Override
	public boolean isReady() {
		return running;
	}

	/**
	 * @return the {@link AbstractWorker} associate with this communicator
	 */
	public AbstractWorker getWorker() {
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

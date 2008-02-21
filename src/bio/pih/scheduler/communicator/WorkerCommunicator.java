package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

	volatile boolean running;
	volatile boolean connected;

	// Socket socket;
	ServerSocket ss;
	Socket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	AbstractWorker worker;

	int port;

	/**
	 * @param worker
	 *            the linked {@link AbstractWorker}
	 * @param port
	 *            that will be running
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public WorkerCommunicator(AbstractWorker worker, int port) {
		this.worker = worker;
		this.port = port;
		this.ss = null;
	}

	public Message receiveMessage() throws IOException, ClassNotFoundException {
		return (Message) ois.readObject();
	}

	public synchronized void sendMessage(Message message) throws IOException {
		oos.writeObject(message);
	}

	public void start() throws IOException {
		ss = new ServerSocket(port);
		ss.setSoTimeout(SOCKET_TIMEOUT);

		Runnable r = new Runnable() {
			public void run() {
				Message m;
				running = true;
				try {
					while (running && (socket == null)) {
						try {
							socket = ss.accept();
						} catch (SocketTimeoutException e) {
							// pass timeout exception
						}
					}

					if (socket != null) {
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());

						oos.writeObject(new LoginMessage(worker.getAvailableprocessors()));

						WelcomeMessage ret = (WelcomeMessage) ois.readObject();
						worker.setIdentifier(ret.getId());

						connected = true;
						while (running) {
							m = (Message) ois.readObject();
							processMessage(m);
						}
						connected = false;
						socket.close();

					}
				} catch (IOException e) {
					System.out.println(e + " no worker " + worker.getIdentifier());
					e.printStackTrace();
					running = false;
				} catch (ClassNotFoundException e) {
					System.out.println(e + " no worker " + worker.getIdentifier());
					e.printStackTrace();
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
			worker.stop();
			break;

		default:
			worker.processMessage(m);
			break;
		}
		return false;
	}

	/**
	 * Stop the client thread.
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		running = false;

		try {
			Thread.sleep(SOCKET_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ss.close();
	}

	public boolean isReady() {
		return running;
	}

	/**
	 * @return <code>true</code> if this communicator is connected with the scheduler
	 */
	public boolean isConnected() {
		return socket == null ? false : socket.isConnected() && this.connected;
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
		if (ss == null) {
			return -1;
		}
		return ss.getLocalPort();
	}

	/**
	 * @return InetAddress of this {@link Communicator}
	 */
	public InetAddress getInetAddress() {
		if (ss == null) {
			return null;
		}
		return ss.getInetAddress();
	}

}

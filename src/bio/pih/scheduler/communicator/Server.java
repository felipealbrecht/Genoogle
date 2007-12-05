package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.WorkerInfo;
import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * A server class where the workers will connect. 
 * @author albrecht
 *
 */
public class Server {
	private volatile boolean isRunning;
	private ServerSocket ss;
	private int port = 5555;
	private List<WorkerInfo> workers;

	/**
	 * Constructor of the server.
	 * @throws IOException
	 */
	public Server() throws IOException {
		workers = new LinkedList<WorkerInfo>();
	}

	/**
	 * Start the server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		if (isRunning || isRunning) {
			throw new IOException("Sockets already started!");
		}

		/**
		 * TODO: ter como avisar quantos trabalhadores se conectarão, para não precisar ficar esperando ad eternum por novas conexões.
		 */
		Runnable r = new Runnable() {
			public void run() {
				ObjectInputStream input = null;
				ObjectOutputStream output = null;
				WorkerInfo workerInfo = null;
				LoginMessage message;
				Socket accept = null;
				try {
					ss = new ServerSocket(port);
					isRunning = true;
					while (isRunning) {
						accept = ss.accept();

						output = new ObjectOutputStream(accept.getOutputStream());
						output.flush();
						input = new ObjectInputStream(accept.getInputStream());

						output.writeObject(new WelcomeMessage(workers.size() + 1));
						message = (LoginMessage) input.readObject();
						System.out.println(message + " " + accept.getRemoteSocketAddress());

						workerInfo = new WorkerInfo(workers.size() + 1, message.getAvailableProcessors(), input, output, accept);
						workerInfo.startThread();
						workers.add(workerInfo);
					}
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
					isRunning = false;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					isRunning = false;
				}
			}
		};
		new Thread(r).start();
	}

	/**
	 * Return if the server is up and running!
	 * @return <code>true</code> if the server is up and running 
	 */
	public boolean isReady() {
		if (isRunning) {
			return !ss.isClosed() && ss.isBound();
		} else {
			return false;
		}
	}

	/**
	 * Stop the sockets.
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		isRunning = false;
		ss.close();
	}

	/**
	 * Return a <code>List</code> of workers connected
	 * @return <code>List</code> of workers connected
	 */
	public List<WorkerInfo> getWorkers() {
		return workers;
	}

	/**
	 * Send a request to the workers nodes.
	 * 
	 * @param requestMessage
	 * @throws IOException
	 */
	public synchronized void sendRequest(RequestMessage requestMessage) throws IOException {
		for (WorkerInfo worker : getWorkers()) {
			// at each one, create a simple threat to send message
			worker.request(requestMessage);
		}
	}
}

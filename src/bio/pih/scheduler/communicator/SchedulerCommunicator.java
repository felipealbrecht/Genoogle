package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.ShutdownMessage;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * A server class where the workers will connect. 
 * @author albrecht
 *
 */
public class SchedulerCommunicator implements Communicator {
	private List<WorkerInfo> workers;
	private List<String> wList;
	volatile boolean isReady;

	/**
	 * Constructor of the server.
	 * @param wList workers must be a string: "address:port" 
	 * @throws IOException
	 */
	public SchedulerCommunicator(List<String>  wList) throws IOException {
		this.workers = new LinkedList<WorkerInfo>();
		this.wList = wList;
		this.isReady = false;
	}

	/**
	 * Start the server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		Runnable r = new Runnable() {
			public void run() {
				ObjectInputStream input = null;
				ObjectOutputStream output = null;
				WorkerInfo workerInfo = null;
				LoginMessage message;
				Socket socket = null;
				try {
					for (String s: wList) {
						String[] split = s.split(":");
						System.out.println("Conectando-se ao trabalhador: " + split[0] + " " + split[1]);
						socket = new Socket(split[0], Integer.parseInt(split[1]));
																	
						output = new ObjectOutputStream(socket.getOutputStream());
						output.flush();
						input = new ObjectInputStream(socket.getInputStream());

						output.writeObject(new WelcomeMessage(workers.size() + 1));
						message = (LoginMessage) input.readObject();
						System.out.println(message + " " + socket.getRemoteSocketAddress());

						workerInfo = new WorkerInfo(workers.size() + 1, message.getAvailableProcessors(), input, output, socket);
						workerInfo.start();
						workers.add(workerInfo);
					}
					isReady = true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(r).start();
	}

	/**
	 * Stop the sockets.
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		sendMessage(ShutdownMessage.SHUTDOWN_MESSAGE);		
		isReady = false;
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
			// at each one, create a simple thread to send message
			worker.request(requestMessage);
		}
	}

	@Override
	public Message reciveMessage() throws IOException, ClassNotFoundException {
		// TODO Read the messages addressed to its
		return null;
	}

	@Override
	public void sendMessage(Message message) throws IOException {
		for (WorkerInfo worker : getWorkers()) {
			worker.sendMessage(message);
		}		
	}
	
	@Override
	public boolean isReady() {
		return isReady;
	}
}

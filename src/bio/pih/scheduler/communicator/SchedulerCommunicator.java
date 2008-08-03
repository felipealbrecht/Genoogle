package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import bio.pih.scheduler.Dispatcher;
import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.ShutdownMessage;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

/**
 * A server class where the workers will connect.
 * 
 * @author albrecht
 * 
 */
public class SchedulerCommunicator implements Communicator {
	private Dispatcher scheduler;
	volatile boolean isReady;

	/**
	 * Constructor of the server.
	 * 
	 * @param scheduler
	 */
	public SchedulerCommunicator(Dispatcher scheduler) {
		this.isReady = false;
		this.scheduler = scheduler;
	}

	/**
	 * Start the server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		Runnable r = new Runnable() {
			public void run() {

				try {
					for (String s : getScheduler().getWorkerAddress()) {
						String[] split = s.split(":");
						System.out.println("Conectando-se ao trabalhador: " + split[0] + " " + split[1]);
						Socket socket = new Socket(split[0], Integer.parseInt(split[1]));

						ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
						output.flush();
						ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

						output.writeObject(new WelcomeMessage(getScheduler().getWorkers().size() + 1));
						LoginMessage message = (LoginMessage) input.readObject();
						System.out.println(message + " " + socket.getRemoteSocketAddress());

						WorkerInfo workerInfo = new WorkerInfo(getScheduler(), getScheduler().getWorkers().size() + 1, message.getAvailableProcessors(), input, output, socket);
						workerInfo.start();
						getScheduler().getWorkers().add(workerInfo);
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
	 * Send a request to the workers nodes.
	 * 
	 * @param requestMessage
	 * @throws IOException
	 */
	public synchronized void sendRequest(RequestMessage requestMessage) throws IOException {
		for (WorkerInfo worker : getScheduler().getWorkers()) {
			// at each one, create a simple thread to send message
			worker.request(requestMessage);
		}
	}

	/**
	 * @return the {@link Dispatcher} that this communicator is related
	 */
	public Dispatcher getScheduler() {
		return scheduler;
	}

	/**
	 * It is not usable, because the {@link SchedulerCommunicator} communicate with the {@link WorkerCommunicator} by a point to point connection between each one.
	 * 
	 * @return <b>always</b> <code>null</code>
	 */
	public Message receiveMessage() throws IOException, ClassNotFoundException {
		return null;
	}

	public void sendMessage(Message message) throws IOException {
		synchronized (getScheduler().getWorkers()) {
			for (WorkerInfo worker : getScheduler().getWorkers()) {
				worker.sendMessage(message);
			}
		}
	}

	public boolean isReady() {
		return isReady;
	}
}

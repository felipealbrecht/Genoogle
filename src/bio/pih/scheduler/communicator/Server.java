package bio.pih.scheduler.communicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.communicator.message.LoginMessage;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.WelcomeMessage;

public class Server {
	private volatile boolean running;
	private ServerSocket ss;
	private int port = 5555;
	private List<WorkerInfo> workers;

	public Server() throws IOException {
		workers = new LinkedList<WorkerInfo>();
		ss = new ServerSocket(port);
	}

	public void start() {
		Runnable r = new Runnable() {
			public void run() {
				ObjectInputStream input = null;
				ObjectOutputStream output = null;
				WorkerInfo workerInfo = null;
				LoginMessage message;
				try {
					while (running) {
						Socket accept = ss.accept();

						input = new ObjectInputStream(accept.getInputStream());
						output = new ObjectOutputStream(accept.getOutputStream());

						output.writeObject(new WelcomeMessage(workers.size() + 1));
						output.flush();
						message = (LoginMessage) input.readObject();
						System.out.println(message + " " + accept.getRemoteSocketAddress());
						workerInfo = new WorkerInfo(workers.size() + 1, message.getAvailableProcessors(), input, output, accept);
						workerInfo.startThread();
						workers.add(workerInfo);
						System.out.println(workerInfo);
					}
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					running = false;
				}
			}
		};
		running = true;
		new Thread(r).start();
	}

	public void stop() {
		running = false;
	}

	class WorkerInfo {
		volatile boolean running;
		int identifier;
		ObjectInputStream input;
		ObjectOutputStream output;
		Socket socket;
		int searchesRunning;
		int availableProcessors;
		Runnable thread;
		List<Search> waitingList;

		public WorkerInfo(int identifier, int availableProcessors, ObjectInputStream input, ObjectOutputStream output, Socket socket) {
			this.identifier = identifier;
			this.input = input;
			this.output = output;
			this.socket = socket;
			this.availableProcessors = availableProcessors;
			this.searchesRunning = 0;
		}

		public void disconect() throws IOException {
			socket.close();
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
			while (((m = (Message) input.readObject()) != null)) {
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
	
	public class Search {
		public Search() {
			
		}
	}
}

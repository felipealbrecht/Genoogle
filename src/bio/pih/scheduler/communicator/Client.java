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

public class Client implements Communicator {
	ObjectInputStream ois;
	ObjectOutputStream oos;
	static int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	
	public Client(InetAddress serverAddress, int port) throws IOException, ClassNotFoundException {
		Socket socket = new Socket(serverAddress, port);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
				
		oos.writeObject(new LoginMessage(getAvailableprocessors()));
		oos.flush();
		WelcomeMessage ret = (WelcomeMessage) ois.readObject();
		System.out.println("Client Recebeu: " + ret + " id: " + ret.getId());
		socket.close();		
	}
	
	public int getAvailableprocessors() {
		return availableProcessors;		
	}

	public Message reciveMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(Message message) {
		// TODO Auto-generated method stub
		
	}
}

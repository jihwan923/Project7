package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class ServerMain implements Observer {
	private ArrayList<PrintWriter> clientOutputStreams;
	private ArrayList<Observable> clientList;
	private int clientNum = 0;

	public static void main(String[] args) {
		try {
			new ServerMain().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		clientOutputStreams = new ArrayList<PrintWriter>();
		clientList = new ArrayList<Observable>();
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			
			clientNum++;
			
			/*
			// Display the client number 
			System.out.println("Starting thread for client " + clientNum +
						" at " + new Date() + '\n'); 
			// Find the client's host name, and IP address 
			InetAddress inetAddress = clientSocket.getInetAddress();
			System.out.println("Client " + clientNum + "'s host name is "
						+ inetAddress.getHostName() + "\n");
			System.out.println("Client " + clientNum + "'s IP Address is " 
						+ inetAddress.getHostAddress() + "\n");
			*/
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			clientOutputStreams.add(writer);
			ClientHandler newHandler = new ClientHandler(clientSocket, clientNum);
			Thread t = new Thread(newHandler);
			clientList.add(newHandler);
			
			t.start();
			System.out.println("got a connection");
		}

	}

	private void notifyClients(String message) {
		for (PrintWriter writer : clientOutputStreams) {
			writer.println(message);
			writer.flush();
		}
	}

	class ClientHandler extends Observable implements Runnable {
		private BufferedReader reader;
		private int number;

		public ClientHandler(Socket clientSocket, int clientNum) throws IOException {
			Socket sock = clientSocket;
			this.number = clientNum;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					notifyClients("Client " + number + ": " + message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) { // observer update
		clientList.remove(o);
	}

}

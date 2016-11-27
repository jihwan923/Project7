package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ServerMain extends Application implements Observer {
	private TextArea serverLog = new TextArea();
	private ArrayList<PrintWriter> clientOutputStreams;
	private ArrayList<Observable> clientList;
	private ArrayList<String> clientNameList;
	private int clientNum = 0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage){
		serverLog.setEditable(false);
		serverLog.setLayoutX(10);
		serverLog.setLayoutY(10);
		
		Button buttonQuit = new Button("Quit Server");
		buttonQuit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	System.exit(0);            
            }
        });
		buttonQuit.setLayoutX(225);
		buttonQuit.setLayoutY(215);
		//ScrollPane scPane = new ScrollPane(serverLog);
		Pane p = new Pane();
		p.getChildren().add(serverLog);
		p.getChildren().add(buttonQuit);
		Scene scene = new Scene(p, 500, 260);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		new Thread( () -> { 
			try {  // Create a server socket 
				clientOutputStreams = new ArrayList<PrintWriter>();
				clientList = new ArrayList<Observable>();
				clientNameList = new ArrayList<String>();
				
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
					
					String newName = "" + clientNum;
					clientNameList.add(newName);
					
					String outMessage = "*setClientName*" + newName;
					writer.println(outMessage);
					
					String outwardMessage = "*peopleOnline*";
					for(String s: clientNameList){
						outwardMessage = outwardMessage + s + ",";
					}
					for (PrintWriter w : clientOutputStreams) {
						w.println(outwardMessage);
						w.flush();
					}
					
					outwardMessage = "*joinedGroup!*" + newName;
					for (PrintWriter wr : clientOutputStreams) {
						wr.println(outwardMessage);
						wr.flush();
					}
					
					
					
					t.start();
					serverLog.appendText("got a connection with " + newName + "\n");
				}
			} 
			catch(IOException ex) { 
				System.err.println(ex);
			}
		}).start();
		
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
		private String name;

		public ClientHandler(Socket clientSocket, int clientNum) throws IOException {
			Socket sock = clientSocket;
			this.number = clientNum;
			this.name = "" + this.number;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if(message.startsWith("*sendToAll*")){
						message = message.replace("*sendToAll*", "");
						for(int i = 0; i < clientOutputStreams.size(); i++){
							clientOutputStreams.get(i).println(this.name + ": " + message);
							clientOutputStreams.get(i).flush();
						}
					}
					else{
						String[] messageParsed = message.split(",");
						List<String> listParsed = Arrays.asList(messageParsed);
						String actualMessage = messageParsed[messageParsed.length - 1].replace("*privateMessages*", "");
						for(int i = 0; i < clientOutputStreams.size(); i++){
							if(listParsed.contains(clientNameList.get(i))){
								clientOutputStreams.get(i).println(this.name + ": " + actualMessage);
								clientOutputStreams.get(i).flush();
							}
						}
					}
					serverLog.appendText("read " + message + "\n");
					//notifyClients("Client " + number + ": " + message);
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

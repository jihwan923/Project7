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
	private boolean newClientAdded;

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
					//newClientAdded = false;
					PrintWriter newWriter = new PrintWriter(clientSocket.getOutputStream());
					//clientOutputStreams.add(newWriter);
					ClientHandler newHandler = new ClientHandler(clientSocket, clientNum, newWriter);
					Thread t = new Thread(newHandler);
					clientList.add(newHandler);
					t.start();
					
					//String newName = newHandler.name;
					//clientNameList.add(newName);
					
					//String outMessage = "*setClientName*" + newName;
					//newWriter.println(outMessage);
					
					/*
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
					
					
					serverLog.appendText("got a connection with " + newName + "\n");*/
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
		private PrintWriter writer;
		private int number;
		public String name;

		public ClientHandler(Socket clientSocket, int clientNum, PrintWriter wr) throws IOException {
			Socket sock = clientSocket;
			this.number = clientNum;
			this.writer = wr;
			this.name = "" + this.number;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if(message.startsWith("*sendToAll*")){
						synchronized(clientOutputStreams){
							message = message.replace("*sendToAll*", "");
							for(int i = 0; i < clientOutputStreams.size(); i++){
								clientOutputStreams.get(i).println(this.name + ": " + message);
								clientOutputStreams.get(i).flush();
							}
						}
						serverLog.appendText("read " + message + "\n");
					}
					else if(message.startsWith("*sendingNewUserName*")){
						synchronized(clientOutputStreams){
							String userNameInput = message.replace("*sendingNewUserName*", "");
							this.name = userNameInput;
							clientOutputStreams.add(this.writer);
							clientNameList.add(this.name);
							
							String outwardMessage = "*peopleOnline*";
							for(String s: clientNameList){
								outwardMessage = outwardMessage + s + ",";
							}
							for (PrintWriter w : clientOutputStreams) {
								w.println(outwardMessage);
								w.flush();
							}
							
							outwardMessage = "*joinedGroup!*" + this.name;
							for (PrintWriter wr : clientOutputStreams) {
								wr.println(outwardMessage);
								wr.flush();
							}
						}
						serverLog.appendText("got a connection with " + this.name + "\n");
					}
					else if(message.startsWith("*CreatedNewGroup!*")){
						synchronized(clientOutputStreams){
							String[] messageParsed = message.replace("*CreatedNewGroup!*", "").split(",");
							List<String> listParsed = Arrays.asList(messageParsed);
							String groupName = messageParsed[0];
							for(int i = 0; i < clientOutputStreams.size(); i++){
								if(listParsed.contains(clientNameList.get(i))){
									clientOutputStreams.get(i).println(message);
									clientOutputStreams.get(i).flush();
								}
							}
						}
					}
					else{
						synchronized(clientOutputStreams){
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
					}
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

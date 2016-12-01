/* CHATROOM ServerMain.java
 * EE422C Project 7 submission by
 * Jihwan Lee
 * jl54387
 * 16445
 * Kevin Liang
 * kgl392
 * 16445
 * Slip days used: <1>
 * Fall 2016
 */

package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

public class ServerMain extends Application {
	private TextArea serverLog = new TextArea();
	private ArrayList<ClientObserver> clientOutputStreams;
	private ArrayList<Observable> clientList;
	private ArrayList<String> clientNameList;
	private int clientNum = 0;
	private boolean newClientAdded;
	private ArrayList<String> modList = new ArrayList<String>();
	private String password = "EE422C";

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
		
		Pane p = new Pane();
		p.getChildren().add(serverLog);
		p.getChildren().add(buttonQuit);
		Scene scene = new Scene(p, 500, 260);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		Server newServer = new Server();
		
		try{
			newServer.setUpNetworking();
		}
		catch(Exception e){
			
		}
	}
	
	public class Server extends Observable{
		
		public void setUpNetworking() throws Exception{
			new Thread( () -> { // start up the server thread
				try {  // Create a server socket 
					clientOutputStreams = new ArrayList<ClientObserver>();
					clientList = new ArrayList<Observable>();
					clientNameList = new ArrayList<String>();
					
					@SuppressWarnings("resource")
					ServerSocket serverSock = new ServerSocket(6000);
					while (true) {
						Socket clientSocket = serverSock.accept();
						
						clientNum++;
						
						ClientObserver newWriter = new ClientObserver(clientSocket.getOutputStream());
						ClientHandler newHandler = new ClientHandler(clientSocket, clientNum, newWriter);
						Thread t = new Thread(newHandler);
						t.start();
						this.addObserver(newWriter);
					}
				} 
				catch(IOException ex) { 
					System.err.println(ex);
				}
			}).start();
		}
		
		class ClientHandler implements Runnable {
			private BufferedReader reader;
			private ClientObserver writer;
			private int number;
			public String name;

			public ClientHandler(Socket clientSocket, int clientNum, ClientObserver wr) throws IOException {
				Socket sock = clientSocket;
				this.number = clientNum;
				this.writer = wr;
				this.name = "" + this.number;
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			}

			public void run() {
				String message;
				try {
					while (((message = reader.readLine()) != null)) {
						if(message.startsWith("*sendToAll*")){
							synchronized(this){
								message = message.replace("*sendToAll*", "");
								for(int i = 0; i < clientOutputStreams.size(); i++){
									clientOutputStreams.get(i).println(this.name + ": " + message);
									clientOutputStreams.get(i).flush();
								}
							}
							serverLog.appendText("read " + message + "\n");
						}
						else if(message.startsWith("*sendingNewUserName*")){
							synchronized(this){
								String userNameInput = message.replace("*sendingNewUserName*", "");
								if(clientNameList.contains(userNameInput)){ // checking if it already exists
									int num = 1;
									String tempName = userNameInput + num;
									while(clientNameList.contains(tempName)){
										num++;
										tempName = userNameInput + num;
									}
									userNameInput = userNameInput + num;
								}
								
								this.name = userNameInput;
								clientOutputStreams.add(this.writer);
								clientNameList.add(this.name);
								
								String nameMessage = "*setClientName*" + this.name;
								this.writer.println(nameMessage);
								this.writer.flush();
								
								String outwardMessage = "*peopleOnline*";
								for(String s: clientNameList){
									outwardMessage = outwardMessage + s + ",";
								}
								
								setChanged();
								notifyObservers(outwardMessage);
								
								outwardMessage = "*joinedGroup!*" + this.name;
								setChanged();
								notifyObservers(outwardMessage);
							}
							serverLog.appendText("got a connection with " + this.name + "\n");
						}
						else if(message.startsWith("*CreatedNewGroup!*")){
							synchronized(this){
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
						else if(message.startsWith("*AddThesePeopleToGroup*")){//
							synchronized(this){
								String[] messageParsed = message.replace("*AddThesePeopleToGroup*", "").split(",");
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
						else if(message.startsWith("*LoggedOut*")){
							synchronized(this){
								String userToRemove = message.replace("*LoggedOut*", "");
								int i = 0;
								for(i = 0; i < clientNameList.size(); i++){
									if(clientNameList.get(i).equals(userToRemove)){
										clientNameList.remove(i);
										clientOutputStreams.remove(i);
										break;
									}
								}
								
								//setChanged();
								//notifyObservers(message);
								
								for(PrintWriter w: clientOutputStreams){
									w.println(message);
									w.flush();
								}
							}
						}
						else if(message.startsWith("*RequestMod*")){
							synchronized(this){
								String userRequestingMod = message.replace("*RequestMod*", "");
								String [] parseStrings = userRequestingMod.split(",");
								List<String> passwordList = Arrays.asList(parseStrings);
								
								
								if(passwordList.contains(password)){
									this.writer.println("*ModSuccess*");
								}
							}
						}
						else if(message.startsWith("*BanPeople*")){
							synchronized(this){
								String peopleToBeRemoved = message.replace("*BanPeople*", "");
								String[] peopleList = peopleToBeRemoved.split(",");
								List<String> listParsed = Arrays.asList(peopleList);
								for(int i = 0; i < clientOutputStreams.size(); i++){
									if(listParsed.contains(clientNameList.get(i))){
										clientOutputStreams.get(i).println("*BanPeople*");
										clientOutputStreams.get(i).flush();
									}
								}
							}
						}
						else{ // else, it is just a message between users
							synchronized(this){
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
	}
	
	public class ClientObserver extends PrintWriter implements Observer {
		public ClientObserver(OutputStream out) {
			super(out);
		}
		@Override
		public void update(Observable o, Object arg) { // used to send same message to all the clients
			this.println(arg); //writer.println(arg);
			this.flush(); //writer.flush();
		}
	}
}

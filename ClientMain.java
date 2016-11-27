package assignment7;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets; 
import javafx.geometry.Pos; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane; 
import javafx.scene.control.TextArea;

import javafx.scene.control.TextField; 
import javafx.scene.layout.BorderPane; 
import javafx.stage.Stage; 


public class ClientMain extends Application{ 
	private BufferedReader fromServer;
	private PrintWriter toServer;
	private TextArea incoming;
	private String serverName;
	private String name; 
	
	private DataOutputStream dataToServer = null; 
	private DataInputStream dataFromServer = null;
	public MenuButton clientsOnline = new MenuButton();
	public ArrayList<String> clientNameList = new ArrayList<String>();
	public TextArea availableClientsText;
	public Stage pS;

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Panel p to hold the label and text field 
		
		pS = primaryStage;
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Enter the server name: ")); 

		TextField ipField = new TextField(); 
		ipField.setAlignment(Pos.BOTTOM_RIGHT); 
		paneForTextField.setCenter(ipField); 


		BorderPane chatTextField = new BorderPane();
		chatTextField.setPadding(new Insets(5, 5, 5, 5));
		chatTextField.setStyle("-fx-border-color: red");
		chatTextField.setLeft(new Label("Chat: "));
		
		TextField outgoing = new TextField();
		outgoing.setAlignment(Pos.BOTTOM_RIGHT);
		chatTextField.setCenter(outgoing);
		
		Button sendButton = new Button("Send");
		
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(dataToServer != null && dataFromServer != null){
            		int numSent = 0;
            		String totalMessage = "";
            		for(int i = 0; i < clientsOnline.getItems().size(); i++){
            			CustomMenuItem m = (CustomMenuItem)clientsOnline.getItems().get(i);
            			CheckBox c = (CheckBox)m.getContent();
            			if(c.isSelected()){
            				String clientName = clientNameList.get(i);
            				totalMessage = totalMessage + clientName + ",";
            				numSent++;
            			}
            		}
            		
            		String chatMessage = outgoing.getText();
            		if(numSent == 0){
            			totalMessage = "*sendToAll*" + chatMessage;
            		}
            		else{
            			totalMessage = totalMessage + name + ","; // include the messenger as well
            			totalMessage = totalMessage + "*privateMessages*" + chatMessage;
            		}
            		toServer.println(totalMessage);
            		toServer.flush();
            		outgoing.setText("");
            		outgoing.requestFocus();
            	}
            }
        });
		chatTextField.setRight(sendButton);

		BorderPane mainPane = new BorderPane(); 
		
		incoming = new TextArea(); // text area for the incoming server message
		incoming.setPrefWidth(496);
		incoming.setPrefHeight(320);
		incoming.setEditable(false); // prevent the users to edit the text areas
		mainPane.setCenter(new ScrollPane(incoming)); 
		mainPane.setTop(paneForTextField); 
		mainPane.setBottom(chatTextField);
		
		availableClientsText = new TextArea();
		availableClientsText.setPrefWidth(200);
		availableClientsText.setPrefHeight(200);
		availableClientsText.setEditable(false);
		mainPane.setRight(new ScrollPane(availableClientsText));
		
		mainPane.setLeft(clientsOnline);
		
		/*
		 * Create GROUP************
		 */
		Button groupButton = new Button("Create Group");
		
		groupButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	
            }
        });
		
		Button logOutButton = new Button("Log Out");
		
		logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	
            }
        });
		
		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 500, 400); 
		primaryStage.setTitle("Client"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 

		ipField.setOnAction(e -> { 
			try { 
				// Get the radius from the text field 
				String ipName = ipField.getText();
				serverName = ipName;
				
				@SuppressWarnings("resource")
				Socket socket = new Socket(serverName, 4242); 
				
				InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
				fromServer = new BufferedReader(streamReader);
				toServer = new PrintWriter(socket.getOutputStream());
				
				dataFromServer = new DataInputStream(socket.getInputStream()); // create an input stream to receive data from server 
				dataToServer = new DataOutputStream(socket.getOutputStream()); // create an output stream to send data to server

				availableClientsText.appendText("Other People Online:\n");
				//System.out.println("networking established");
				Thread clientThread = new Thread(new IncomingReader(socket));
				clientThread.start();
			} 
			catch (IOException ex) { 
				System.err.println(ex); 
			} 
		}); 
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	class IncomingReader implements Runnable {
		public boolean clientRunning;
		private Socket clientSocket;
		
		public IncomingReader(Socket s){
			this.clientSocket = s;
			clientRunning = true;
		}
		
		public void stopChat(){
			try{
				this.clientSocket.close();
			}
			catch(IOException i){
				
			}
		}
		
		public void run() {
			String message;
			try {
				while ((message = fromServer.readLine()) != null) {
					if(message.startsWith("*peopleOnline*")){
						//clientsOnline.getItems().clear();
						//availableClientsText.setText("");
						String[] peopleOnline = message.replace("*peopleOnline*", "").split(",");
						for(int i = 0; i < peopleOnline.length; i++){
							if(!clientNameList.contains(peopleOnline[i]) && !peopleOnline[i].equals(name)){
								CheckBox checkBox = new CheckBox(peopleOnline[i]);
								clientNameList.add(peopleOnline[i]);
								CustomMenuItem newItem = new CustomMenuItem(checkBox);
								newItem.setHideOnClick(false);
								clientsOnline.getItems().add(newItem);
								availableClientsText.appendText(peopleOnline[i] + "\n");
							}
						}
					}
					else if(message.startsWith("*setClientName*")){
						String m = message.replace("*setClientName*", "");
						name = m;
						//pS.setTitle("Client: " + name);
					}
					else if(message.startsWith("*joinedGroup!*")){
						String j = message.replace("*joinedGroup!*", "");
						incoming.appendText(j + " has joined the chat!\n");
					}
					else{
						incoming.appendText(message + "\n");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}



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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
	private boolean initialized;
	public MenuButton groupCreated = new MenuButton();

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Panel p to hold the label and text field 
		initialized = false;
		pS = primaryStage;
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Enter the server name: ")); 
		Label initialPrompt = new Label("Please enter your ID and desired server name:");
		Label userId = new Label("User ID:");
		Label serverPrompt = new Label("Server IP:");
		TextField userIdEntry = new TextField();
		TextField serverEntry = new TextField();
		Button sendInitialInfoButton = new Button("Confirm");
		
		sendInitialInfoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(userIdEntry.getText() != null && serverEntry.getText() != null && !initialized){
            		try { 
            			initialized = true;
            			
        				String ipName = serverEntry.getText();
        				String idName = userIdEntry.getText();
        				serverName = ipName;
        				name = idName;
        				
        				@SuppressWarnings("resource")
        				Socket socket = new Socket(serverName, 4242); 
        				
        				InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        				fromServer = new BufferedReader(streamReader);
        				toServer = new PrintWriter(socket.getOutputStream());
        				
        				dataFromServer = new DataInputStream(socket.getInputStream()); // create an input stream to receive data from server 
        				dataToServer = new DataOutputStream(socket.getOutputStream()); // create an output stream to send data to server

        				String confirmNewName = "*sendingNewUserName*";
        				String newMessage = confirmNewName + idName;
        				toServer.println(newMessage);
        				toServer.flush();
        				userIdEntry.setText("");
                		userIdEntry.requestFocus();
                		serverEntry.setText("");
                		serverEntry.requestFocus();
                		userIdEntry.setDisable(true);
                		serverEntry.setDisable(true);
        				sendInitialInfoButton.setDisable(true);
        				initialPrompt.setText("ChatRoom Access: " + idName);
        				//availableClientsText.appendText("Other People Online:\n");
        				//System.out.println("networking established");
        				Thread clientThread = new Thread(new IncomingReader(socket));
        				clientThread.start();
        			} 
        			catch (IOException ex) { 
        				System.err.println(ex); 
        			} 
            	}
            }
        });

		Label chatLabel = new Label("Chat: ");
		
		TextField outgoing = new TextField();
		outgoing.setAlignment(Pos.BOTTOM_RIGHT);
		
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
		
		incoming = new TextArea(); // text area for the incoming server message
		incoming.setPrefWidth(300);
		incoming.setPrefHeight(320);
		incoming.setEditable(false); // prevent the users to edit the text areas
		
		Label peopleOnlineLabel = new Label("Available People:");
		availableClientsText = new TextArea();
		availableClientsText.setPrefWidth(100);
		availableClientsText.setPrefHeight(200);
		availableClientsText.setEditable(false);
		
		Label privateMessageLabel = new Label("Select people to\n message to:");
		
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
		
		initialPrompt.setLayoutX(200);
		initialPrompt.setLayoutY(5);
		initialPrompt.setFont(Font.font("Verdana",15));
		initialPrompt.setTextFill(Color.BLUE);
		userId.setLayoutX(200);
		userId.setLayoutY(50);
		serverPrompt.setLayoutX(200);
		serverPrompt.setLayoutY(100);
		userIdEntry.setLayoutX(260);
		userIdEntry.setLayoutY(45);
		userIdEntry.setFont(Font.font("Tahoma"));
		serverEntry.setLayoutX(260);
		serverEntry.setLayoutY(95);
		serverEntry.setFont(Font.font("Tahoma"));
		sendInitialInfoButton.setLayoutX(430);
		sendInitialInfoButton.setLayoutY(95);
		peopleOnlineLabel.setLayoutX(10);
		peopleOnlineLabel.setLayoutY(150);
		peopleOnlineLabel.setFont(Font.font("Cambria", 12));
		peopleOnlineLabel.setTextFill(Color.GREEN);
		availableClientsText.setLayoutX(10);
		availableClientsText.setLayoutY(175);
		privateMessageLabel.setLayoutX(140);
		privateMessageLabel.setLayoutY(140);
		privateMessageLabel.setFont(Font.font("Cambria", 12));
		privateMessageLabel.setTextFill(Color.DARKGREEN);
		clientsOnline.setLayoutX(140);
		clientsOnline.setLayoutY(175);
		incoming.setLayoutX(240);
		incoming.setLayoutY(140);
		chatLabel.setLayoutX(200);
		chatLabel.setLayoutY(460);
		chatLabel.setFont(Font.font("Impact", 12));
		chatLabel.setTextFill(Color.FORESTGREEN);
		outgoing.setLayoutX(240);
		outgoing.setLayoutY(460);
		outgoing.setPrefWidth(300);
		sendButton.setLayoutX(540);
		sendButton.setLayoutY(460);
		sendButton.setTextFill(Color.DARKBLUE);
		
		Pane mainPane = new Pane(); 
		mainPane.getChildren().add(initialPrompt);
		mainPane.getChildren().add(userId);
		mainPane.getChildren().add(serverPrompt);
		mainPane.getChildren().add(userIdEntry);
		mainPane.getChildren().add(serverEntry);
		mainPane.getChildren().add(sendInitialInfoButton);
		mainPane.getChildren().add(peopleOnlineLabel);
		mainPane.getChildren().add(availableClientsText);
		mainPane.getChildren().add(privateMessageLabel);
		mainPane.getChildren().add(clientsOnline);
		mainPane.getChildren().add(incoming);
		mainPane.getChildren().add(chatLabel);
		mainPane.getChildren().add(outgoing);
		mainPane.getChildren().add(sendButton);
		
		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 700, 500); 
		primaryStage.setTitle("Chatroom"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 
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



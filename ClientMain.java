package assignment7;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage; 


public class ClientMain extends Application{ 
	private BufferedReader fromServer;
	private PrintWriter toServer;
	private TextArea incoming;
	private String serverName;
	private String name; 
	
	private Socket mainSocket;
	private DataOutputStream dataToServer = null; 
	private DataInputStream dataFromServer = null;
	public MenuButton clientsOnline = new MenuButton();
	public ArrayList<String> clientNameList = new ArrayList<String>();
	public TextArea availableClientsText;
	public Stage pS;
	private boolean initialized;
	public MenuButton groupCreated = new MenuButton();
	public ArrayList<String> groupClientList = new ArrayList<String>();
	public ArrayList<ArrayList<String>> clientsInGroupsLists = new ArrayList<ArrayList<String>>();////
	public TextArea groupListArea = new TextArea();
	public ArrayList<String> groupNameList = new ArrayList<String>();
	public HashSet<String> groupNameListCheck = new HashSet<String>();
	
	public HashSet<String> friendCheckList = new HashSet<String>();///////
	public ArrayList<String> friendList = new ArrayList<String>();//////

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Panel p to hold the label and text field 
		
		Stage initialMessageWindow = new Stage();
		
		final ImageView imv = new ImageView();
        final Image image1 = new Image(getClass().getResourceAsStream("/resource/SmileyFace.png"));
        imv.setImage(image1);
        imv.setFitHeight(100);
        imv.setFitWidth(100);
        
        final ImageView imv2 = new ImageView();
        final Image image2 = new Image(getClass().getResourceAsStream("/resource/ChatRoom.png"));
        imv2.setImage(image2);
        imv2.setFitHeight(120);
        imv2.setFitWidth(120);
		
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
		Button sendButton = new Button("Send");
		Button groupButton = new Button("Create Group");
		Button addPeopleToGroupButton = new Button("Add To Group");
		Button logOutButton = new Button("Log Out");
		Button sendFriendRequestButton = new Button("Send Friend Request");///////////
		
		sendInitialInfoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(!userIdEntry.getText().isEmpty() && !serverEntry.getText().isEmpty() && !initialized){
            		try { 
            			initialized = true;
            			
        				String ipName = serverEntry.getText();
        				String idName = userIdEntry.getText();
        				serverName = ipName;
        				name = idName;
        				
        				@SuppressWarnings("resource")
        				Socket socket = new Socket(serverName, 6000); 
        				mainSocket = socket;
        				
        				InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        				fromServer = new BufferedReader(streamReader);
        				toServer = new PrintWriter(socket.getOutputStream());
        				
        				dataFromServer = new DataInputStream(socket.getInputStream()); // create an input stream to receive data from server 
        				dataToServer = new DataOutputStream(socket.getOutputStream()); // create an output stream to send data to server

        				String confirmNewName = "*sendingNewUserName*";
        				String newMessage = confirmNewName + idName;
        				toServer.println(newMessage);
        				toServer.flush();
        				primaryStage.show();
        				initialMessageWindow.close();
        				userIdEntry.setText("");
                		userIdEntry.requestFocus();
                		serverEntry.setText("");
                		serverEntry.requestFocus();
                		userIdEntry.setDisable(true);
                		serverEntry.setDisable(true);
        				sendInitialInfoButton.setDisable(true);
        				sendButton.setDisable(false);
        				groupButton.setDisable(false);
        				logOutButton.setDisable(false);
        				addPeopleToGroupButton.setDisable(false);
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
		
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(dataToServer != null && dataFromServer != null){
            		int numGroupPeopleSelected = 0;
            		int k = 0;
            		for(k = 0; k < groupCreated.getItems().size(); k++){
            			CustomMenuItem m = (CustomMenuItem)groupCreated.getItems().get(k);
            			CheckBox c = (CheckBox)m.getContent();
            			if(c.isSelected()){
            				numGroupPeopleSelected += 1;
            				break;
            			}
            		}
            		
            		String totalMessage = "";
            		
            		if(numGroupPeopleSelected == 0){
            			int numSent = 0;
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
            		}
            		else{
            			
            			String groupClientsNames = groupClientList.get(k);
            			String[] membersInGroup = groupClientsNames.split(",");
            			String groupNameExtracted = membersInGroup[0];
            			/*for(int r = 1; r < membersInGroup.length; r++){
            				totalMessage = totalMessage + membersInGroup[r] + ",";
            			}*/
            			for(String s: clientsInGroupsLists.get(k)){
            				totalMessage = totalMessage + s + ",";
            			}
            			
            			String messageToBeSent = outgoing.getText();
            			totalMessage = totalMessage + "*privateMessages*" + "*" + groupNameExtracted + "*: " + messageToBeSent;
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
		incoming.setPrefHeight(450);
		incoming.setEditable(false); // prevent the users to edit the text areas
		
		Label peopleOnlineLabel = new Label("Available People:");
		availableClientsText = new TextArea();
		availableClientsText.setPrefWidth(100);
		availableClientsText.setPrefHeight(200);
		availableClientsText.setEditable(false);
		
		Label privateMessageLabel = new Label("Select people to\n message to:");
		
		Label groupCreateLabel = new Label("Enter name of new group:\n*People selected to receive\nmessage will be added*");
		TextField newGroupField = new TextField();
		Label selectGroupLabel = new Label("Select Group to\nsend message to: ");
		Label groupYouArePartOf = new Label("Groups you are part of:");
		groupListArea.setPrefWidth(100);
		groupListArea.setPrefHeight(150);
		groupListArea.setEditable(false);
		
		
		/*
		 * Create GROUP************
		 */
		
		groupButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(!newGroupField.getText().isEmpty()){
            		int peopleSelected = 0;
            		String totalMessage = "*CreatedNewGroup!*";
            		String newGroupName = newGroupField.getText();
            		totalMessage = totalMessage + newGroupName + ",";
            		for(int i = 0; i < clientsOnline.getItems().size(); i++){
            			CustomMenuItem m = (CustomMenuItem)clientsOnline.getItems().get(i);
            			CheckBox c = (CheckBox)m.getContent();
            			if(c.isSelected()){
            				String clientName = clientNameList.get(i);
            				totalMessage = totalMessage + clientName + ",";
            				peopleSelected++;
            			}
            		}
            		
            		//groupClientList.add(newGroupName);
            		if(peopleSelected == 0){
            			incoming.appendText("Please select people to add to the group before creating a group!!!\n");
            		}
            		else{
            			totalMessage = totalMessage + name + ","; // include the messenger as well
            			toServer.println(totalMessage);
            			toServer.flush();
            			newGroupField.setText("");
            			newGroupField.requestFocus();
            		}
            	}
            }
        });
		
		Label addPeopleLabel = new Label("Choose people\nto add to checked group");
		
		addPeopleToGroupButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	if(true){
            		int peopleSelected = 0;
            		int groupSelected = 0;
            		String totalMessage = "*AddThesePeopleToGroup*";
            		List<String> peopleInGroupList = new ArrayList<String>();
            		
            		for(int g = 0; g < groupCreated.getItems().size(); g++){
            			CustomMenuItem mG = (CustomMenuItem)groupCreated.getItems().get(g);
            			CheckBox cG = (CheckBox)mG.getContent();
            			if(cG.isSelected()){
            				String groupPeopleList = groupClientList.get(g);
            				String[] groupParsed = groupPeopleList.split(",");
							peopleInGroupList = Arrays.asList(groupParsed);
            				
            				String groupName = groupNameList.get(g);
            				groupSelected++;
            				totalMessage = totalMessage + groupPeopleList;
            				break;
            			}
            		}

            		for(int i = 0; i < clientsOnline.getItems().size(); i++){
            			CustomMenuItem m = (CustomMenuItem)clientsOnline.getItems().get(i);
            			CheckBox c = (CheckBox)m.getContent();
            			if(c.isSelected()){
            				String clientName = clientNameList.get(i);
            				if(peopleInGroupList != null && !peopleInGroupList.contains(clientName)){
            					totalMessage = totalMessage + clientName + ",";
                				peopleSelected++;
            				}
            			}
            		}
            		
            		//groupClientList.add(newGroupName);
            		if(groupSelected == 0){
            			incoming.appendText("Choose an appropriate group!\n");
            		}
            		else if(peopleSelected == 0){
            			incoming.appendText("Select appropriate people to add to the group!\n");
            		}
            		else{
            			//incoming.appendText("Group updated!\n");
            			toServer.println(totalMessage);
            			toServer.flush();
            		}
            	}
            }
        });
		
		
		logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	String message = "*LoggedOut*";
            	message = message + name;
            	toServer.println(message);
            	toServer.flush();
            	try {
					mainSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	System.exit(0);
            }
        });
		
		sendFriendRequestButton.setOnAction(new EventHandler<ActionEvent>() {////////////////////
            @Override
            public void handle(ActionEvent event) {
            	String message = "*AddFriends*";
            	message = message + name;
            	toServer.println(message);
            	toServer.flush();
            	try {
					mainSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	System.exit(0);
            }
        });
		
		imv.setLayoutX(50);
		imv.setLayoutY(20);
		imv2.setLayoutX(70);
		imv2.setLayoutY(10);
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
		incoming.setLayoutY(10);
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
		groupCreateLabel.setLayoutX(560);
		groupCreateLabel.setLayoutY(140);
		groupCreateLabel.setFont(Font.font("Cambria", 12));
		groupCreateLabel.setTextFill(Color.NAVY);
		newGroupField.setLayoutX(560);
		newGroupField.setLayoutY(200);
		newGroupField.setFont(Font.font("Impact"));
		newGroupField.setPrefWidth(100);
		groupButton.setLayoutX(660);
		groupButton.setLayoutY(200);
		groupButton.setTextFill(Color.DARKRED);
		selectGroupLabel.setLayoutX(140);
		selectGroupLabel.setLayoutY(250);
		selectGroupLabel.setFont(Font.font("Cambria", 12));
		selectGroupLabel.setTextFill(Color.DARKRED);
		groupCreated.setLayoutX(140);
		groupCreated.setLayoutY(285);
		groupYouArePartOf.setLayoutX(560);
		groupYouArePartOf.setLayoutY(240);
		groupYouArePartOf.setFont(Font.font("Cambria", 12));
		groupYouArePartOf.setTextFill(Color.MEDIUMSEAGREEN);
		groupListArea.setLayoutX(560);
		groupListArea.setLayoutY(265);
		addPeopleLabel.setLayoutX(120);
		addPeopleLabel.setLayoutY(350);
		addPeopleLabel.setFont(Font.font("Cambria", 11));
		addPeopleLabel.setTextFill(Color.DARKGREEN);
		addPeopleToGroupButton.setLayoutX(120);
		addPeopleToGroupButton.setLayoutY(380);
		logOutButton.setLayoutX(10);
		logOutButton.setLayoutY(400);
		
		sendButton.setDisable(true);
		groupButton.setDisable(true);
		addPeopleToGroupButton.setDisable(true);
		logOutButton.setDisable(true);
		
		Pane mainPane = new Pane(); 
		//mainPane.getChildren().add(initialPrompt);
		//mainPane.getChildren().add(userId);
		//mainPane.getChildren().add(serverPrompt);
		//mainPane.getChildren().add(userIdEntry);
		//mainPane.getChildren().add(serverEntry);
		//mainPane.getChildren().add(sendInitialInfoButton);
		mainPane.getChildren().add(peopleOnlineLabel);
		mainPane.getChildren().add(availableClientsText);
		mainPane.getChildren().add(privateMessageLabel);
		mainPane.getChildren().add(clientsOnline);
		mainPane.getChildren().add(incoming);
		mainPane.getChildren().add(chatLabel);
		mainPane.getChildren().add(outgoing);
		mainPane.getChildren().add(sendButton);
		mainPane.getChildren().add(groupCreateLabel);
		mainPane.getChildren().add(newGroupField);
		mainPane.getChildren().add(groupButton);
		mainPane.getChildren().add(selectGroupLabel);
		mainPane.getChildren().add(groupCreated);
		mainPane.getChildren().add(groupYouArePartOf);
		mainPane.getChildren().add(groupListArea);
		mainPane.getChildren().add(addPeopleToGroupButton);
		mainPane.getChildren().add(addPeopleLabel);
		mainPane.getChildren().add(logOutButton);
		mainPane.getChildren().add(imv2);
		
		
		Pane popUpPane = new Pane();
		popUpPane.getChildren().add(initialPrompt);
		popUpPane.getChildren().add(userId);
		popUpPane.getChildren().add(serverPrompt);
		popUpPane.getChildren().add(userIdEntry);
		popUpPane.getChildren().add(serverEntry);
		popUpPane.getChildren().add(sendInitialInfoButton);
		popUpPane.getChildren().add(imv);
		
		Scene popUpScene = new Scene(popUpPane, 580, 150);
		initialMessageWindow.setTitle("Log-In Set Up");
		initialMessageWindow.setScene(popUpScene);
		initialMessageWindow.show();
		
		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 780, 500); 
		primaryStage.setTitle("Chatroom"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		//primaryStage.show(); // Display the stage 
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void printOnScreenChat(String message){
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	incoming.appendText(message);
            }
        });
	}
	
	public void printAvailableClients(String message){
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	availableClientsText.appendText(message);
            }
        });
	}
	
	class IncomingReader extends Observable implements Runnable {
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
								//availableClientsText.appendText(peopleOnline[i] + "\n");
								printAvailableClients(peopleOnline[i] + "\n");
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
						printOnScreenChat(j + " has joined the chat!\n");
						//incoming.appendText(j + " has joined the chat!\n");
					}
					else if(message.startsWith("*CreatedNewGroup!*")){
						String allNewGroupMembers = message.replace("*CreatedNewGroup!*", "");
						String[] newGroupMembers = allNewGroupMembers.split(",");
						
						ArrayList<String> newMembersSet = new ArrayList<String>();
						for(int n = 1; n < newGroupMembers.length; n++){ // exclude first index since first index contains group name
							newMembersSet.add(newGroupMembers[n]);
						}
						String newGroupName = newGroupMembers[0]; // first name is the group name
						CheckBox checkBox = new CheckBox(newGroupName);
						CustomMenuItem newItem = new CustomMenuItem(checkBox);
						groupCreated.getItems().add(newItem);
						newItem.setHideOnClick(false);
						groupListArea.appendText(newGroupName + "\n");
						groupClientList.add(allNewGroupMembers);
						groupNameList.add(newGroupName);
						groupNameListCheck.add(newGroupName);
						clientsInGroupsLists.add(newMembersSet);
						printOnScreenChat("You have been added to " + newGroupName + "\n");
						//incoming.appendText("You have been added to " + newGroupName + "\n");
					}
					else if(message.startsWith("*AddThesePeopleToGroup*")){
						boolean groupExists = false;
						String allNewGroupMembers = message.replace("*AddThesePeopleToGroup*", "");
						String[] newGroupMembers = allNewGroupMembers.split(",");
						String groupName = newGroupMembers[0]; // first name is the group name
						for(int i = 0; i < groupNameList.size(); i++){
							if(groupNameListCheck.contains(groupName)){ // this is for people who are already in the group
								groupExists = true;
								String currentClientListString = groupClientList.get(i);
								String newClientList = currentClientListString;
								String[] currentClientList = currentClientListString.split(",");
								//List<String> listOfCurrentClients = Arrays.asList(currentClientList);
								
								for(int j = 1; j < newGroupMembers.length; j++){
									//newClientList = "";
									if(!clientsInGroupsLists.get(i).contains(newGroupMembers[j])){
										clientsInGroupsLists.get(i).add(newGroupMembers[j]);
										//newClientList = currentClientListString + newGroupMembers[j] + ",";
									}
								}
								//groupClientList.set(i, newClientList);
							}
						}
						if(!groupExists){ // if the person is not in the group
							CheckBox checkBox = new CheckBox(groupName);
							CustomMenuItem newItem = new CustomMenuItem(checkBox);
							groupCreated.getItems().add(newItem);
							newItem.setHideOnClick(false);
							groupListArea.appendText(groupName + "\n");
							groupClientList.add(allNewGroupMembers);
							groupNameList.add(groupName);
							ArrayList<String> newGroupList = new ArrayList<String>();
							for(int s = 1; s < newGroupMembers.length; s++){ // exclude first index since first index contains group name
								newGroupList.add(newGroupMembers[s]);
							}
							clientsInGroupsLists.add(newGroupList);
						}
						printOnScreenChat(groupName + " Updated!\n");
						//incoming.appendText(groupName + " Updated!\n");
					}
					else if(message.startsWith("*LoggedOut*")){
						String userToBeRemoved = message.replace("*LoggedOut*", "");
						for(int i = 0; i < clientNameList.size(); i++){
							if(clientNameList.get(i).equals(userToBeRemoved)){
								clientNameList.remove(i);
								clientsOnline.getItems().remove(i);
							}
						}
						
						for(ArrayList<String> listString: clientsInGroupsLists){ // remove from group lists
							for(int k = 0; k < listString.size(); k++){
								if(listString.get(k).equals(userToBeRemoved)){
									listString.remove(k);
								}
							}
						}
						
						availableClientsText.setText("");
						for(String s: clientNameList){
							availableClientsText.appendText(s + "\n");
						}
						printOnScreenChat(userToBeRemoved + " logged out!\n");
						//incoming.appendText(userToBeRemoved + " logged out!\n");
					}
					else if(message.startsWith("*AddFriends*")){//
						
					}
					else{
						printOnScreenChat(message + "\n");
						//incoming.appendText(message + "\n");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}



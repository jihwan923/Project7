package assignment7;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
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
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label; 
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
	private String clientName; 
	
	private DataOutputStream dataToServer = null; 
	private DataInputStream dataFromServer = null;


	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Panel p to hold the label and text field 
		
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
            		toServer.println(outgoing.getText());
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
					incoming.appendText(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}



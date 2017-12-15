package application;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import data.ClypeData;
import data.FileClypeData;
import data.MessageClypeData;
import data.PhotoClypeData;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.ClypeClient;

public class Main extends Application {

	private int numLinesConvo = 10;
	private int numLinesUsers = numLinesConvo;
	private ClypeClient client;

	public static final int DEFAULT_PORT = 7000;

	public void showLoginWindow(Stage primaryStage) {

		try {
			/*
			 * create root
			 */
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 300, 200);

			/*
			 * title
			 */

			// title text
			Label titleLabel = new Label("Welcome to Clype!");
			titleLabel.setId("login-title");

			// add title to root
			VBox titleVBox = new VBox();
			titleVBox.getChildren().addAll(titleLabel);
			root.setTop(titleVBox);

			/*
			 * credentials input
			 */

			// credential input fields
			TextField usernameInput = new TextField();
			TextField hostnameInput = new TextField();
			hostnameInput.setText(InetAddress.getLocalHost().getHostAddress());
			TextField portInput = new TextField();
			portInput.setText(Integer.toString(DEFAULT_PORT));

			// add fields to root
			VBox credentialsVBox = new VBox();
			credentialsVBox.getChildren().addAll(usernameInput, hostnameInput, portInput);
			root.setCenter(credentialsVBox);

			/*
			 * credentials labels
			 */

			// credential labels
			Label usernameLabel = new Label("Username:");
			Label hostnameLabel = new Label("Host-name:");
			Label portLabel = new Label("Port:");

			VBox credentialsLabels = new VBox();
			credentialsLabels.getChildren().addAll(usernameLabel, hostnameLabel, portLabel);
			credentialsLabels.setSpacing(10);

			// add labels to root
			root.setLeft(credentialsLabels);

			// Error box
			HBox errorBox = new HBox();
			Label errorField = new Label("");
			errorBox.setMinHeight(30);
			errorField.setId("error-field");
			errorBox.getChildren().add(errorField);
			errorBox.setAlignment(Pos.CENTER);

			// login button
			Button login = new Button("Log in");
			login.setId("login-button");
			login.setMinWidth(300);

			HBox buttonBox = new HBox();
			buttonBox.getChildren().add(login);
			buttonBox.setAlignment(Pos.CENTER);
			HBox.setHgrow(buttonBox, Priority.ALWAYS);

			VBox bottomBox = new VBox();
			bottomBox.getChildren().addAll(errorBox, buttonBox);

			// add button to root
			root.setBottom(bottomBox);

			// login button handler: creates new client and shows main window
			login.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					try {
						if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
							String username = usernameInput.getText();
							String hostname = hostnameInput.getText();
							int port = Integer.parseInt(portInput.getText());

							if (!username.isEmpty() && !hostname.isEmpty()) {
								System.out.println("Attempting to connect to server.");
								client = new ClypeClient(username, hostname, port);
                                                                client.connect(); //Starts the socket and object streams

								if (!client.closed()) { // allows us to check if connection was made
									showMainWindow(primaryStage);
								} else {
									errorField.setText("Could not connect to server.");
								}
							} else {
								errorField.setText("Error: Invalid username or host-name given");
							}
						}
					} catch (NumberFormatException nfe) {
						errorField.setText("Invalid port number given.");
					}catch(IllegalArgumentException iae) {
						errorField.setText(iae.getMessage());
					}
					catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

			// show
			primaryStage.setScene(scene);
			primaryStage.setTitle("Clype 2.0 Login");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showMainWindow(Stage primaryStage) {
		try {
			//BorderPane root element
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 900, 900);


			Label titleLabel = new Label("Clype 2.0");

			// add to root
			VBox titleVBox = new VBox();
			titleVBox.getChildren().addAll(titleLabel);
			root.setTop(titleVBox);

			/*
			 * Conversation display
			 */

			Label conversationLabel = new Label("Message Window");

			// list of incoming messages
			TextArea conversationOutputText = new TextArea(); // messages from users box
			conversationOutputText.setPrefRowCount(this.numLinesConvo);
			conversationOutputText.clear();
			conversationOutputText.setWrapText(true);
			conversationOutputText.setEditable(false);
			conversationOutputText.setMinHeight(200);
			conversationOutputText.setPromptText("Messages from other users will appear here!");
			conversationOutputText.setFont(Font.font("Arial", FontWeight.LIGHT, 20));

			// add convoBox to root
			VBox convoBox = new VBox();
			convoBox.getChildren().addAll(conversationLabel, conversationOutputText);
			VBox.setVgrow(conversationOutputText, Priority.ALWAYS);// allows conversation box to grow with window

			root.setCenter(convoBox);

			/*
			 * List users box
			 */

			// label
			Label usersBoxLabel = new Label("Users Window");
			usersBoxLabel.setId("users-box-label");

			// list of users
			TextArea usersList = new TextArea();
			usersList.setId("users-box-list");
			usersList.setPrefRowCount(this.numLinesUsers);
			usersList.setWrapText(true);
			usersList.setMaxWidth(100);
			usersList.setEditable(false);
			usersList.setMinHeight(200);

			// add usersList box to root
			VBox usersBox = new VBox();
			usersBox.setAlignment(Pos.CENTER);
			usersBox.getChildren().addAll(usersBoxLabel, usersList);
			VBox.setVgrow(usersList, Priority.ALWAYS);// Allows the list to expand with the window
			usersBox.setScaleX(.92);
			root.setRight(usersBox);

			/*
			 * Handles incoming messages
			 */
			Task<Void> incomingMessageTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					boolean noMessages = true;// used to handle default text
					boolean closedSocket = false;
					while (!client.closed()) {
						ClypeData messageFromServer = client.receiveData();

						if (messageFromServer.getType() == ClypeData.SENDMESSAGE) {
							MessageClypeData messageDataFromServer = (MessageClypeData) messageFromServer;
							String username = messageDataFromServer.getUserName();
							String message = messageDataFromServer.getData();

							if (!closedSocket) {
								if (noMessages) {
									conversationOutputText.clear();
									noMessages = false;
									conversationOutputText.setText(username + ": " + message);
								} else {
									conversationOutputText.setText(conversationOutputText.getText() + System.getProperty("line.separator")
											+ username + ": " + message);
								}
							}
						} else if (messageFromServer.getType() == ClypeData.LISTUSERS) {
							MessageClypeData users = (MessageClypeData)messageFromServer;
							usersList.setText(users.getData());
						} else if (messageFromServer.getType() == ClypeData.SENDFILE) {
							FileClypeData fileMessageFromServer = (FileClypeData) messageFromServer;
							String username = fileMessageFromServer.getUserName();
							String message = (String)fileMessageFromServer.getData();

							if (!closedSocket) {
								if (noMessages) {
									conversationOutputText.clear();
									noMessages = false;
									conversationOutputText.setText(username + ": " + message);
								} else {
									conversationOutputText.setText(conversationOutputText.getText() + System.getProperty("line.separator")
											+ username + ": " + message);
								}
							}
						} else if (messageFromServer.getType() == ClypeData.PHOTO) {
							PhotoClypeData photoMessageFromServer = (PhotoClypeData) messageFromServer;
							String username = photoMessageFromServer.getUserName();
							RenderedImage message = photoMessageFromServer.getData();

							if (!closedSocket) {
								if (noMessages) {
									conversationOutputText.clear();
									noMessages = false;
									conversationOutputText.setText(username + ": " + message);
								} else {
									conversationOutputText.setText(conversationOutputText.getText() + System.getProperty("line.separator")
										+ username + ": " + message);
								}
							}
						}
					}
					return null;
				}
			};
			Thread recieveMessageThread = new Thread(incomingMessageTask);
			recieveMessageThread.start();

			/*
			 * Box for sending messages
			 */

			// label
			Label sendMessageBoxLabel = new Label("  Send Message Window");
			sendMessageBoxLabel.setId("send-message-box-label");

			// VBox for spacing on left
			VBox leftSpacing = new VBox();
			leftSpacing.setMaxWidth(10);
			leftSpacing.setMinWidth(10);

			// VBox for spacing on right
			VBox rightSpacing = new VBox();
			rightSpacing.setMaxWidth(10);
			rightSpacing.setMinWidth(10);

			// user input box
			TextArea messageInput = new TextArea(); // input box
			messageInput.setPrefRowCount(4);
			messageInput.setWrapText(true);
			messageInput.setStyle("-fx-background-color: transparent");
			messageInput.setFont(Font.font("Arial", FontWeight.LIGHT, 18));
			messageInput.setEditable(true);
			messageInput.setPromptText("Type a message here!");
			
			// button to send message
			Button sendButton = new Button("Send");
			sendButton.setMinSize(63, 150);
			sendButton.setWrapText(true);
			sendButton.setTextAlignment(TextAlignment.CENTER);
			sendButton.setFont(Font.font("Arial", FontWeight.BOLD, 15));
			sendButton.setOnMouseReleased(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
						//if (client.getDataToSendToServer() == null) {
							ClypeData textMessageData = new MessageClypeData(client.getUserName(),
									messageInput.getText(), ClypeData.SENDMESSAGE);
							client.setDataToSendToServer(textMessageData);
						//}
						client.sendData();

						messageInput.clear();
					}
				}

			});

			// button to send media
			Button addFileButton = new Button("Add File");
			addFileButton.setMinSize(55, 150);
			addFileButton.setWrapText(true);
			addFileButton.setTextAlignment(TextAlignment.CENTER);
			addFileButton.setFont(Font.font("Arial", FontWeight.BOLD, 15));

			addFileButton.setOnMouseReleased(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
						FileChooser fileChooser = new FileChooser();
						fileChooser.setTitle("Select File");

						File file = fileChooser.showOpenDialog(primaryStage);
						FileClypeData fileData = new FileClypeData(client.getUserName(), file.getAbsolutePath(),
								ClypeData.SENDFILE);

						try {
							fileData.readFileContents();
							client.setDataToSendToServer(fileData);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}

			});

			Button addPhotoButton = new Button("Add photo");
			addPhotoButton.setMinSize(55, 150);
			addPhotoButton.setWrapText(true);
			addPhotoButton.setTextAlignment(TextAlignment.CENTER);
			addPhotoButton.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
			addPhotoButton.setOnMouseReleased( new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Select File");
					
					File file = fileChooser.showOpenDialog(primaryStage);
					PhotoClypeData photoData = new PhotoClypeData(client.getUserName(), file.getAbsolutePath());
					
					try {
						photoData.readClientData();
						client.setDataToSendToServer(photoData);
					} catch (Exception ioe) {
						ioe.printStackTrace();
					}
				}
				
			});
			
			// HBox to hold both buttons with
			HBox sendMessageButtons = new HBox();
			sendMessageButtons.setCenterShape(true);
			sendMessageButtons.getChildren().addAll(sendButton, addFileButton, addPhotoButton);

			// HBox for all sending message controls
			HBox sendMessageBoxControls = new HBox();
			HBox.setHgrow(messageInput, Priority.ALWAYS);
			HBox.setHgrow(addFileButton, Priority.ALWAYS);
			HBox.setHgrow(addPhotoButton, Priority.ALWAYS);
			HBox.setHgrow(sendButton, Priority.ALWAYS);
			sendMessageBoxControls.getChildren().addAll(leftSpacing, messageInput, sendMessageButtons, rightSpacing);

			/*
			 * set spacing between message receiving box and message sending box
			 */
			HBox topSpacer = new HBox();
			topSpacer.setMaxHeight(10);
			topSpacer.setMinHeight(10);
			HBox bottomSpacer = new HBox();
			bottomSpacer.setMaxHeight(10);
			bottomSpacer.setMinHeight(10);

			// add message controls to root
			VBox sendMessageBox = new VBox();
			sendMessageBox.getChildren().addAll(topSpacer, sendMessageBoxLabel, sendMessageBoxControls, bottomSpacer);
			root.setBottom(sendMessageBox);

			/*
			 * Spacer for left side of border pane
			 */
			VBox leftSpacer = new VBox();
			leftSpacer.setMaxWidth(10);
			leftSpacer.setMinWidth(10);
			root.setLeft(leftSpacer);

			primaryStage.setScene(scene);
			primaryStage.setTitle("Clype 2.0");
			primaryStage.show();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					client.setClosedConnection(true);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		showLoginWindow(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
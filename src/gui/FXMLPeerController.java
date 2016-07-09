/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import common.Common;
import static common.Common.logger;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import peer.Host;

/**
 *  Controller for {@code FXMLPeer}.
 */
public class FXMLPeerController implements Initializable {
    
    /**
     * Special tab that can be used to add new tabs to the {@code roomsTabPane}.
     */
    @FXML private Tab addTab;
    
    /**
     * Tab pane where the chat rooms will be shown.
     */
    @FXML private TabPane roomsTabPane;
    
    private ResourceBundle resourceBundle;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Handles the selection of the tab '+' (add tab).
     */
    @FXML
    private void handleNewTab () {
        
        if (addTab.isSelected()) {
         
            roomsTabPane.getSelectionModel().selectPrevious();
            
            connect();
        }
        
        
    }
    
    /**
     * Sets the language of every component on Spanish.
     */
    @FXML
    private void setSpanish () {
        
        changeLanguage(new Locale("es", "ES"));
    }
    
    /**
     * Sets the language of every component on English.
     */
    @FXML
    private void setEnglish () {
        
        changeLanguage(new Locale("en", "UK"));
    }
    
    
    /**
     * Checks the connection with all the 
     */
    @FXML
    private void checkConnection () {
        
        if (PeerGUI.peer.checkConnection()) {
            
            logger.logWarning("All connections were succesfully checked.\n");
        }
    }
    
    /**
     * Shows a dialog to set the parameters of the new connection and tries to
     * establish it.
     */
    @FXML
    public void connect () {
        
        Host host = ConnectionDialog.showInputDialog(resourceBundle);
        
        if (host != null) {
            
            if (PeerGUI.peer.connect(host)) {
                
                /* Adds a new tab for the connection messages to be shown */
                addTab(host.getDataFlow());
                logger.logWarning("Connected to " + host.toString());
            } else {
                
                logger.logError("Error trying to connect to " + host.toString());
            }
        } else {
            
            roomsTabPane.getSelectionModel().selectFirst();
        }
    }
    
/* ------------------------------ */
/* ---- END OF @FXML METHODS ---- */
/* ------------------------------ */
     
    /**
     * Adds a tab to handle a new connection.
     * 
     * @param chatRoomID 
     *              The ID for the new chat room.
     */
    public synchronized void addTab (byte chatRoomID) {
        
        /* Creates a new tab right beside the '+' tab */
        int newTabPos = roomsTabPane.getTabs().indexOf(addTab);
        Tab newTab = new Tab("Tab" + newTabPos); 
        newTab.setId("chatRoom" + chatRoomID);
        
        /* Sets a handler for a closure request */
        newTab.setOnCloseRequest(e -> {
            
            if (PeerGUI.peer.leaveChatRoom(chatRoomID)) {
                
                logger.logWarning("Disconnected correctly.\n");
            } else {
                
                logger.logWarning("Some peers may have not received the "
                                  + "disconnection message.\n");
            }
        });
        
        /* If a tab for the given connection already exists, exits */
        for (Tab tab : roomsTabPane.getTabs()) {
            
            if ((tab.getId() != null) 
                && tab.getId().equals(newTab.getId())) {
                
                return;
            }
        }
        
        newTab.setContent(newRoomPane(chatRoomID));
        
        /* Adds the new tab and selects it */
        roomsTabPane.getTabs().add(newTabPos, newTab);
        roomsTabPane.getSelectionModel().select(newTab);
    }
    
    /**
     * Creates and returns a pane with all the elements for the new chat room 
     * tab.
     * 
     * <p>
     * Also sets the ID's for the main pane (<i>chatRoom</i> + chat room ID); 
     * and adds the messages text area to the logger's list.
     * 
     * @param chatRoomID 
     *                  ID of the room for which this pane is being created.
     */
    private Pane newRoomPane (byte chatRoomID) {
        
        VBox pane = new VBox();
        
        pane.setAlignment(Pos.CENTER);
        
        /* Adds three main elements: 
            the message text area (where the messages will be displayed),
            the user input text area (where the user will type the message)
            and the bottom pane, with all the buttons for the conversation 
            (send, disconnect...).
        */
        TextFlow msgTextArea = new TextFlow();
        msgTextArea.setId("msgTextArea" + chatRoomID);
        
        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setOnKeyPressed((KeyEvent keyEvent) -> {
            
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                
                keyEvent.consume();
                send(userInput, chatRoomID);
            }
        });
        
        msgTextArea.setPrefHeight(200);
        userInput.setPrefHeight(msgTextArea.getPrefHeight() / 2);
        
        /* Pane with the buttons */
        HBox optionsPane = new HBox();
        /* Send button */
        Button sendButton = new Button();
        sendButton.setText(resourceBundle.getString("button_send"));
        
        sendButton.setOnAction(e -> {
            
            e.consume();
            send(userInput, chatRoomID);
        });
        /* Disconnect button */
        Button disconnectButton = new Button();
        disconnectButton.setText(resourceBundle.getString("button_disconnect"));
        /* Action for the disconnection button */
        disconnectButton.setOnAction(e -> {
            
            e.consume();
            PeerGUI.peer.leaveChatRoom(chatRoomID);
            
            /* Shows a little message and leaves the tab open */
            logger.logMsg("\n---------------------\n"
                        + "Disconnected from room."
                        + "\n---------------------\n", chatRoomID);
            
            /* Changes the state of the text areas */
            userInput.setEditable(false);
            userInput.setDisable(true);
        });
        
        /* Adds a scroll bar for the msgTextArea */
        ScrollPane msgScroll = new ScrollPane(msgTextArea);
        msgScroll.setPrefSize(msgTextArea.getPrefWidth(),
                              msgTextArea.getPrefHeight());
        msgScroll.setFitToWidth(true);
        
         /* Adds a listener so the scroll bar can go to the 
        bottom automatically */
        msgTextArea.heightProperty().addListener( observable -> {
            
            pane.layout();
            msgScroll.setVvalue(msgScroll.getVmax());
        });
        
        optionsPane.setAlignment(Pos.BASELINE_RIGHT);
        optionsPane.getChildren().addAll(disconnectButton, sendButton);
        
        /* Adds the elements to the main pane */
        pane.getChildren().addAll(msgScroll, userInput, optionsPane);
        
        logger.addTextArea(msgTextArea, 2);
        
        return pane;
    }
    
    
    /**
     * Sends the message to the rest of the peers on the same room.
     * 
     * @param userInput 
     *              TextArea where the message has been written.
     * 
     * @param chatRoom 
     *              ID of the chat room where the message will be sent.
     */
    private void send (TextArea userInput, byte chatRoom) {
        
        String message = (userInput.getText() == null)? "" : userInput.getText();
        
        if (!PeerGUI.peer.sendMessage(message, chatRoom).isEmpty()) {
            
            logger.logWarning("Some peers may have not received the message.\n");
        }
        
        logger.logMsg("You:\n\t" + message + "\n", chatRoom);
        
        userInput.setText(null);
    }
    
    /**
     * Changes the language of every component on the scene.
     */
    private void changeLanguage (Locale locale) {
        
        try {
            
            /* Loads the scene with the new language */
            Parent root = FXMLLoader.load(
                    getClass().getResource(Common.fxmlDocument),
                    ResourceBundle.getBundle(Common.resourceBundle, locale)
            );
            
            PeerGUI.stage.getScene().setRoot(root);
        
        } catch (IOException ex) {
            
            Logger.getLogger(PeerGUI.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Closes the window after disconnecting the peer and closing all the open
     * streams.
     */
    private void close () {
        
        if (PeerGUI.peer.close()) {
            
            logger.logWarning("Closed correctly.\n");
        } else {
            
            logger.logWarning("Closed with errors.\n");
        }
        
        PeerGUI.stage.close();
    }
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        resourceBundle = rb;

        /* Sets the handler when the application is closed */
        PeerGUI.stage.setOnCloseRequest(e -> {
            
            e.consume();
            close();
        });
        
        Common.connectionObserver.addViewController(this);
    }   
    
}

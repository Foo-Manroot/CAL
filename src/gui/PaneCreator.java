/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import common.Common;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import peer.Host;

import static common.Common.logger;
import peer.Peer;

/**
 * Factory methods to create certain panes, as the ones needed for the tab of
 * the chat room.
 */
public class PaneCreator {
    
    /**
     * Creates and returns a pane with all the elements for the new chat room 
     * tab.
     * 
     * <p>
     * Also sets the ID's for the main pane (<i>chatRoom</i> + chat room ID); 
     * and adds the messages text area to the logger's list.
     * 
     * @param chatRoomID 
     *              ID of the room for which this pane is being created.
     * 
     * @param resourceBundle 
     *              Resources to set the language of the components.
     * 
     * @param peer 
     *              The peer for which this pane is being created.
     * 
     * 
     * @return 
     *              A pane with all the necessary elements for the tab.
     */
    public static Pane roomsTabPane (byte chatRoomID,
                                     ResourceBundle resourceBundle,
                                     Peer peer) {
        
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
                send(userInput, chatRoomID, peer);
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
            send(userInput, chatRoomID, peer);
        });
        /* Disconnect button */
        Button disconnectButton = new Button();
        disconnectButton.setText(resourceBundle.getString("button_disconnect"));
        /* Action for the disconnection button */
        disconnectButton.setOnAction(e -> {
            
            e.consume();
            peer.leaveChatRoom(chatRoomID);
            
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
    private static void send (TextArea userInput, byte chatRoom, Peer peer) {
        
        String message = (userInput.getText() == null)? "" : userInput.getText();
        
        Host aux = new Host(Common.getInterfaces().get(0),
                            peer.getServer().getSocket().getLocalPort(),
                            chatRoom);
        
        logger.logMsg(message + "\r\n", aux, true);
        
        if (!peer.sendMessage(message, chatRoom).isEmpty()) {
            
            logger.logWarning("Some peers may have not received the message.\n");
        }
        
        userInput.setText(null);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.room;

import commands.Command;
import commands.Parser;
import common.Common;
import gui.main.FXMLPeerController;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import networking.NetUtils;
import peer.Host;
import peer.Peer;

/**
 *
 */
public class RoomPane {

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
     * @param peer
     *              The peer for which this pane is being created.
     *
     *
     * @return
     *              A pane with all the necessary elements for the tab.
     */
    public static Pane roomsTabPane(byte chatRoomID, Peer peer) {
        
        ResourceBundle aux = ResourceBundle.getBundle(Common.resourceBundle, FXMLPeerController.currentLocale);
        VBox chatPane = new VBox();
        chatPane.setAlignment(Pos.CENTER);
        TextFlow msgTextArea = new TextFlow();
        msgTextArea.setId("msgTextArea" + chatRoomID);
        TextArea userInput = userInputArea(chatRoomID, peer);
        msgTextArea.setPrefHeight(200);
        userInput.setPrefHeight(msgTextArea.getPrefHeight() / 2);
        HBox optionsPane = new HBox();
        Button sendButton = new Button();
        sendButton.setText(aux.getString("button_send"));
        sendButton.setId("button_send" + chatRoomID);
        sendButton.setOnAction((ActionEvent e) -> {
            e.consume();
            send(userInput, chatRoomID, peer);
        });
        Button disconnectButton = new Button();
        disconnectButton.setText(aux.getString("button_disconnect"));
        disconnectButton.setId("button_disconnect" + chatRoomID);
        disconnectButton.setOnAction((ActionEvent e) -> {
            e.consume();
            peer.leaveChatRoom(chatRoomID);
            Common.logger.logMsg("\n---------------------\n" + "Disconnected from room." + "\n---------------------\n", chatRoomID);
            userInput.setEditable(false);
            userInput.setDisable(true);
        });
        ScrollPane msgScroll = new ScrollPane(msgTextArea);
        msgScroll.setPrefSize(msgTextArea.getPrefWidth(), msgTextArea.getPrefHeight());
        msgScroll.setFitToWidth(true);
        msgTextArea.heightProperty().addListener((Observable observable) -> {
            chatPane.layout();
            msgScroll.setVvalue(msgScroll.getVmax());
        });
        optionsPane.setAlignment(Pos.BASELINE_RIGHT);
        optionsPane.getChildren().addAll(disconnectButton, sendButton);
        chatPane.getChildren().addAll(msgScroll, userInput, optionsPane);
        Common.logger.addTextArea(msgTextArea, 2);
        Common.langObserver.addNode(sendButton);
        Common.langObserver.addNode(disconnectButton);
        return chatPane;
    }

    /**
     * Creates the text area for the user input.
     *
     * <p>
     * This method also creates a handler for the user input, so different
     * actions (as executing commands) can be performed.
     */
    private static TextArea userInputArea(byte chatRoomID, Peer peer) {
        
        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setOnKeyPressed((KeyEvent keyEvent) -> {
            String command;
            switch (keyEvent.getCode()) {
                case TAB:
                    if (userInput.getText() == null) {
                        break;
                    }
                    if (userInput.getText().trim().startsWith(String.valueOf(Common.escapeChar))) {
                        keyEvent.consume();
                        if (userInput.getText().length() > 1) {
                            command = Common.parser.completeCommand(userInput.getText().trim().substring(1));
                            if (!command.equalsIgnoreCase(Command.UNKNOWN.name())) {
                                userInput.appendText(command);
                            }
                        }
                    }
                    break;
                case ENTER:
                    keyEvent.consume();
                    if (userInput.getText().equalsIgnoreCase(Common.escapeChar + Command.HOSTS.name() + " all")) {
                        Common.logger.logWarning("Command executed: " + Command.HOSTS.name() + " all\n");
                        Parser.showHostsList();
                        userInput.setText(null);
                        break;
                    }
                    if (Parser.isCommand(userInput.getText())) {
                        Common.parser.executeCommand(Command.getCommand(userInput.getText().trim().substring(1)));
                        userInput.setText(null);
                    } else {
                        if (userInput.getText().trim().startsWith(String.valueOf(Common.escapeChar))) {
                            Common.logger.logMsg("Unknown command.\n");
                            Common.logger.logError("Unknown command.\n");
                            userInput.setText(null);
                        } else {
                            send(userInput, chatRoomID, peer);
                        }
                    }
                    break;
            }
        });
        return userInput;
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
    private static void send(TextArea userInput, byte chatRoom, Peer peer) {
        
        String message = (userInput.getText() == null) ? "" : userInput.getText();
        Host aux = new Host(NetUtils.getInterfaces().get(0), peer.getServer().getSocket().getLocalPort(), chatRoom);
        Common.logger.logMsg(message + "\r\n", aux, true);
        if (!peer.sendMessage(message, chatRoom).isEmpty()) {
            Common.logger.logWarning("Some peers may have not " + "received the message.\n");
        }
        userInput.setText(null);
    }
    
}

/*
 * CAL.
 *  A P2P chat program that lets you communicate without any infrastructure.
 *
 *   Copyright (C) 2015  Foo-Manroot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui.room;


import static common.Common.logger;
import static common.Common.parser;
import static common.Common.resourceBundle;

import commands.Command;
import commands.Parser;
import common.Common;
import gui.main.FXMLPeerController;
import java.util.ResourceBundle;
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
    public static Pane roomsTabPane (byte chatRoomID,
                                     Peer peer) {

        ResourceBundle aux = ResourceBundle.getBundle(resourceBundle,
                                                      FXMLPeerController.currentLocale);
        VBox chatPane = new VBox(); /* Pane for the chat itself */
//        VBox roomOptionsPane = new VBox(); /* Pane for the extra options */
//        HBox mainPane = new HBox(); /* Pane to contain the others */

        chatPane.setAlignment(Pos.CENTER);

    /* Chat pane */
        /* Adds three main elements:
            the message text area (where the messages will be displayed),
            the user input text area (where the user will type the message)
            and the bottom pane, with all the buttons for the conversation
            (send, disconnect...).
        */
        TextFlow msgTextArea = new TextFlow();
        msgTextArea.setId("msgTextArea" + chatRoomID);

        TextArea userInput = userInputArea(chatRoomID, peer);

        msgTextArea.setPrefHeight(200);
        userInput.setPrefHeight(msgTextArea.getPrefHeight() / 2);

        /* Pane with the buttons */
        HBox optionsPane = new HBox();
        /* Send button */
        Button sendButton = new Button();

        sendButton.setText(aux.getString("button_send"));
        sendButton.setId("button_send" + chatRoomID);

        sendButton.setOnAction(e -> {

                e.consume();
                send(userInput, chatRoomID, peer);
            });

        /* Disconnect button */
        Button disconnectButton = new Button();

        disconnectButton.setText(aux.getString("button_disconnect"));
        disconnectButton.setId("button_disconnect" + chatRoomID);

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
        msgTextArea.heightProperty().addListener(observable -> {

                chatPane.layout();
                msgScroll.setVvalue(msgScroll.getVmax());
            });

        optionsPane.setAlignment(Pos.BASELINE_RIGHT);
        optionsPane.getChildren().addAll(disconnectButton, sendButton);

        /* Adds the elements to the pane */
        chatPane.getChildren().addAll(msgScroll, userInput, optionsPane);

        logger.addTextArea(msgTextArea, 2);

        /* Adds the necessary labeled nodes to the observer's list */
        Common.langObserver.addNode(sendButton);
        Common.langObserver.addNode(disconnectButton);
        
    /* Options pane */
        /* Adds a TextFlow to show the information about the connected hosts */
//        TextFlow connectedHosts = new TextFlow();

    /* Main pane */
        /* Adds all the secondary panes */

        return chatPane;
    }

    /**
     * Creates the text area for the user input.
     * 
     * <p>
     * This method also creates a handler for the user input, so different 
     * actions (as executing commands) can be performed.
     */
    private static TextArea userInputArea (byte chatRoomID, Peer peer) {

        TextArea userInput = new TextArea();
        userInput.setWrapText(true);

        userInput.setOnKeyPressed((KeyEvent keyEvent) -> {

            String command;

                switch (keyEvent.getCode()) {
                    /* If the pressed key is "tab", calls the autocompletion
                    method on the parser */
                    case TAB:

                        if (userInput.getText() == null) {

                            break;
                        }

                        /* Only if the text begins with the escape character, it
                        will be interpreted as a command. */
                        if (userInput.getText()
                                .trim().startsWith(
                                        String.valueOf(Common.escapeChar))
                            ) {
                            
                            /* Consumes the event, so a tab won't be added to
                            the text */
                            keyEvent.consume();

                            if (userInput.getText().length() > 1) {

                                command = parser.completeCommand(
                                                    userInput.getText()
                                                             .trim()
                                                             .substring(1));

                                if (!command.equalsIgnoreCase(
                                                    Command.UNKNOWN.name())
                                    ) {

                                    /* Completes the command */
                                    userInput.appendText(command);
                                }
                            }
                        }
                        break;

                    /* If the pressed key is "enter", sends the message */
                    case ENTER:

                        keyEvent.consume();

                        /* Special case: if the command is "HOSTS" and has 
                        one argument ("all"), executes it directly */
                        if (userInput.getText().equalsIgnoreCase(
                                Common.escapeChar
                                + Command.HOSTS.name()
                                + " all")
                            ) {
                            
                            logger.logWarning("Command executed: "
                                                + Command.HOSTS.name()
                                                + " all\n");
                            
                            Parser.showHostsList();
                            userInput.setText(null);
                            
                            break;
                        }
                        
                        /* If the input was a command, tries to execute it */
                        if (Parser.isCommand (userInput.getText())) {

                            /* Executes the given command */
                            parser.executeCommand(Command.getCommand(
                                                        userInput.getText()
                                                                 .trim()
                                                                 .substring(1))
                                                  );
                            userInput.setText(null);
                        } else {

                            if (userInput.getText().trim().startsWith(
                                        String.valueOf(Common.escapeChar))
                                ) {

                                logger.logMsg("Unknown command.\n");
                                logger.logError("Unknown command.\n");
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
    private static void send (TextArea userInput, byte chatRoom, Peer peer) {

        String message = (userInput.getText() == null)?
                                    ""
                                  : userInput.getText();

        Host aux = new Host(NetUtils.getInterfaces().get(0),
                            peer.getServer().getSocket().getLocalPort(),
                            chatRoom);

        logger.logMsg(message + "\r\n", aux, true);

        if (!peer.sendMessage(message, chatRoom).isEmpty()) {

            logger.logWarning("Some peers may have not "
                            + "received the message.\n");
        }

        userInput.setText(null);
    }
    
}

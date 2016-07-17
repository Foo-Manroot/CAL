/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import static common.Common.logger;
import static common.Common.parser;

import commands.Command;
import commands.Parser;
import common.Common;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import peer.Host;
import peer.Peer;

/**
 * Factory methods to create certain panes, as the ones needed for the tab of
 * the chat room.
 */
public class PaneCreator {
    
    /**
     * Generates and adds the text to the given TextFlow. Also, adds the 
     * necessary items, like a context menu.
     * 
     * @param outArea
     *              Text area where the new text will be added.
     * 
     * @param host
     *              Host that sent the message.
     * 
     * @param msg
     *              The received message.
     * 
     * @param hostName
     *              The text area where the host's name will be shown.
     */
    public static void genText (TextFlow outArea,
                                Host host,
                                String msg,
                                Text hostName) {
        
        /* Notifies the GUI thread to add the text */
        Platform.runLater(() -> {

            Color colour;
        
            /* Adds a context menu so a new connection can be 
            done with the selected host */
            MenuItem connectMenu = new MenuItem(
                    ResourceBundle.getBundle(Common.resourceBundle)
                            .getString("private_conv_menu")
                    );

            connectMenu.setOnAction(e -> {

                    Alert alert = new Alert (Alert.AlertType.ERROR);
                    String text = ResourceBundle
                                    .getBundle(Common.resourceBundle)
                                    .getString("error_private_conv");

                    /* Starts a new conversation */
                    if (!PeerGUI.peer.startConversation(host)) {

                        logger.logError(text);
                        alert.setContentText(text);
                        alert.show();
                    }
                });
            
            /* Adds another menu item to set the host alias */
            MenuItem aliasMenu = new MenuItem(
                    ResourceBundle.getBundle(Common.resourceBundle)
                            .getString("chng_alias_menu")
                    );

            aliasMenu.setOnAction(e -> {

                    Optional answer;
                    TextInputDialog dialog = new TextInputDialog();
                    String text = ResourceBundle
                                    .getBundle(Common.resourceBundle)
                                    .getString("chng_alias_menu");

                    dialog.setContentText(text);

                    answer = dialog.showAndWait();

                    /* Gets the new value and adds it to the list (only if any 
                    value has been submitted) */
                    if (answer.isPresent() &&
                        !((String) answer.get()).isEmpty()
                        ) {

                        logger.setHostAlias(host, (String) answer.get());

                        hostName.setText((String) answer.get() + ":\n\t");
                    }
                });
            
            ContextMenu context = (Common.isLocalPeer(host))? 
                                        new ContextMenu(aliasMenu) 
                                      : new ContextMenu(aliasMenu, connectMenu);
            Text text;

            colour = logger.getColour(host);

            /* Adds the host name (if needed) and its context menu */
            if (!hostName.getText().isEmpty()) {
            
                hostName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

                    if (e.getButton() == MouseButton.SECONDARY) {

                        context.show(hostName, e.getScreenX(), e.getScreenY());
                    }
                });
                
                hostName.setFill(colour);
                outArea.getChildren().add(hostName);
            }
            
            /* Adds the message */
            text = new Text(msg);

            text.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

                if (e.getButton() == MouseButton.SECONDARY) {

                    context.show(text, e.getScreenX(), e.getScreenY());
                }
            });

            text.setFill(colour);

            outArea.getChildren().add(text);
        });
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
        
        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setOnKeyPressed((KeyEvent keyEvent) -> {
            
            String command;

            switch (keyEvent.getCode()) {
                /* If the pressed key is "tab", calls the autocompletion method
                on the parser */
                case TAB:

                    /* Only if the text begins with the escape character, it 
                    will be interpreted as a command. */
                    if (userInput.getText()
                            .trim().startsWith(
                                    String.valueOf(Common.escapeChar))
                        ) {

                        /* Consumes the event, so a tab won't be added to the 
                        text */
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
                    
                    /* If the input was a command, tries to execute it */
                    if (Parser.isCommand(userInput.getText())) {
                        
                        /* Executes the given command */
                        parser.executeCommand(Command.getCommand(
                                                    userInput.getText()
                                                             .trim()
                                                             .substring(1)));
                    } else {
                        
                        if (userInput.getText().trim().startsWith(
                                    String.valueOf(Common.escapeChar))
                            ) {
                                
                            logger.logMsg("Unknown command.\n");
                            logger.logError("Unknown command.\n");
                        } else {
                            
                            send(userInput, chatRoomID, peer);
                        }
                    }
                break;
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
        msgTextArea.heightProperty().addListener(observable -> {
                chatPane.layout();
                msgScroll.setVvalue(msgScroll.getVmax());
            });
        
        optionsPane.setAlignment(Pos.BASELINE_RIGHT);
        optionsPane.getChildren().addAll(disconnectButton, sendButton);
        
        /* Adds the elements to the pane */
        chatPane.getChildren().addAll(msgScroll, userInput, optionsPane);
        
        logger.addTextArea(msgTextArea, 2);
        
    /* Options pane */
        /* Adds a TextFlow to show the information about the connected hosts */
//        TextFlow connectedHosts = new TextFlow();
        
    /* Main pane */
        /* Adds all the secondary panes */
        
        return chatPane;
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

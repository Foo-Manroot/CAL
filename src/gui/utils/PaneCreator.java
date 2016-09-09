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
package gui.utils;

import gui.main.PeerGUI;
import static common.Common.logger;
import static common.Common.resourceBundle;
import static gui.main.FXMLPeerController.currentLocale;

import common.Common;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import peer.Host;

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
            MenuItem connectMenu = new MenuItem();

            connectMenu.setOnAction(e -> {

                    ResourceBundle aux = ResourceBundle.getBundle(resourceBundle,
                                                                  currentLocale);
                    Alert alert = new Alert (Alert.AlertType.ERROR);
                    String text = aux.getString("error_private_conv");

                    /* Starts a new conversation */
                    if (!PeerGUI.peer.startConversation(host)) {

                        logger.logError(text);
                        alert.setContentText(text);
                        alert.show();
                    }
                });

            /* Adds another menu item to set the host alias */
            MenuItem aliasMenu = new MenuItem();

            aliasMenu.setOnAction(e -> {

                    ResourceBundle aux = ResourceBundle.getBundle(resourceBundle,
                                                                  currentLocale);
                    Optional answer;
                    TextInputDialog dialog = new TextInputDialog();
                    String text = aux.getString("chng_alias_menu");

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

                    ResourceBundle aux;

                    if (e.getButton() == MouseButton.SECONDARY) {

                        aux = ResourceBundle.getBundle(resourceBundle,
                                                       currentLocale);

                        context.show(hostName, e.getScreenX(), e.getScreenY());

                        connectMenu.setText(aux.getString("private_conv_menu"));
                        aliasMenu.setText(aux.getString("chng_alias_menu"));
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
}

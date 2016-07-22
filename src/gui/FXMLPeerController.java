/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import static gui.PeerGUI.peer;
import static common.Common.logger;

import common.Common;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import peer.Host;
import java.io.File;
import java.net.InetAddress;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

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

    /**
     * Current locale of the text.
     */
    public static Locale currentLocale;

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

        if (peer.checkConnection()) {

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

            if (peer.connect(host)) {

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

    /**
     * Saves the information about the known hosts on a file.
     */
    @FXML
    private void saveHosts () {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(resourceBundle.getString("ask_file_path"));
        fileChooser.setInitialFileName(Common.FILE_PATH);

        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("Data Files", "*.dat"),
                new ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showSaveDialog(roomsTabPane.getScene().getWindow());

        if (selectedFile != null) {

            System.out.println(selectedFile.toString());
        }

        if (selectedFile != null) {

            if (peer.getHostsList().writeFile (selectedFile.getPath(), true)) {

                logger.logWarning("File stored successfully.\n");
            } else {

                logger.logError("Error storing the file.\n");
            }
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

                if (peer.leaveChatRoom(chatRoomID)) {

                    logger.logWarning("Disconnected correctly.\n");
                } else {

                    logger.logWarning("Some peers may have not received the "
                                      + "disconnection message.\n");
                }
            });
        
        newTab.setOnSelectionChanged(e -> {
        
                /* When this tab is selected, sets the variable "currentRoom" */
                if (newTab.isSelected()) {

                    Common.currentRoom = chatRoomID;
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
        Common.currentRoom = chatRoomID;
        
        /* Adds a new alias for the local peer */
        for (InetAddress addr : Common.getInterfaces()) {

            logger.setHostAlias(new Host (addr,
                                          peer.getServer().getSocket().getLocalPort(),
                                          chatRoomID)
                            , "You");
        }
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

        return PaneCreator.roomsTabPane(chatRoomID, peer);
    }

    /**
     * Changes the language of every component on the scene.
     */
    private void changeLanguage (Locale locale) {

        /* Stores the open tabs and the selected tab */
        ObservableList<Tab> openTabs = roomsTabPane.getTabs();
        int selectedIndex = roomsTabPane.getSelectionModel().getSelectedIndex();
        TabPane aux;

        try {

            /* Loads the scene with the new language */
            Parent root = FXMLLoader.load(
                    getClass().getResource(Common.fxmlDocument),
                    ResourceBundle.getBundle(Common.resourceBundle, locale)
            );

            PeerGUI.stage.getScene().setRoot(root);

            for (Node n : root.getChildrenUnmodifiable()) {

                if ((n.getId() != null) && (n.getId().equals("roomsTabPane"))) {

                    aux = (TabPane) n;

                    /* Restores the open tabs */
                    for (Tab t : openTabs) {

                        /* Only adds the conversation tabs (the only tabs that
                        aren't closable are the initial and the '+' ones) */
                        if (t.isClosable()) {

                            aux.getTabs().add(aux.getTabs().size() - 1, t);
                        }
                    }

                    /* Sets the previously selected tab */
                    aux.getSelectionModel().select(selectedIndex);
                }
            }

            /* Notifies the observer */
            Common.langObserver.languageChanged(locale);

            /* Updates the current locale */
            currentLocale = locale;

        } catch (IOException ex) {

            logger.logError("IOException at FXMLPeerController.changeLanguage():"
                            + ex.getMessage() + "\n");
        }
    }

    /**
     * Closes the window after disconnecting the peer and closing all the open
     * streams.
     */
    private void close () {

        if (peer.close()) {

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

        currentLocale = resourceBundle.getLocale();
    }

}

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
package gui.files;

import common.Common;
import gui.main.FXMLPeerController;
import gui.main.PeerGUI;
import java.io.File;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *  Methods to show the graphical interface involved on the file sharing.
 */
public class FileShareGUI {

    /**
     * Return value for {@code showConfirmDialog()}.
     */
    private static boolean retVal;


/* --------------------------------------- */
/* ---- END OF ATTRIBUTES DECLARATION ---- */
/* --------------------------------------- */

    /**
     * Confirmation dialog to accept a file interchange.
     *
     * @param resources
     *              Resource bundle with the strings to put on this dialog.
     *
     * @param fileProps
     *              Information of the file.
     *
     *
     * @return
     *              <i>true</i> if the "Accept" button has been pressed,
     *          <i>false</i> otherwise.
     */
    public static boolean showConfirmationDialog (ResourceBundle resources,
                                                  String fileProps) {

        retVal = true;
        GridPane layout = new GridPane();
        layout.setMinSize(365, 191);

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle (resources.getString("accept_file_dialog_title"));

        /* Label for the confirmation text and the file information */
        Label confirmLabel = new Label (resources.getString("accept_file_dialog"));
        Label fileInfo = new Label (fileProps);

        /* "Accept" and "cancel" buttons */
        Button acceptButton = new Button(resources.getString("accept_button"));
        Button cancelButton = new Button(resources.getString("cancel_button"));

        /* Sets the action on each button */
        acceptButton.setOnAction (e -> {

            retVal = true;
            stage.close();
        });

        cancelButton.setOnAction(e -> {

            /* Sets the attribute to false and closes the window */
            retVal = false;
            stage.close();
        });

        stage.setOnCloseRequest(e -> {

            e.consume();
            /* Sets the attribute to false and closes the window */
            retVal = false;
            stage.close();
        });

        /* Adds everything to the layout and shows the window */
        layout.addRow(0, confirmLabel);
        layout.addRow(1, fileInfo);
        layout.addRow(2, acceptButton, cancelButton);

        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);

        stage.setScene(scene);
        stage.showAndWait();

        return retVal;
    }

    /**
     * Shows a dialog to select a file.
     *
     * @return
     *              The selected {@link File}, or {@code null} if none has been
     *          selected.
     */
    public static File selectFile () {

        ResourceBundle resourceBundle;
        FileChooser fileChooser = new FileChooser();

        resourceBundle = ResourceBundle.getBundle(Common.resourceBundle,
                                                  FXMLPeerController.currentLocale);

        fileChooser.setTitle(resourceBundle.getString("ask_file_path"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Data Files", "*.dat"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg",
                                                               "*.bmp",
                                                               "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(PeerGUI.stage
                                                              .getScene()
                                                              .getWindow());

        return selectedFile;
    }

    /**
     * Shows a dialog to select a path to store a file.
     *
     * @param initialFileName
     *              String with the initial name of the file. It can
     *          be {@code null}.
     *
     * @return
     *              The selected {@link File}, or {@code null} if none has been
     *          selected.
     */
    public static String selectSavePath (String initialFileName) {

        ResourceBundle resourceBundle;
        FileChooser fileChooser = new FileChooser();

        resourceBundle = ResourceBundle.getBundle(Common.resourceBundle,
                                                  FXMLPeerController.currentLocale);

        fileChooser.setTitle(resourceBundle.getString("ask_file_path"));

        if (initialFileName != null) {

            fileChooser.setInitialFileName (initialFileName);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Data Files", "*.dat"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg",
                                                               "*.bmp",
                                                               "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selectedFile = fileChooser.showSaveDialog(PeerGUI.stage
                                                              .getScene()
                                                              .getWindow());

        return selectedFile.getAbsolutePath();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import java.io.File;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *  Methods to show the graphical interface involved on the file sharing.
 */
public class FileShareGUI {
    
    private static boolean retVal;
    
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
     * Generates a string with the information of the file.
     * 
     * @param file 
     *              The file whose information is going to be returned.
     * 
     * 
     * @return 
     *              A string with the information of the file.
     */
    public static String genFileInfo (File file) {
        
        return "\nFile information: \n"
                + "\tName: " + file.getName() + "\n"
                + "\tSize: " + file.length() + " Bytes\n";
    }
}

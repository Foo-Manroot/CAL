/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import common.Common;
import static common.Common.logger;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import peer.Peer;

/**
 *  Graphic application for the peer.
 */
public class PeerGUI extends Application {
    
    /**
     * Main stage of this applications.
     */
    public static Stage stage;
    
    /**
     * Peer that will be controlled with this GUI application. It's initialised
     * on the first available port.
     */
    public static Peer peer;
    
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    @Override
    public void start(Stage stage) throws Exception {
//        int i = 0;
        PeerGUI.stage = stage;
        PeerGUI.peer = new Peer();
        
        /* Loads everything needed and sets the Caspian look and feel */
        loadView (new Locale ("en", "UK"));
        setUserAgentStylesheet(STYLESHEET_CASPIAN);       
        
        stage.setTitle("Peer listening on port " + peer.getServer().getPort());
        
        stage.show();
//        TextFlow textFlow = new TextFlow();
//        
//        ScrollPane scroll = new ScrollPane(textFlow);
//        
//        scroll.setPrefSize(300, 200);  
//        scroll.setFitToWidth(true);
//        
//        stage.setScene(new Scene (scroll));
//        stage.show();
//        HashSet <String> colours = new HashSet<>();
//        
//        for (i = 0; i < 1000; i++) {
//            
//            Color colour = common.Logger.chooseColour();
//
//            Text text = new Text(colour.toString() + "\t");
//            text.setFill(colour);
//            
//            if (colours.contains(colour.toString())) {
//                
//                System.out.println("Repeated colour: " + colour.toString());
//            }
//            
//            colours.add(colour.toString());
//            
//            textFlow.getChildren().add(text);
//        }
//        
//        System.out.println("Total repeated colours: " + (i - colours.size()));
    }
    
    /**
     * Loads the view with the given locale.
     */
    private void loadView (Locale locale) {
        
        ScrollPane scroll;
        TitledPane titled;
        
        try {
            
            Parent root = FXMLLoader.load(
                    getClass().getResource(Common.fxmlDocument),
                    ResourceBundle.getBundle(Common.resourceBundle, locale)
            );
            
            Scene scene = new Scene (root);
            
            stage.setScene(scene);
            
            /* Adds the text areas to the logger. The text areas are inside a 
            titled pane that's inside a scroll pane */
            for (Node n : root.getChildrenUnmodifiable()) {
                
                if (n.getId() == null) {
                    
                    continue;
                }
                
                switch (n.getId()) {
                    
                    case "warningScrollPane":
                        scroll = (ScrollPane) n;
                        
                        /* Its content is the titled pane */
                        titled = (TitledPane) scroll.getContent();
                        
                        /* The child of the titled pane is the text flow */
                        logger.addTextArea((TextFlow) titled.getContent(), 5);
                        break;
                        
                    case "errorsScrollPane":
                        scroll = (ScrollPane) n;
                        
                        /* Its content is the titled pane */
                        titled = (TitledPane) scroll.getContent();
                        
                        /* The child of the titled pane is the text flow */
                        logger.addTextArea((TextFlow) titled.getContent(), 3);
                        break;
                }
            }
        
        } catch (IOException ex) {
            
            Logger.getLogger(PeerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

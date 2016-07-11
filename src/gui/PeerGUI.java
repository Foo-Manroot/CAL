/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import common.Common;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import peer.Peer;


import static common.Common.logger;

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
        
        PeerGUI.stage = stage;
        PeerGUI.peer = new Peer();
        
        /* Loads everything needed and sets the Caspian look and feel */
        loadView (new Locale ("en", "UK"));
        setUserAgentStylesheet(STYLESHEET_CASPIAN);       
        
        stage.setTitle("Peer listening on port " + peer.getServer().getPort());
        
        stage.show();
    }
    
    /**
     * Loads the view with the given locale.
     */
    private void loadView (Locale locale) {
        
        ScrollPane scroll;
        TitledPane titled;
        TextFlow text;
        
        try {
            
            Parent root = FXMLLoader.load(
                    getClass().getResource(Common.fxmlDocument),
                    ResourceBundle.getBundle(Common.resourceBundle, locale)
            );
            
            Scene scene = new Scene (root);
            
            stage.setScene(scene);
            
            /* Adds the text areas to the logger. The text areas are inside a 
            scroll pane that's inside a titled pane */
            for (Node n : root.getChildrenUnmodifiable()) {
                
                if (n.getId() == null) {
                    
                    continue;
                }
                
                switch (n.getId()) {
                    
                    case "warningsTitledPane":
                        titled = (TitledPane) n;
                        
                        /* Its content is the titled pane */
                        scroll = (ScrollPane) titled.getContent();
                        
                        /* The child of the titled pane is the text flow */
                        text = (TextFlow) scroll.getContent();
                        
                        /* Adds a listener so the scroll bar can go to the 
                        bottom automatically */
                        text.heightProperty().addListener( observable -> {

                            TitledPane auxTitled = (TitledPane) n;
                            ScrollPane auxScroll = (ScrollPane) auxTitled.getContent();
                            
                            auxTitled.layout();
                            
                            auxScroll.setVvalue(auxScroll.getVmax());

                        });
                        
                        logger.addTextArea(text, 5);
                        break;
                        
                    case "errorsTitledPane":
                        titled = (TitledPane) n;
                        
                        /* Its content is the titled pane */
                        scroll = (ScrollPane) titled.getContent();
                        
                        /* The child of the titled pane is the text flow */
                        text = (TextFlow) scroll.getContent();
                        
                        /* Adds a listener so the scroll bar can go to the 
                        bottom automatically */
                        text.heightProperty().addListener( observable -> {

                            TitledPane auxTitled = (TitledPane) n;
                            ScrollPane auxScroll = (ScrollPane) auxTitled.getContent();
                            
                            auxTitled.layout();
                            
                            auxScroll.setVvalue(auxScroll.getVmax());

                        });
                        
                        logger.addTextArea(text, 3);
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

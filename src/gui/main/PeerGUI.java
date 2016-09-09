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
package gui.main;

import static common.Common.logger;

import common.Common;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
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
            
            logger.logError("IOException at PeerGUI.loadView(): "
                            + ex.getMessage());
        }        
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        launch(args);
    }
    
}

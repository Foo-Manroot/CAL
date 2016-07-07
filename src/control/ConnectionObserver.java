/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import gui.FXMLPeerController;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.application.Platform;

/**
 * Observer to control the connections. This class serves as a union between 
 * the GUI application and the rest of the program.
 */
public class ConnectionObserver {
    
    /**
     * List with all the observed {@link FXMLPeerController}.
     */
    private final ConcurrentLinkedQueue<FXMLPeerController> guiControllers;
    
    
    /**
     * Constructor.
     */
    public ConnectionObserver () {
        
        guiControllers = new ConcurrentLinkedQueue<>();
    }
    
    /**
     *  This method notifies the observer that a new connection has been 
     * established and all the {@link FXMLPeerController} elements on the list
     * will be warned.
     * 
     * @param dataFlow 
     *              The data flow where the connection has been accepted.
     */
    public void connectionAccepted (byte dataFlow) {
        
        for (FXMLPeerController aux : guiControllers) {
            
            /* Notifies the controller so it can add a new tab when possible */
            Platform.runLater(() -> {
                aux.addTab(dataFlow);
            });
        }
    }
    
//    /**
//     *  This method notifies the observer that some connection has been 
//     * finished and all the {@link FXMLPeerController} elements on the list
//     * will be warned.
//     * 
//     * @param dataFlow 
//     *              The data flow where the connection has been accepted.
//     */
//    public void disconnectionReceived (byte dataFlow) {
//        
//        
//    }
    
    /**
     * Adds the given controller to the list.
     * 
     * @param controller
     *              Element to be added.
     */
    public void addViewController (FXMLPeerController controller) {
        
        if (!guiControllers.contains(controller)) {
            
            guiControllers.add(controller);
        }
    }
}

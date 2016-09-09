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
package control;

import gui.main.FXMLPeerController;
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
    
    /**
     * Returns the list with all the {@link FXMLPeerController}.
     * 
     * @return 
     */
    public ConcurrentLinkedQueue<FXMLPeerController> getViewControllers () {
        
        return guiControllers;
    }
}

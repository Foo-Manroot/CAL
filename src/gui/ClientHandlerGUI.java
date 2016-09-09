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
package gui;

import javax.swing.JTextArea;
import peer.ClientHandler;
import peer.Peer;

/**
 * Implementation of the class {@link ClientHandler} to handle the user input
 * on a graphic interface.
 * 
 * <p>
 * As the main processes on the peer doesn't create anything to handle the user
 * input, this class (or some of its implementations, as
 * {@link ClientHandlerGUI}) must be created separately and added by calling the
 * method {@code Peer.addClient()}.
 * 
 * <p>
 * Unlike {@code ClientHandler}, this handler controls all the possible data 
 * flows, so no more than one instance per <i>GUI</i> is needed.
 */
public class ClientHandlerGUI extends ClientHandler {
    
    /**
     * Only one instance of this class is allowed
     */
    private static ClientHandlerGUI instance;

/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */

    /**
     * Constructor.
     * 
     * 
     * @param peer 
     *              Peer at which this client-side handler belongs.
     * 
     * @param dataFlow 
     *              Data flow that this client will control.
     */
    private ClientHandlerGUI (Peer peer, byte dataFlow) {
        
        super(peer, dataFlow);
                            
    }
    
    /**
     * Initialises the only allowed instance of this object and returns it.
     * If an instance already existed, returns it.
     * 
     * 
     * @param peer 
     *              Peer at which this client-side handler belongs.
     * 
     * 
     * @return 
     *              The only allowed instance.
     */
    public static ClientHandlerGUI newHandler (Peer peer) {
        
        if (instance == null) {
            
            instance = new ClientHandlerGUI(peer,
                                            common.Common.RESERVED_DATA_FLOW);
            return instance;
        }
        
        return instance;
    }
    
    /**
     * Does nothing. On this implementation, the correct method to invoke to 
     * handle user input is <b><i>ClientHandlerGUI.handleInput()</i></b>
     */
    @Override
    public void run() {}
    
    /**
     * Gets the user input and process it to perform the right task (send a 
     * message, execute some commands...).
     * 
     * 
     * @param inputArea 
     *              JTextArea where the user introduced the text.
     * 
     * @param dataFlow 
     *              Data flow where this input has been made.
     */
    public void handleInput (JTextArea inputArea, byte dataFlow) {
        
        /* Gets the text and tells the peer to create a plain message and send
        it to all the hosts on the given data flow (chat room) */
        String msg = inputArea.getText();
        
        /* If Peer.sendMessage() returns an empty list, all the hosts received
        the message and sent an ACK back. If not, some of them didn't 
        answered back. */
        if (!getPeer().sendMessage(msg, dataFlow).isEmpty()) {
            
            common.Common.logger.logWarning("Some peers may have not received "
                                            + "the message.\n");
        }
        
        /* Deletes the text on the input area */
        inputArea.setText(null);
    }
}

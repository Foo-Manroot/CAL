package peer;

import static common.Common.logger;

import control.ControlMessage;
import control.Notification;
import control.PacketCreator;
import gui.ClientHandlerGUI;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class created to handle the user input.
 * 
 * <p>
 * As the main processes on the peer doesn't create anything to handle the user
 * input, this class (or some of its implementations, as
 * {@link ClientHandlerGUI}) must be created separately and added by calling the
 * method {@code Peer.addClient()}, or directly added with 
 * {@code Peer.createClient()}.
 * 
 * <p>
 * Even though this class can be overridden, it implements the necessary methods
 * to handle some basic user input on .
 */
public class ClientHandler extends Thread {
        
    /**
     * Peer at which this client-side handler belongs.
     */
    private final Peer peer;

    /**
     * Data flow that this client will control.
     */
    private final byte dataFlow;

    /**
     * This attribute, if <i>true</i> tells the thread to end its execution.
     */
    private final AtomicBoolean end;

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
    protected ClientHandler (Peer peer, byte dataFlow) {

        this.peer = peer;
        this.dataFlow = dataFlow;
        this.end = new AtomicBoolean(false);
    }

    /**
     * Asks for a user input and sends it to the rest of the peers on this 
     * conversation.
     */
    @Override
    public void run() {

        Scanner userInput = new Scanner (System.in);
        String message;

        ArrayList<Host> hosts;

        DatagramPacket packet;
        Notification expectedAnswer;

        /* Infinite loop to get user input and send it to the rest of the 
        peers */
        while (!end.get()) {

            /* Waits until the user enters something */
            message = userInput.nextLine();

            if (end.get()) {

                break;
            }

            /* Updates the hosts list */
            hosts = peer.getHostsList().search(dataFlow);

            /* Sends the message to the rest of the peers on the current
            conversation (4 tries until giving up) */
            for (Host h : hosts) {

                packet = PacketCreator.PLAIN(dataFlow,
                                             message.getBytes(),
                                             peer.getServer().getPort());

                expectedAnswer = new Notification(h.getIPaddress(),
                                                 h.getDataFlow(),
                                                 ControlMessage.ACK);

                if (!h.send(packet, expectedAnswer, peer, 4)) {

                    logger.logError("Error trying to send the message \"" + 
                                    message + "\" to:" + 
                                    "\n" + h.toString());
                }
                
                /* Removes the notification from the list */
                peer.getServer().removeNotification(expectedAnswer);
            }
        }
    }

    /**
     * Sets the variable {@code end} to <i>true</i> so this thread can end
     * its execution.
     */
    public void endExecution () {

        end.compareAndSet(false, true);
    }

/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */

    /**
     * Returns the data flow that this client is handling.
     * 
     * @return 
     *              The value of {@code dataFlow}.
     */
    public byte getDataFlow () {

        return dataFlow;
    }
    
    /**
     * Returns <i>true</i> if the execution of this thread has been marked as
     * it should end.
     * 
     * @return 
     *              The value of {@code end}.
     */
    public boolean hasEnded () {

        return end.get();
    }
    
    /**
     * Returns the peer that created this client.
     * 
     * @return 
     *              The value of {@code peer}.
     */
    public Peer getPeer () {
        
        return peer;
    }
}

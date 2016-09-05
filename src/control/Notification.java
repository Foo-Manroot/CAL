package control;

import common.Common;
import packets.ControlMessage;
import packets.PacketChecker;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;

/**
 * When the client-side of a peer sends any message and must wait for a 
 * response to come back, it creates a notification to the server-side so it 
 * waits for the response and returns it to the client-side.
 */
public class Notification {

    /**
     * In the argument array, indicates that this notification indicates an 
     * expected {@code PLAIN} continuation.
     */
    public static final byte CONT_PLAIN = 1;
    
    /**
     * In the argument array, indicates that this notification indicates an 
     * expected {@code DATA} continuation.
     */
    public static final byte CONT_DATA = 2;
    
/* ---------------------------------- */
/* ---- END OF STATIC ATTRIBUTES ---- */
/* ---------------------------------- */
    
    /**
     * If this attribute is <i>false</i>, it means that the message that this 
     * notification refers to hasn't been received yet. If it has been received,
     * this attribute must be set to <i>true</i>.
     */
    private boolean received = false;
    
    /**
     * Date when this notification was created.
     */
    private final Date creationDate = new Date();
    
    /**
     * This is the response that must come from the specified address.
     */
    private final ControlMessage message;
    
    /**
     * Internet address where the answer message must come from.
     */
    private final InetAddress sourceIP;
    
    /**
     * Port number that can be sent with the message. If no port number is 
     * expected, this value is {@code -1}.
     */
    private int sourcePort;
    
    /**
     * Data flow at which the answer message must belong.
     */
    private final byte sourceDataFlow;
    
    /**
     * Expected value of the arguments, if any.
     */
    private final byte [] args;
        
/* --------------------------------------- */
/* ---- END OF ATTRIBUTES DECLARATION ---- */
/* --------------------------------------- */
    
    /**
     * Constructor.
     * 
     * @param sourceIP 
     *              Internet address where the answer message must come from.
     * 
     * @param sourceDataFlow 
     *              Data flow at which the answer message must belong.
     * 
     * @param message 
     *              {@link ControlMessage} expected as the answer coming from 
     *          the specified address and data flow.
     */
    public Notification (InetAddress sourceIP,
                         byte sourceDataFlow,
                         ControlMessage message) {
        
        this.sourceIP = sourceIP;
        this.sourceDataFlow = sourceDataFlow;
        this.message = message;
        args = null;
        sourcePort = -1;
    }
    
    /**
     * Constructor.
     * 
     * @param sourceIP 
     *              Internet address where the answer message must come from.
     * 
     * @param sourceDataFlow 
     *              Data flow at which the answer message must belong.
     * 
     * @param message 
     *              {@link ControlMessage} expected as the answer coming from 
     *          the specified address and data flow.
     * 
     * @param args
     *              A byte array with the expected value of the arguments.
     */
    public Notification (InetAddress sourceIP,
                         byte sourceDataFlow,
                         ControlMessage message,
                         byte [] args) {
        
        this.sourceIP = sourceIP;
        this.sourceDataFlow = sourceDataFlow;
        this.message = message;
        this.args = args;
        sourcePort = -1;
    }
    
    /**
     * Checks the given packet and returns <i>true</i> if it is the one that
     * corresponds to this notification. 
     * 
     * <p>
     * Also, if the packet is the correct one, sets the attribute 
     * {@code received} to <i>true</i>.
     * 
     * @param packet 
     *              Packet to be checked.
     * 
     * @return 
     *              <i>true</i> if it's the answer corresponding to this
     *          notification, or <i>false</i> if it isn't.
     */
    public boolean checkPacket (DatagramPacket packet) {
        
        byte [] data,
                portAux = new byte [4];
        int port = -1;
                
        if (packet == null ||
            packet.getAddress() == null || 
            packet.getData() == null) {
            
            return false;
        }
        
        /* Gets the data of the packet */
        data = new byte [packet.getLength()];
        System.arraycopy(packet.getData(), 0,
                         data, 0,
                         packet.getLength());
        
        /* If a port number is expected, gets it (if it's possible) */
        if ( (sourcePort > 0) &&
             (data.length >= (message.getLength() + portAux.length)) ) {
            
            System.arraycopy (data, message.getLength(),
                              portAux, 0,
                              portAux.length);
            
            port = Common.arrayToInt(portAux);
        }        
        
        return (received = (
                            PacketChecker.checkPacket(data).equals(message) &&
                            packet.getAddress().equals(sourceIP) &&
                            (data[1] == sourceDataFlow) &&
                            (port == sourcePort)
                            )
                );
    }
    
    /**
     * Returns a string representation of this object. The returned string will
     * be formatted this way:
     * <pre>
     * Message: ({@link ControlMessage})
     *      Creation date: (creation date)
     *      Source IP: (source address)
     *      Source data flow: (source data flow)
     *      Received: (true/false)
     *      Expected args.: (args)/No args.expected.
     * </pre>
     * 
     * @return 
     *              A string with all the values that this object has.
     */
    @Override
    public String toString () {
        
        StringBuilder msg = new StringBuilder(
               "Message: " + message
             + "\n\tCreation date: " + creationDate
             + "\n\tSource IP: " + sourceIP
             + "\n\tSource data flow: " + sourceDataFlow
             + "\n\tReceived: " + received);
        
        if (hasArgs()) {
            
            msg.append("\n\tExpected args.: ").append(args);
        } else {
            
            msg.append("\n\tNo args. expected.");
        }
        
        msg.append("\n");
        
        return new String(msg);
    }
    
/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */
    
    /**
     * Returns the date when this notification was created.
     *
     * @return
     *              the value of {@code creationDate}.
     */
    public Date getCreationDate () {
        
        return creationDate;
    }

    /**
     * Returns <i>true</i> if the message has been received. If not, returns 
     * <i>false</i>.
     *
     * @return
     *              the value of {@code received}.
     */
    public boolean isReceived () {
        
        return received;
    }

    /**
     * Set the value of received
     *
     * @param received 
     *              New value of {@code received}.
     */
    public void setReceived (boolean received) {
        
        this.received = received;
    }

    
    /**
     * Returns the expected arguments of the packet.
     * 
     * @return 
     *              The value of {@code expectedArgs}.
     */
    public byte [] getArgs () {
        
        return this.args;
    }
    
    /**
     * Returns <i>true</i> if this notification has arguments.
     * 
     * @return 
     *              <i>true</i> when {@code expectedArgs == null}.
     */
    public boolean hasArgs () {
        
        return (args != null);
    }
    
    /**
     * Returns the source address from which the answer must come.
     * 
     * @return 
     *              The value of {@code sourceIP}.
     */
    public InetAddress getSourceAddress () {
        
        return this.sourceIP;
    }
    
    /**
     * Returns the source data flow from which the answer must come.
     * 
     * @return 
     *              The value of {@code sourceDataFlow}.
     */
    public byte getSourceDataFlow () {
        
        return this.sourceDataFlow;
    }
    
    /**
     * Returns the value of the expected argument representing a port number 
     * (normally, the 4 bytes after the control message).
     * 
     * @return 
     *              The value of {@code sourcePort}.
     */
    public int getPort () {
        
        return this.sourcePort;
    }
    
    /**
     * Sets the value of the expected argument representing a port number 
     * (normally, the 4 bytes after the control message).
     * 
     * @param newValue 
     *              The new value for {@code sourcePort}.
     */
    public void setPort (int newValue) {
        
        sourcePort = newValue;
    }
}

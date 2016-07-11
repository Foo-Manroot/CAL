package peer;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import common.Common;
import control.ControlMessage;
import control.Notification;
import control.PacketCreator;
import java.net.UnknownHostException;

import static common.Common.logger;

/**
 * This class represents a host and holds all the needed information to
 * communicate correctly with another peer.
 * 
 * <p>
 * Implements {@link Serializable} so it can be stored in a file.
 */
public class Host implements Serializable {

    /**
     * Internet (IP) address of the host.
     */
    private final InetAddress IPaddress;
    
    /**
     * Port on which the packets will be sent.
     */
    private final int port;
    
    /**
     * Date when the last packet was sent or received.
     */
    private Date lastConnection = new Date();
    
    /**
     * Round Trip Time (in milliseconds).
     * Average time that a packet to make a complete trip from the origin host
     * to the destination one and the response to come back from the destination
     * host to the origin one.
     */
    private float RTT = 1000;
    
    /**
     * Indicates which communication this host belongs to.
     * With this parameter, one peer can have multiple connections with 
     * different data flows, and to share the same data flow with different 
     * hosts.
     */
    private byte dataFlow;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     * 
     * @param IPaddress
     *              Internet address of the host.
     * 
     * @param port 
     *              Port where the packets will be sent.
     * 
     * @param dataFlow 
     *              Indicates which communication this host belongs to.
     *          With this parameter, one peer can have multiple connections with 
     *          different data flows, and to share the same data flow with
     *          different hosts.
     */
    public Host (InetAddress IPaddress, int port, byte dataFlow) {
        
        this.IPaddress = IPaddress;
        this.port = port;
        this.dataFlow = dataFlow;
    }
    
/* ------------------------ */
/* ---- STATIC METHODS ---- */
/* ------------------------ */
    
    /**
     * Creates a new instance of a {@link Host} with the information from the 
     * byte array.
     * 
     * <p>
     * The array has to have the following format, with <i>exactly</i> 
     * <b>7 bytes</b>:
     * <pre>
     *  Bytes: 
     *      0: Data flow.
     *      1, 2, 3, 4: IP address (on {@code array[1]} is the highest byte).
     *      5, 6, 7, 8: port (on {@code array [5]} is the highest byte)
     * </pre>
     * 
     * @param buffer 
     *              Byte array containing the necessary info. This array must 
     *          have <i>exactly</i> <b>7 bytes</b>.
     * 
     * 
     * @return 
     *              A new instance of a host. If the information on the buffer 
     *          wasn't correct, it will return {@code null}.
     */
    public static Host newHost (byte [] buffer) {
        
        Host host;
        byte dataFlow;
        byte [] addressAux = new byte [4];
        byte [] portAux = new byte [4];
        int port;
        InetAddress address;
        
        /* Checks the length of the buffer. If it's longer or shorter than
        needed, it will return null */
        if (buffer.length != 9) {
            
            return null;
        }
        
        
        /* Creates the variables needed, so a new host can be created */
        dataFlow = buffer[0];
        System.arraycopy(buffer, 1, addressAux, 0, addressAux.length);
        System.arraycopy(buffer, addressAux.length + 1, portAux, 0, portAux.length);
        
        /* Converts the byte array into an int */
        port = Common.arrayToInt(portAux);
        
        try {
            address = InetAddress.getByAddress(addressAux);
            
            host = new Host (address, port, dataFlow);
            
        } catch (UnknownHostException ex) {
            
            host = null;
            logger.logError("UnknownHostException at Host.newHost(): "
                            + ex.getMessage() + "\n");
        }
        
        return host;
    }
    
/* ---------------------------- */
/* ---- NON-STATIC METHODS ---- */
/* ---------------------------- */
    
    /**
     * When a new message was sent to or received from this host, this method
     * must be called to update the last connection date.
     */
    public void updateLastConnection () {
        
        lastConnection = new Date();
    }
    
    
    /**
     * Updates the value of RTT.
     * 
     * @param lastRTTSample
     *              The time that took the last response to come back after a 
     *          message has been sent.
     * 
     * @return 
     *              The updated value of {@code RTT}.
     */
    public float updateRTT (float lastRTTSample) {
        
        float alpha = 0.875f;
        
        RTT = (RTT * (1 - alpha)) + (lastRTTSample * alpha);
        
        return RTT;
    }
    
    /**
     * Sends a packet to the host and waits for the response.
     * 
     * @param packet
     *              Packet to be sent.
     * 
     * @param waitedResponse 
     *                  Notification to be sent to the server.
     * 
     * @param origin 
     *              Peer that's checking this host's connection.
     * 
     * @param tries
     *              The number of tries to wait for a response.
     * 
     * 
     * @return 
     *              <i>true</i> if the response was received correctly; 
     *          <i>false</i> if it wasn't.
     */
    public boolean send (DatagramPacket packet,
                         Notification waitedResponse,
                         Peer origin,
                         int tries) {
        float count;
        DatagramPacket aux;
        Notification notification;
        Date sendDate;
        
        do {
            try {
                
                /* Creates the socket, sets the destination address to the
                packet and sends it */
                try (DatagramSocket socket = new DatagramSocket()) {
                    count = 1;
                    aux = packet;
                    notification = waitedResponse;
                    
                    aux.setAddress(IPaddress);
                    aux.setPort(port);
                    
                    sendDate = new Date();
                    socket.send(aux);
                    
                    /* Notifies the server and waits for the answer */
                    origin.getServer().addNotification(notification);
                    
                    /* Waits until the message comes back or the wait time
                    runs out */
                    while (!notification.isReceived() && count > 0) {
                        
                        Thread.sleep((long) (RTT * count));
                        
                        /* Decreases the counter, so it waits RTT, then
                        0'8 * RTT, RTT * 0'2... until it comes to RTT * 0 */
                        count -= 0.2;
                    }
                    
                    
                    if (waitedResponse.isReceived()) {
                        
                        /* Updates the last connection date and the RTT */
                        updateLastConnection();
                        updateRTT(new Date().getTime() - sendDate.getTime());
                        
                        return true;
                    }
                }

            } catch (IOException | InterruptedException ex) {

                logger.logError("Exception at Host.send(): " 
                                 + ex.getMessage() + "\n");
            }
            
        } while (--tries > 0);
        
        return waitedResponse.isReceived();
    }
    
    
    /**
     * Sends a packet to the host without waiting for a response.
     * 
     * @param packet
     *              Packet to be sent.
     */
    public void send (DatagramPacket packet) {
        
        try {
            /* Creates the socket, sets the destination address to the
            packet and sends it */ 
            try (DatagramSocket socket = new DatagramSocket()) {
                
                packet.setAddress(IPaddress);
                packet.setPort(port);
                
                socket.send(packet);
                
                /* Updates the last connection date */
                updateLastConnection();
            }
            
        } catch (IOException ex) {
            
            logger.logError("Exception at Host.send(): " 
                             + ex.getMessage() + "\n");
        }
    }
    
    /**
     * Sends a packet to the host without waiting for a response.
     * 
     * @param packet
     *              Packet to be sent.
     * 
     * @param port
     *              Port of this host where the packet should be sent.
     */
    public void send (DatagramPacket packet, int port) {
                
        try {
            /* Creates the socket, sets the destination address to the
            packet and sends it */ 
            try (DatagramSocket socket = new DatagramSocket()) {
                
                packet.setAddress(IPaddress);
                packet.setPort(port);
                
                socket.send(packet);
                
                /* Updates the last connection date */
                updateLastConnection();
            }
            
        } catch (IOException ex) {
            
            logger.logError("Exception at Host.send(): " 
                             + ex.getMessage() + "\n");
        }
    }
    
    /**
     * Tries to communicate with this host by sending 4 packets. If none comes
     * back after 2*RTT, this method returns <i>false</i>.
     * 
     * 
     * @param origin 
     *              Peer that's checking this host's connection.
     * 
     * @return 
     *              <i>true</i> if at least one packet received an answer.
     *          <i>false</i> if none of them does.
     */
    public boolean checkConnection (Peer origin) {
        
        DatagramPacket packet = PacketCreator.CHECK_CON (dataFlow, 
                                                         origin.getServer().getPort());
        Notification notif = new Notification(IPaddress,
                                              dataFlow,
                                              ControlMessage.ACK);
        int tries = 4;
        
        return send (packet, notif, origin, tries);
    }
    

    /**
     * Returns a string representation of this object. The returned string will
     * be formatted this way:
     * <pre>
     * IPaddress: (address)
     *      Port: (port number)
     *      Last connection: (last connection date)
     *      RTT: (RTT)
     *      Data flow: (data flow ID)
     * </pre>
     * 
     * @return 
     *              A string with all the values that this object has.
     */
    @Override
    public String toString() {
        return "IPaddress: " + IPaddress
               + "\n\tPort: " + port
               + "\n\tLast connection: " + lastConnection
               + "\n\tRTT: " + RTT
               + "\n\tData flow: " + dataFlow 
               + "\n";
    }
    
    /**
     *  Creates and returns a byte array with the essential information about
     * this host. The array has the following fields:
     * <pre>
     *  Bytes: 
     *      0: Data flow.
     *      1, 2, 3, 4: IP address (on {@code array[1]} is the highest byte).
     *      5, 6, 7, 8: port (on {@code array [5]} is the highest byte)
     * </pre>
     * 
     * @return 
     *              An array of bytes with the essential information about this
     *          host.
     */
    public byte [] getInfo () {
        
        byte [] retVal = new byte [9];
        byte [] address = IPaddress.getAddress();
        byte [] portArray;
        
        /* Converts the port (4 Bytes) into a byte array */
        portArray = Common.intToArray(port);
        
        /* Copies the information on the correct order */
        retVal [0] = dataFlow;
        System.arraycopy(address, 0, retVal, 1, 4);
        System.arraycopy(portArray, 0, retVal, 5, 4);
        
        return retVal;
    }
    
/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */
    
    /**
     * Changes the data flow this host belongs to.
     * 
     * @param newDataFlow  
     *              The new value for {@code dataFlow}
     */
    public void setDataFlow (byte newDataFlow) {
        
        this.dataFlow = newDataFlow;
    }    
    
    /**
     * Returns the data flow this host belongs to.
     * 
     * @return 
     *              The value of {@code dataFlow}
     */
    public byte getDataFlow () {
        
        return dataFlow;
    }    

    /**
     * Returns the IP address where the data must be sent in order to 
     * communicate with this host.
     * 
     * @return 
     *              The value of {@code IPadress}
     */
    public InetAddress getIPaddress() {
        
        return IPaddress;
    }

    /**
     * Returns the port where the data must be sent in order to communicate with
     * this host.
     * 
     * @return 
     *              The value of {@code port}
     */
    public int getPort() {
        
        return port;
    }

    /**
     * Returns the time when the last communication with this host took place.
     * 
     * @return 
     *              The value of {@code lastConnection}
     */
    public Date getLastConnection() {
        
        return lastConnection;
    }

    /**
     * Returns the time (in milliseconds) that takes a packet to travel from 
     * the origin host to the destination and come back to the origin again.
     * 
     * @return 
     *              The value of {@code RTT}
     */
    public float getRTT() {
        
        return RTT;
    }
}

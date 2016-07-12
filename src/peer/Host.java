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
import java.util.concurrent.TimeUnit;

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
     * Smoothed round trip time, like the one used on TCP.
     * 
     * <p>
     * Used to calculate the time to wait before retransmitting a packet, as 
     * described in <a href="https://tools.ietf.org/html/rfc6298">RFC 6298</a>.
     * 
     * <p>
     * If its value is {@code -1}, no RTT measures has been taken, so this 
     * attribute isn't initialised yet and can't be used to calculate the RTO.
     */
    private float SRTT = -1;
    
    /**
     * Round Trip Time Variation, like the one used on TCP.
     * 
     * <p>
     * Used to calculate the time to wait before retransmitting a packet, as 
     * described in <a href="https://tools.ietf.org/html/rfc6298">RFC 6298</a>.
     * 
     * <p>
     * If its value is {@code -1}, no RTT measures has been taken, so this 
     * attribute isn't initialised yet and can't be used to calculate the RTO.
     */
    private float RTTVAR = -1;
    
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
     * Updates the needed values for the calculation of the estimated RTT.
     * 
     * @param sampleRTT
     *              The time, in milliseconds that took the last response to 
     *          come back after a message has been sent.
     * 
     * @return 
     *              The updated value of {@code SRTT}.
     */
    public float updateRTT (float sampleRTT) {
        
        float alpha = (1 / 8);
        float beta = (1 / 4);
        
        /* If any of the needed attributes are negative, no measures has been
        taken yet */
        if (SRTT < 0 || RTTVAR < 0) {
        
            SRTT = sampleRTT;
            RTTVAR = (sampleRTT / 2);
        } else {
        
            RTTVAR = (1 - beta) * RTTVAR + beta * Math.abs(SRTT - sampleRTT);
            SRTT = (1 - alpha) * SRTT + alpha * sampleRTT;
        }
        
        return SRTT;
    }
    
    /**
     * Returns the retransmission timeout (the time to wait before a packet to
     * be retransmitted), calculated as described in 
     * <a href="https://tools.ietf.org/html/rfc6298">RFC 6298</a>.
     */
    private float getRTO () {
        
        /* Constant defined on the RFC 6298 */
        float k = 4;
        float RTO;
        
        /* If any of the needed attributes are negative, no measures has been
        taken yet, so it must return the default value (1 second) */
        if (SRTT < 0 || RTTVAR < 0) {
            
            return 1000;
        }
        
        /* Calculates the RTO */
        RTO = (SRTT + k * RTTVAR);
        
        return (RTO < 1000)?
                    1000 :
                    RTO;
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
        boolean retransmitted = false;
        DatagramPacket aux;
        Notification notification;
        long sendTime;
        float maxWaitTime = getRTO();
        
        do {
            try {
                
                /* Creates the socket, sets the destination address to the
                packet and sends it */
                try (DatagramSocket socket = new DatagramSocket()) {
                    
                    count = 0.1f;
                    aux = packet;
                    notification = waitedResponse;
                    
                    aux.setAddress(IPaddress);
                    aux.setPort(port);
                    
                    sendTime = System.currentTimeMillis();
                    socket.send(aux);
                    
                    /* Notifies the server and waits for the answer */
                    origin.getServer().addNotification(notification);
                    
                    /* Waits until the message comes back or the wait time
                    runs out */
                    while (!notification.isReceived() && count <= 0.4) {
                        
                        Thread.sleep((long) (maxWaitTime * count));
                        
                        /* Decreases the counter, so it waits (waitTime x 0'1),
                        then (waitTime x 0'2), and so on, until it comes to 
                        (waitTime x 0'4), so the total slept time equals
                        (waiTime x 1) */
                        count += 0.1;
                    }
                    
                    
                    if (waitedResponse.isReceived()) {
                        
                        /* Updates the last connection date */
                        updateLastConnection();
                        
                        /* Only updates the RTT if the packet hasn't been 
                        retransmitted (Karn's algorithm) */
                        if (!retransmitted) {
                            
                            updateRTT (System.currentTimeMillis() - sendTime);
                        }
                        
                        return true;
                    } 
                    
                    retransmitted = true;
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
               + "\n\tRTT: " + ((SRTT < 0)? "?" : SRTT)
               + "\n\tData flow: " + dataFlow 
               + "\n";
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
}

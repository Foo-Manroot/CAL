package control;

/**
 * Represents one of the possible messages that can be send between peers.
 * As there are different groups of messages, there are codes that identify 
 * which group belongs a message to.
 * 
 * <pre>
 * The groups and their codes are:
 *  ·Code 0 -> Control messages
 *  ·Code 1 -> Plaintext
 * </pre>
 */
public enum ControlMessage {
    
/* 
CODE 0 GROUP:
    CONNECTION CONTROL MESSAGES.
*/
    /**
     * Acknowledges the receipt of the message.
     */
    ACK (0, 5),

    /**
     * Rejects a message.
     */
    NACK (0, 6),
    
    /**
     * Asks another peer for its hosts list to send back.
     */
    HOSTS_REQ (0, 11),
    
    /**
     * Response for HOST_REQ.
     * Indicates that the next bytes store the requested information.
     */
    HOSTS_RESP (0, 12),
    
    /**
     * Request for "connection". If the receiptor peer sends back an ACK 
     * message, it means that it included the sender peer into its hosts list 
     * and further communication can be done.
     * 
     * <p>
     * This message must have a single argument on the following 2 bytes, after
     * the message "HELLO". This two bytes will contain the port number where
     * the origin peer is waiting for messages.
     */
    HELLO (0, 7),
    
    /**
     * Request for "disconnection". If the receiptor peer sends back an ACK 
     * message, it means that it deleted the sender peer into its hosts list 
     * and no more communication will be done.
     */
    BYE (0, 5),
    
    /**
     * Sends a message requesting a check for the connection. This message
     * should be answered with an ACK one, so the origin host knows that the
     * destination is still up.
     */
    CHECK_CON (0, 11),
    
    
//    /**
//     * If this message is on the beginning of the buffer, that means that the
//     * following data is a continuation of another packet sent before.
//     * 
//     * <p>
//     * If this message is at the end of the buffer (from BUFF_SIZE - 4 to 
//     * BUFF_SIZE), means that the data is not full and another packet will be 
//     * sent.
//     */
//    CONT (0, 6),
    
    /**
     * This message asks the destination peer to change the data flow id.
     * 
     * <p>
     * This message will have one argument on the next byte (the 14th. byte on 
     * the array), and will tell the destination peer the new data flow id
     * proposed by the origin peer.
     * 
     * <p>
     * The answer to this message should be a {@code CHNG_DF_RESP} with the
     * proper arguments so the origin peer can know if the request has been
     * accepted or rejected.
     */
    CHNG_DF_REQ (0, 13),
    
    /**
     * This message is a response for a data flow id change from another peer.
     * 
     * <p>
     * This message can one argument on the next byte (the 14th. 
     * byte on the array), and will tell the destination peer if the proposed
     * id was <b>rejected</b>, in case the 14th. byte exists , or 
     * <b>accepted</b>, in case the packet is just 13 bytes long.
     * 
     * <p>
     * If the request has been rejected, the packet will have an extra byte (the
     * 14th byte) with another data flow as a counteroffer.
     * In this byte the destination peer (the one who rejected the origin peer's
     * proposal) will send another data flow id, as the origin peer did before.
     */
    CHNG_DF_RESP (0, 14),
    
    
/* 
CODE 1 GROUP:
    PLAINTEXT.
*/
    /**
     * Indicates that the following data is plain text.
     */
    PLAIN (1, 7);
    
    
/* ------------------------------------------ */
/* ---- END OF ENUM ELEMENTS DECLARATION ---- */
/* ------------------------------------------ */
        
    /**
     * Code of the group that this message belongs to. 
     */
    private final int code;
    
    /**
     * Length that will have the header, including the control message itself. 
     * The next byte will be data (or the end of the packet, if no more data 
     * is needed).
     */
    private final int length;
        
   /**
    * Each message must belong to one of the existing groups.
    * 
    * @param code 
    *              Code of the group that this message belongs to. 
    * @param length 
    *               Length that will have the header, including the control 
    *           message itself. The next byte will be data (or the end of the
    *           packet, if no more data is needed).
    */
    private ControlMessage(int code, int length) {
        
        this.code = code;
        this.length = length;
    }
    
    /**
     * Returns the code of the group of which this message belongs.
     * 
     * @return 
     *              The value of {@code code}
     */
    public int getCode () {
        
        return code;
    }
    
    /**
     * Returns the length of the header, including the control message itself.
     * The next byte will be data (or the end of the packet, if no more data is
     * needed).
     * 
     * @return 
     *              The value of {@code length}
     */
    public int getLength () {
        
        return length;
    }
}

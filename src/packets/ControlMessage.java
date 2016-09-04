package packets;

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
     *
     * <p>
     * This method has 4 bytes as the first argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[5]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[8]} the lowest byte.
     */
    ACK (0, 5),

    /**
     * Rejects a message.
     */
    NACK (0, 6),

    /**
     * Asks another peer for its hosts list to send back.
     *
     * <p>
     * This method has 4 bytes as a single argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[11]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[14]} the lowest byte.
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
     *
     * <p>
     * This method has 4 bytes as a single argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[6]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[9]} the lowest byte.
     */
    BYE (0, 5),

    /**
     * Sends a message requesting a check for the connection. This message
     * should be answered with an ACK one, so the origin host knows that the
     * destination is still up.
     *
     * <p>
     * This method has 4 bytes as a single argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[11]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[14]} the lowest byte.
     */
    CHECK_CON (0, 11),


    /**
     * If this message is on the beginning of the buffer, that means that the
     * following data is a continuation of another packet sent before.
     *
     * <p>
     * If this message is at the end of the buffer (from BUFF_SIZE - 4 to
     * BUFF_SIZE), means that the data is not full and another packet will be
     * sent.
     */
    CONT (0, 6),

    /**
     * This message asks the destination peer to change the data flow id.
     *
     * <p>
     * This message will have one argument on the 17th. byte of the array,
     * and will tell the destination peer the new data flow id proposed by
     * the origin peer, after the 4 bytes reserved to the port number.
     *
     * <p>
     * The answer to this message should be a {@code CHNG_DF_RESP} with the
     * proper arguments so the origin peer can know if the request has been
     * accepted or rejected.
     *
     * <p>
     * This method has 4 bytes as the first argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[13]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[16]} the lowest byte.
     */
    CHNG_DF_REQ (0, 13),

    /**
     * This message is a response for a data flow id change from another peer.
     *
     * <p>
     * This message can one argument on the 18th. byte of the array, and will
     * tell the destination peer if the proposed id was <b>rejected</b>, in
     * case the 18th. byte exists , or <b>accepted</b>, in case the packet is
     * just 17 bytes long.
     *
     * <p>
     * If the request has been rejected, the packet will have an extra byte (the
     * 14th byte) with another data flow as a counteroffer.
     * In this byte the destination peer (the one who rejected the origin peer
     * proposal) will send another data flow id, as the origin peer did before.
     *
     * <p>
     * This method has 4 bytes as a single argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[14]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[17]} the lowest byte.
     */
    CHNG_DF_RESP (0, 14),
    
    /**
     * Indicates that the following bytes are information for the application.
     * 
     * <p>
     * This method has 4 bytes as a single argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[11]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[14]} the lowest byte.
     */
    INFO (0, 6),


/*
CODE 1 GROUP:
    PLAINTEXT.
*/
    /**
     * Indicates that the following data is plain text.
     *
     * <p>
     * This method has 4 bytes as the first argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[7]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[10]} the lowest byte.
     */
    PLAIN (1, 7),

    /**
     * Indicates that the following bytes are data (photo, video or any other
     * file).
     *
     * <p>
     * This method has 4 bytes as the first argument: the port where the answer
     * has to be sent. This bytes start at {@code packet[6]}, being that
     * position the highest byte of the integer that represents the port, and
     * {@code packet[9]} the lowest byte.
     */
    DATA (1, 6);


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

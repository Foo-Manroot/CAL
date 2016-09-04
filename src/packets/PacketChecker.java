package packets;

import static packets.ControlMessage.*;

/**
 * This class has some methods to check if a packet is correctly formed and
 * belongs to a specific kind of message.
 *
 * <p>
 * E.G.: if a host receives a message
 * and wants to know if it's an ACK message, it has to call the method
 * {@code ACK()}. If this method returns <i>true</i>, the message was
 * indeed an ACK and was correctly formed.
 */
public class PacketChecker {

    /**
     * Checks the given packet and returns the type of {@link ControlMessage}
     * that is stored in the array. If there isn't a valid packet, returns
     * {@code null}.
     *
     * @param buffer
     *              Array of bytes with the full packet received from the peer.
     *
     * @return
     *              The type of {@link ControlMessage} stored in the buffer, or
     *          {@code null} if there wasn't a recognised packet.
     */
    public static ControlMessage checkPacket (byte [] buffer) {

        if (ACK (buffer)) {

            return ACK;
        }

        if (BYE (buffer)) {

            return BYE;
        }

        if (CHECK_CON (buffer)) {

            return CHECK_CON;
        }

        if (CHNG_DF_REQ (buffer)) {

            return CHNG_DF_REQ;
        }

        if (CHNG_DF_RESP (buffer)) {

            return CHNG_DF_RESP;
        }

        if (HELLO (buffer)) {

            return HELLO;
        }

        if (HOSTS_REQ (buffer)) {

            return HOSTS_REQ;
        }

        if (HOSTS_RESP (buffer)) {

            return HOSTS_RESP;
        }

        if (NACK (buffer)) {

            return NACK;
        }

        if (PLAIN (buffer)) {

            return PLAIN;
        }

        if (CONT (buffer)) {

            return CONT;
        }
        
        if (DATA (buffer)) {
            
            return DATA;
        }
        
        if (INFO (buffer)) {
            
            return INFO;
        }

        return null;
    }

/* ------------------------------------ */
/* ---- CONTROL MESSAGES - GROUP 0 ---- */
/* ------------------------------------ */

    /**
     * Checks if the given byte array is a valid {@code ACK} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean ACK (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8
                  0  x  A  C  K  p1 p2 p3 p4

            Also, the packet length must be ACK.length (no more, nor less).
         */
        return ((buffer.length == ACK.getLength() + 4) &&
                (buffer[0] == ACK.getCode()) &&
                (buffer[2] == 'A') &&
                (buffer[3] == 'C') &&
                (buffer[4] == 'K'));
    }

    /**
     * Checks if the given byte array is a valid {@code NACK} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean NACK (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow:
            Byte: 0  1  2  3  4  5
                  0  x  N  A  C  K

            Also, the packet length must be NACK.length (no more, nor less).
         */
        return ((buffer.length == NACK.getLength()) &&
                (buffer[0] == NACK.getCode()) &&
                (buffer[2] == 'N') &&
                (buffer[3] == 'A') &&
                (buffer[4] == 'C') &&
                (buffer[5] == 'K'));
    }

    /**
     * Checks if the given byte array is a valid {@code HOSTS_REQ} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean HOSTS_REQ (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
                  0  x  H  O  S  T  S  _  R  E  Q  p1 p2 p3 p4

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length == HOSTS_REQ.getLength() + 4) &&
                (buffer[0] == HOSTS_REQ.getCode()) &&
                (buffer[2] == 'H') &&
                (buffer[3] == 'O') &&
                (buffer[4] == 'S') &&
                (buffer[5] == 'T') &&
                (buffer[6] == 'S') &&
                (buffer[7] == '_') &&
                (buffer[8] == 'R') &&
                (buffer[9] == 'E') &&
                (buffer[10] == 'Q'));
    }

    /**
     * Checks if the given byte array is a valid {@code HOSTS_REQ} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean HOSTS_RESP (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow:
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11
                  0  x  H  O  S  T  S  _  R  E  S  P

                Also, the packet length must have the proper length
            (probably more than HOSTS_RESP.length, but no less).
         */
        return ((buffer.length >= HOSTS_RESP.getLength()) &&
                (buffer[0] == HOSTS_RESP.getCode()) &&
                (buffer[2] == 'H') &&
                (buffer[3] == 'O') &&
                (buffer[4] == 'S') &&
                (buffer[5] == 'T') &&
                (buffer[6] == 'S') &&
                (buffer[7] == '_') &&
                (buffer[8] == 'R') &&
                (buffer[9] == 'E') &&
                (buffer[10] == 'S') &&
                (buffer[11] == 'P'));
    }

    /**
     * Checks if the given byte array is a valid {@code HELLO} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean HELLO (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the dataFlow and
            'p1', 'p2', 'p3' and 'p4' the four positions of the byte array
            representing the port:
            Byte: 0  1  2  3  4  5  6  7  8  9  10
                  0  x  H  E  L  L  O  p1 p2 p3 p4

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length == (HELLO.getLength() + 4)) &&
                (buffer[0] == HELLO.getCode()) &&
                (buffer[2] == 'H') &&
                (buffer[3] == 'E') &&
                (buffer[4] == 'L') &&
                (buffer[5] == 'L') &&
                (buffer[6] == 'O'));
    }

    /**
     * Checks if the given byte array is a valid {@code BYE} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean BYE (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8
                  0  x  B  Y  E  p1 p2 p3 p4

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length == BYE.getLength() + 4) &&
                (buffer[0] == BYE.getCode()) &&
                (buffer[2] == 'B') &&
                (buffer[3] == 'Y') &&
                (buffer[4] == 'E'));
    }

    /**
     * Checks if the given byte array is a valid {@code CHECK_CON} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean CHECK_CON (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
            p1, p2... the bytes of the port where the answer is expected (p1 is
            the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
                  0  x  C  H  E  C  K  _  C  O  N  p1 p2 p3 p4

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length == CHECK_CON.getLength() + 4) &&
                (buffer[0] == CHECK_CON.getCode()) &&
                (buffer[2] == 'C') &&
                (buffer[3] == 'H') &&
                (buffer[4] == 'E') &&
                (buffer[5] == 'C') &&
                (buffer[6] == 'K') &&
                (buffer[7] == '_') &&
                (buffer[8] == 'C') &&
                (buffer[9] == 'O') &&
                (buffer[10] == 'N'));
    }

    /**
     * Checks if the given byte array is a valid {@code BYE} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean CONT (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 ...
                  0  x  C  O  N  T  p1 p2 p3 p4  ... (data) ...

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length >= CONT.getLength() + 4) &&
                (buffer[0] == CONT.getCode()) &&
                (buffer[2] == 'C') &&
                (buffer[3] == 'O') &&
                (buffer[4] == 'N') &&
                (buffer[5] == 'T'));
    }

    /**
     * Checks if the given byte array is a valid {@code CHNG_DF_REQ} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean CHNG_DF_REQ (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow, 'f'
          the proposed new data flow id and p1, p2... the bytes of the port
          where the answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17
                  0  x  C  H  N  G  _  D  F  _  R  E  Q  p1 p2 p3 p4 f

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return ((buffer.length == CHNG_DF_REQ.getLength() + 5) &&
                (buffer[0] == CHNG_DF_REQ.getCode()) &&
                (buffer[2] == 'C') &&
                (buffer[3] == 'H') &&
                (buffer[4] == 'N') &&
                (buffer[5] == 'G') &&
                (buffer[6] == '_') &&
                (buffer[7] == 'D') &&
                (buffer[8] == 'F') &&
                (buffer[9] == '_') &&
                (buffer[10] == 'R') &&
                (buffer[11] == 'E') &&
                (buffer[12] == 'Q'));
    }

    /**
     * Checks if the given byte array is a valid {@code CHNG_DF_RESP} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean CHNG_DF_RESP (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow and
         'f' the new propsed data flow id (this last argument is optional) and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 (18)
                  0  x  C  H  N  G  _  D  F  _  R  E  S  P  p1 p2 p3 p4 (f)

                Also, the packet length must have the proper length
            (no more, nor less).
         */
        return (
                (buffer.length == CHNG_DF_RESP.getLength() + 4)
                    ||
                 (buffer.length == CHNG_DF_RESP.getLength() + 5)
                )
                &&
                (buffer[0] == CHNG_DF_RESP.getCode()) &&
                (buffer[2] == 'C') &&
                (buffer[3] == 'H') &&
                (buffer[4] == 'N') &&
                (buffer[5] == 'G') &&
                (buffer[6] == '_') &&
                (buffer[7] == 'D') &&
                (buffer[8] == 'F') &&
                (buffer[9] == '_') &&
                (buffer[10] == 'R') &&
                (buffer[11] == 'E') &&
                (buffer[12] == 'S') &&
                (buffer[13] == 'P');
    }
    
    /**
     * Checks if the given byte array is a valid {@code INFO} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean INFO (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow  and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10  ... buffer.length
                  0  x  I  N  F  O  p1 p1 p3 p4 (data) ...
                Also, the packet length must have the proper length
            (probably more than INFO.length, but no less).
         */
        return ((buffer.length >= INFO.getLength() + 4) &&
                (buffer[0] == INFO.getCode()) &&
                (buffer[2] == 'I') &&
                (buffer[3] == 'N') &&
                (buffer[4] == 'F') &&
                (buffer[5] == 'O'));
    }

/* ----------------------------- */
/* ---- PLAINTEXT - GROUP 1 ---- */
/* ----------------------------- */

    /**
     * Checks if the given byte array is a valid {@code PLAIN} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean PLAIN (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow  and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11  ... buffer.length
                  1  x  P  L  A  I  N  p1 p1 p3 p4 (plaintext message)
                Also, the packet length must have the proper length
            (probably more than PLAIN.length, but no less).
         */
        return ((buffer.length >= PLAIN.getLength() + 4) &&
                (buffer[0] == PLAIN.getCode()) &&
                (buffer[2] == 'P') &&
                (buffer[3] == 'L') &&
                (buffer[4] == 'A') &&
                (buffer[5] == 'I') &&
                (buffer[6] == 'N'));
    }
    
    /**
     * Checks if the given byte array is a valid {@code PLAIN} message.
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message is valid, and <i>false</i>
     *          otherwise.
     */
    public static boolean DATA (byte [] buffer)  {
        /* The packet has the following structure, being 'x' the data flow  and
          p1, p2... the bytes of the port where the answer is expected (p1 is
          the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10   ... buffer.length
                  1  x  D  A  T  A  p1 p1 p3 p4  (plaintext message)
                Also, the packet length must have the proper length
            (probably more than DATA.length, but no less).
         */
        return ((buffer.length >= DATA.getLength() + 4) &&
                (buffer[0] == DATA.getCode()) &&
                (buffer[2] == 'D') &&
                (buffer[3] == 'A') &&
                (buffer[4] == 'T') &&
                (buffer[5] == 'A'));
    }
    
    
/* ----------------------- */
/* ---- MISCELLANEOUS ---- */
/* ----------------------- */

    /**
     * Checks if the given byte array contains the {@code CONT} control message
     * at the end (meaning that there is more data to receive).
     *
     * @param buffer
     *              Byte array with the received message.
     *
     *
     * @return
     *              <i>true</i> if the message has "CONT" at the end of it, and
     *          <i>false</i> otherwise.
     */
    public static boolean hasMoreData (byte [] buffer)  {
        int end = buffer.length;
        
        return ((end > 4) &&
                (buffer[end - 4] == 'C') &&
                (buffer[end - 3] == 'O') &&
                (buffer[end - 2] == 'N') &&
                (buffer[end - 1] == 'T'));
    }
}

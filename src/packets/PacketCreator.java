package packets;

import static packets.ControlMessage.*;

import common.Common;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class has some factory methods to correctly create different packets.
 */
public class PacketCreator {
    
    /**
     * Creates and returns an empty packet of length {@code length}.
     * 
     * @param length 
     *              Length of the buffer from the packet.
     * 
     * 
     * @return 
     *              An empty packet of the desired length.
     */
    public static DatagramPacket voidPacket (int length) {
        
        byte [] buffer = new byte [length];        
        DatagramPacket packet = new DatagramPacket(buffer, length);
        
        return packet;
    }
    
/* ------------------------------------ */
/* ---- CONTROL MESSAGES - GROUP 0 ---- */
/* ------------------------------------ */
    
    /**
     * Creates a packet with an ACK {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param port 
     *              Port that identifies the peer that sent this message.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket ACK (byte dataFlow, int port) {
        
        /* Creates a buffer of ACK.length */
        byte [] buffer = new byte [ACK.getLength() + 4];
        
        byte [] aux = ACK.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
          parameter dataFlow and p1, p2... the bytes of the port where the 
          answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8
                  0  x  A  C  K  p1 p2 p3 p4
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    /**
     * Creates a packet with a NACK {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket NACK (byte dataFlow) {
        
        /* Creates a buffer of NACK.length */
        byte [] buffer = new byte [NACK.getLength()];
        byte [] aux = NACK.toString().getBytes();
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
          parameter dataFlow:
            Byte: 0  1  2  3  4  5
                  0  x  N  A  C  K
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    
    /**
     * Creates a request for the list of hosts on the given data flow.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param port 
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket HOSTS_REQ (byte dataFlow, int port) {
        
        /* Creates a buffer of HOSTS_REQ.length, plus the length for the port */
        byte [] buffer = new byte [HOSTS_REQ.getLength() + 4];
        
        byte [] aux = HOSTS_REQ.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
                  0  x  H  O  S  T  S  _  R  E  Q  p1 p2 p3 p4
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message  and adds the parameter */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    /**
     * Creates a packet with the information about the hosts.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param info 
     *              An array of Bytes representing all the hosts information in
     *          the following format (as returned from Host.getInfo()):
     *  <pre>
     *  Bytes: 
     *      0: Data flow.
     *      1, 2, 3, 4: IP address (on {@code array[1]} is the highest byte).
     *      5, 6: port
     *  </pre>
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket HOSTS_RESP (byte dataFlow,
                                             byte [] info) {
        
        /* Creates a buffer of HOSTS_RESP.length + info.length */
        byte [] buffer = new byte [HOSTS_RESP.getLength()
                                   + info.length];
        byte [] aux = HOSTS_RESP.toString().getBytes();
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow:
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 ... buffer.length
                  0  x  H  O  S  T  S  _  R  E  S  P   (info)
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        
        /* Copies the information after the header */
        System.arraycopy(info, 0, buffer, HOSTS_RESP.getLength(),
                         info.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    
    /**
     * Creates a packet with a HELLO {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param port
     *              Port where the sender will wait for an answer.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket HELLO (byte dataFlow, int port) {
        
        /* Creates a buffer of HELLO.length */
        byte [] buffer = new byte [HELLO.getLength() + 4];
        byte [] aux = HELLO.toString().getBytes();
        DatagramPacket packet;
        byte [] portArray = Common.intToArray(port);
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and 'p1', 'p2', 'p3' and 'p4' the four positions
            of the byte array representing the parameter "port":
            Byte: 0  1  2  3  4  5  6  7  8  9  10
                  0  x  H  E  L  L  O  p1 p2 p3 p4
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        
        /* Adds the port number */
        System.arraycopy(portArray, 0, buffer, aux.length + 2, portArray.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    /**
     * Creates a packet with a BYE {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param port 
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket BYE (byte dataFlow, int port) {
        
        /* Creates a buffer of BYE.length, plus the length of the port array */
        byte [] buffer = new byte [BYE.getLength() + 4];
        
        byte [] aux = BYE.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8
                  0  x  B  Y  E  p1 p2 p3 p4
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    
    /**
     * Creates a packet with a CHECK_CON {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param port 
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket CHECK_CON (byte dataFlow, int port) {
        
        /* Creates a buffer of CHECK_CON.length */
        byte [] buffer = new byte [CHECK_CON.getLength() + 4];
       
        byte [] aux = CHECK_CON.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
                  0  x  C  H  E  C  K  _  C  O  N  p1 p2 p3 p4
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
    
    /**
     * Creates a packet with a CHNG_DF_REQ {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param proposedFlow 
     *              The new data flow proposed to the destination peer.
     * 
     * @param port
     *              Port where the sender will wait for an answer.
     * 
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket CHNG_DF_REQ (byte dataFlow,
                                              byte proposedFlow,
                                              int port) {
        
        /* Creates a buffer of CHNG_DF_REQ.length, plus the argument 
        proposedFlow (1 Byte) and the port as the other argument (4 bytes) */
        byte [] buffer = new byte [CHNG_DF_REQ.getLength() + 5];
        
        byte [] aux = CHNG_DF_REQ.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow, 'f' the parameter proposedFlow and p1, p2... 
            the bytes of the port where the answer is expected (p1 is the 
            highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17
                  0  x  C  H  N  G  _  D  F  _  R  E  Q  p1 p2 p3 p4 f
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message and adds the port */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        /* Adds the parameter on the last byte */
        buffer [buffer.length - 1] = proposedFlow;
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }  
    
    /**
     * Creates a packet with a CHNG_DF_RESP {@link ControlMessage}.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param accepted
     *              This parameter determines whether the parameter 
     *          {@code propsedFlow} should be used (if {@code accepted == true}) 
     *          to form the packet or not (if {@code accepted == false}).
     * 
     * @param proposedFlow 
     *              The new data flow proposed to the destination peer. If the 
     *          parameter {@code accepted} wasn't <b>1</b>, this parameter won't
     *          be used.
     * 
     * @param port
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}.
     */
    public static DatagramPacket CHNG_DF_RESP (byte dataFlow,
                                               boolean accepted,
                                               byte proposedFlow,
                                               int port) {
        
        /* If the proposal was rejected, the buffer must have an extra Byte to 
        store the second argument. */
        int packet_size = (accepted)? 
                            CHNG_DF_RESP.getLength() + 4:
                            CHNG_DF_RESP.getLength() + 5;
        
        /* Creates a buffer of the needed length */
        byte [] buffer = new byte [packet_size];
        
        byte [] aux = CHNG_DF_RESP.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
          parameter dataFlow, 'f' the new proposed flow id (only if needed) 
          and p1, p2... the bytes of the port where the answer is expected (p1 
          is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 (18)
                  0  x  C  H  N  G  _  D  F  _  R  E  S  P  p1 p2 p3 p4 (f)
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message and */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        
        /* Adds the required arguments */        
        if (!accepted) {
            
            buffer [packet_size - 1] = proposedFlow;
        }
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        return packet;
    }
    
/* ----------------------------- */
/* ---- PLAINTEXT - GROUP 1 ---- */
/* ----------------------------- */
    
    /**
     * Creates a packet with the desired plaintext.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param plaintext 
     *              The plaintext message.
     * 
     * @param port
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              An array of completely formed {@link DatagramPacket}, if the
     *          size of the text is larger than the max buffer size.
     */
    public static ArrayList<DatagramPacket> PLAIN (byte dataFlow,
                                                   byte [] plaintext,
                                                   int port) {
        
        
        ArrayList<DatagramPacket> list = new ArrayList<>();
        int size = PLAIN.getLength() + 4 + plaintext.length;
        
        boolean largeText = (size > Common.BUFF_SIZE);
        byte [] buf;
        
        /* Adds first the CONT packets */
        if (largeText) {
            
            buf = Arrays.copyOfRange (plaintext,
                                      Common.BUFF_SIZE - CONT.toString().length(),
                                      plaintext.length);
            list = createCONT(dataFlow, buf, port);
            size = Common.BUFF_SIZE;
        }
        
        /* After the continuation packets, adds the first one (with the PLAIN 
        control message) */
        
        /* Creates a buffer of the proper length */
        byte [] buffer = new byte [size];
        byte [] aux = PLAIN.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11  ... buffer.length
                  1  x  P  L  A  I  N  p1 p1 p3 p4 (plaintext message)
        */
        buffer[0] = 1;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        /* Copies the data after the header and the argument */
        System.arraycopy(plaintext,
                         0, 
                         buffer,
                         PLAIN.getLength() + 4,
                         buffer.length - PLAIN.getLength() - 4);
        
        /* Adds the CONT control message, if needed */
        if (largeText) {
            
            System.arraycopy(CONT.toString().getBytes(),
                             0,
                             buffer,
                             (buffer.length - CONT.toString().length()),
                             CONT.toString().length());
        }
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        /* Adds the packet as the first item of the list */
        list.add (0, packet);
                
        return list;
    }
    
    /**
     * Creates a packet with the desired data.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param data 
     *              The byte array with the data.
     * 
     * @param port
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              An array of completely formed {@link DatagramPacket}, if the
     *          size of the data is larger than the max buffer size.
     */
    public static ArrayList<DatagramPacket> DATA (byte dataFlow,
                                                  byte [] data,
                                                  int port) {
        
        
        ArrayList<DatagramPacket> list = new ArrayList<>();
        int size = DATA.getLength() + 4 + data.length;
        
        boolean largeText = (size > Common.BUFF_SIZE);
        byte [] buf;
        
        /* Adds first the CONT packets */
        if (largeText) {
            
            buf = Arrays.copyOfRange (data,
                                      Common.BUFF_SIZE - CONT.toString().length(),
                                      data.length);
            list = createCONT(dataFlow, buf, port);
            size = Common.BUFF_SIZE;
        }
        
        /* After the continuation packets, adds the first one (with the PLAIN 
        control message) */
        
        /* Creates a buffer of the proper length */
        byte [] buffer = new byte [size];
        byte [] aux = DATA.toString().getBytes();
        byte [] portAux = Common.intToArray(port);
        
        DatagramPacket packet;
        
        /* Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10   ... buffer.length
                  1  x  D  A  T  A  p1 p1 p3 p4 (plaintext message)
        */
        buffer[0] = 1;
        buffer[1] = dataFlow;
        
        /* Fills the control message */
        System.arraycopy(aux, 0, buffer, 2, aux.length);
        System.arraycopy(portAux, 0, buffer, aux.length + 2, portAux.length);
        /* Copies the data after the header and the argument */
        System.arraycopy(data,
                         0, 
                         buffer,
                         DATA.getLength() + 4,
                         buffer.length - DATA.getLength() - 4);
        
        /* Adds the CONT control message, if needed */
        if (largeText) {
            
            System.arraycopy(CONT.toString().getBytes(),
                             0,
                             buffer,
                             (buffer.length - CONT.toString().length()),
                             CONT.toString().length());
        }
        
        packet = new DatagramPacket(buffer, buffer.length);
        
        /* Adds the packet as the first item of the list */
        list.add (0, packet);
                
        return list;
    }
    
    
/* --------------------- */
/* ---- AUX METHODS ---- */
/* --------------------- */
    
    /**
     * Creates an array of packets with continuation data (with the CONT 
     * control message).
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param data 
     *              The data with which the packet will be filled.
     * 
     * @param port
     *              Port where the answer is expected.
     * 
     * 
     * @return 
     *              An array of completely formed {@link DatagramPacket}.
     */
    private static ArrayList<DatagramPacket> createCONT (byte dataFlow,
                                                        byte [] data,
                                                        int port) {
        
        ArrayList<DatagramPacket> packets = new ArrayList<>();
        int size =  Common.BUFF_SIZE -
                    (CONT.getLength() + 4 
                    + CONT.toString().length()),
            from = 0,
            to = size;
        
        byte [] buffer;
        DatagramPacket aux;
        
        /* Creates packets until no more data is left */
        while ((from < (data.length - size)) && (size > 0)) {
            
            buffer = Arrays.copyOfRange (data, from, to);
            
            aux = addCONTdata (dataFlow, buffer, port, true);
            
            if (aux != null) {
                
                packets.add (aux);
                from += size;
                to += size;
                
            } else {
                
                /* If an error occured, maybe the size of the buffer should be
                decreased */
                size--;
            }
        }
        
        /* Adds the last chunk of data */
        size = data.length;
        buffer = Arrays.copyOfRange (data, from, size);
        
        while ((aux = addCONTdata (dataFlow, buffer, port, false)) == null &&
               (size > 0)) {
            
            size--;
        }
        
        if (aux != null) {
            
            packets.add(aux);
        }
        
        return packets;        
    }
    
    /**
     * Creates packets with continuation data.
     * 
     * @param dataFlow 
     *              The flow of this packet. This byte will be on the second
     *          position of the buffer, after the message code.
     * 
     * @param data 
     *              The data with which the packet will be filled. If the size 
     *          of this data is bigger than the packet max size, returns 
     *          {@code null}.
     * 
     * @param port
     *              Port where the answer is expected.
     * 
     * @param moreData 
     *              If this parameter is <i>true</i>, the last four bytes will 
     *          be reserved for the control message "CONT" to notify that more 
     *          data is left.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket}; or 
     *          {@code null}, if the data size is too big.
     */
    private static DatagramPacket addCONTdata (byte dataFlow,
                                               byte [] data,
                                               int port,
                                               boolean moreData) {
        
        int size = (moreData)?
                        CONT.getLength() + 4 + data.length
                        + CONT.toString().length()
                      : CONT.getLength() + 4 + data.length;
        
        if (size > Common.BUFF_SIZE) {
            
            return null;
        }
        
        byte [] buffer = new byte [size];
        byte [] cont = CONT.toString().getBytes();
        
        byte [] portAux = Common.intToArray(port);
        DatagramPacket packet;
        
        
        /*  Fills the data. The packet has the following structure, being 'x' the
            parameter dataFlow and p1, p2... the bytes of the port where the 
            answer is expected (p1 is the highest byte):
            Byte: 0  1  2  3  4  5  6  7  8  9  10 11  ... buffer.length
                  0  x  C  O  N  T  p1 p1 p3 p4     (data)
        */
        buffer[0] = 0;
        buffer[1] = dataFlow;
        
        /* Fills the control message and the port number */
        System.arraycopy(cont, 0, buffer, 2, cont.length);
        System.arraycopy(portAux, 0, buffer, cont.length + 2, portAux.length);
        /* Copies the data after the header and the port number */
        System.arraycopy(data,
                         0, 
                         buffer,
                         CONT.getLength() + 4, 
                         data.length);
        
        /* If there are more data, adds the message "CONT" on the last bytes */
        if (moreData) {
            
            System.arraycopy(cont,
                             0,
                             buffer,
                             (buffer.length - cont.length),
                             CONT.toString().length());
        }
        
        
        packet = new DatagramPacket (buffer, buffer.length);
        
        return packet;
    }
}

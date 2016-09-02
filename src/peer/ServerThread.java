/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import static common.Common.logger;

import common.Common;
import packets.ControlMessage;
import control.Notification;
import packets.PacketChecker;
import packets.PacketCreator;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Thread to handle the server behaviour for the peer.
 */
public class ServerThread extends Thread {

    /**
     * Server socket.
     */
    private DatagramSocket socket;

    /**
     * Thread of pools that will handle all the incoming packets.
     */
    ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /**
     * Peer that has created this server.
     */
    private final Peer peer;

    /**
     * Port on which this server is listening.
     */
    private final int port;

    /**
     * List of notifications.
     */
    private final ConcurrentLinkedQueue<Notification> notifications;

/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */

    /**
     * Constructor.
     *
     * @param port
     *              Port where the server will be listening.
     *
     * @param peer
     *              Peer that has created this server.
     */
    public ServerThread (int port, Peer peer) {

        this.peer = peer;
        this.port = port;

        this.notifications = new ConcurrentLinkedQueue<>();

        try {
            /* Initialises the server thread */
            socket = new DatagramSocket (port);

        } catch (SocketException ex) {

            logger.logError("Exception at ServerThread(): "
                            + ex.getMessage() + "\n");

            socket = null;
        }
    }


    /**
     * Constructor.
     *
     * <p>
     * This method starts the server on the first available port.
     *
     * @param peer
     *              Peer that has created this server.
     */
    public ServerThread (Peer peer) {

        this.peer = peer;

        this.notifications = new ConcurrentLinkedQueue<>();

        try {
            /* Initialises the server thread */
            socket = new DatagramSocket ();

        } catch (SocketException ex) {

            logger.logError("Exception at ServerThread(): "
                            + ex.getMessage() + "\n");

            socket = null;
        }

        port = socket.getLocalPort();
    }

    /**
     * Waits until a message is received and elaborates the answer.
     */
    @Override
    public void run () {

        DatagramPacket received;
        PacketHandler handler;

        /* If the socket hsn't been created correctly, dowsn't perform
        any action */
        if (socket == null) {

            return;
        }

        /* Infinite loop to receive datagrams */
        while (true) {

            /* Reserves memory for the packet to be received */
            received = PacketCreator.voidPacket (Common.BUFF_SIZE);

            try {
                /* Waits until a message is available */
                socket.receive(received);

                handler = new PacketHandler (received);
                /* Adds a new task to the thread pool */
                pool.execute(handler);

                /* Cleans the notifications that were created more than
                10 minutes ago */
                cleanNotifications();

            } catch (SocketException ex) {

                /* Exception thrown when the socket is closed while this thread
                is blocked at socket.receive() */
                logger.logWarning("Server closed.\n");
                break;

            }catch (IOException ex) {

                logger.logError("IOException at ServerThread.run(): "
                            + ex.getMessage() + "\n");
                /* An error happened, so this message can't be shown */
                //continue;
            }

            /* Gets the message and prints it */
//            System.out.println(java.util.Arrays.toString(received.getData()));
        }
    }

    /**
     * Closes the socket and stops the server loop.
     *
     * @return
     *              <i>true</i> if the socket has been correctly closed;
     *          <i>false</i> otherwise.
     */
    public boolean close () {

        socket.close();

        return socket.isClosed();
    }

    /**
     * Eliminates every notification that has been on the list for more than
     * 10 minutes.
     */
    public void cleanNotifications () {

        Date currentDate = new Date ();
        /* 10 minutes = 600.000 milliseconds */
        long eraseTime = 600000;

        for (Notification n : notifications) {

            if ((currentDate.getTime() - n.getCreationDate().getTime())
                    > eraseTime) {

                notifications.remove(n);
            }
        }
    }

    /**
     * Adds a new notification to the list. If it wasn't already there, this
     * method returns <i>true</i>.
     *
     * @param notification
     *              A notification with the message that must come back as a
     *          response.
     *
     * @return
     *              <i>true</i> if the element has been added; <i>false</i> if
     *          it hasn't been added (if it was already on the list).
     */
    public boolean addNotification (Notification notification) {

        if (!notifications.contains(notification)) {

            return (notifications.add(notification));
        }

        /* Cleans the notification list */
        cleanNotifications();

        return false;
    }


    /**
     * Removes the given notification from the list.
     *
     * @param notification
     *              The notification to be removed (if it's on the list).
     */
    public void removeNotification (Notification notification) {

        notifications.remove(notification);
    }

    /**
     * Searches the given {@link DatagramPacket} on the list of
     * {@link Notification} and returns it if it was a message for which
     * the client was waiting.
     *
     * @param packet
     *              The packet that possibly contains a notification.
     *
     *
     * @return
     *              The notification that pointed to the given message, or
     *          {@code null} if it hasn't been found.
     */
    public Notification searchNotification (DatagramPacket packet) {

        /* Iterates through the list until finds a packet that matches the
        searched one (or the list is over) */
        for (Notification n : notifications) {

            if (n.checkPacket(packet)) {

                return n;
            }
        }

        return null;
    }

    /**
     * Searches the given {@link DatagramPacket} on the list of
     * {@link Notification} and returns it if it was a message for which
     * the client was waiting.
     *
     * @param sourceAddr
     *              The address from where the packet has been sent.
     *
     * @param dataFlow
     *              The data flow of the packet.
     *
     *
     * @return
     *              The notification that pointed to the given message, or
     *          {@code null} if it hasn't been found.
     */
    public Notification searchNotification (InetAddress sourceAddr,
                                            byte dataFlow) {

        /* Searches a waiting notification (if exists) that matches the
        source of this NACK and removes it */
        for (Notification n : notifications) {

            if (n.getSourceAddress().equals(sourceAddr) &&
                n.getSourceDataFlow() == dataFlow) {

                return n;
            }
        }

        return null;
    }

/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */

    /**
     * Returns the current socket that's being used.
     *
     * @return
     *              The value of {@code server}, an instance of
     *          {@link DatagramSocket}.
     */
    public DatagramSocket getSocket () {

        return socket;
    }

    /**
     * Returns the port on which the server is listening.
     *
     * @return
     *              The value of {@code port}.
     */
    public int getPort () {

        return port;
    }

/* --------------------- */
/* ---- INNER CLASS ---- */
/* --------------------- */

    /**
     * Inner class to process the received packets and let the server to listen
     * for more incoming packets
     */
    private final class PacketHandler implements Runnable {

        /**
         * Packet that this object will have to process.
         */
        private final DatagramPacket packet;

        /**
         * Buffer with the received message.
         */
        private final byte [] buffer;

        /**
         * Data flow marked by the packet header.
         */
        private final byte dataFlow;

    /* -------------------------------------- */
    /* ---- END OF ATTRIBUTE DECLARATION ---- */
    /* -------------------------------------- */

        /**
         * Constructor.
         *
         * @param packet
         *              Packet that will be processed and answered from this
         *          handler.
         */
        protected PacketHandler (DatagramPacket packet) {

            this.packet = packet;

            /* Innitialices the buffer with the message */
            buffer = new byte [packet.getLength()];
            byte [] packetData = packet.getData();

            /* Gets rid of the unused bytes */
            System.arraycopy(packetData, 0, buffer, 0, packet.getLength());

            this.dataFlow = buffer [1];
//
//            System.out.println("Text: " + new String (buffer) +
//                                "\nBytes: " + java.util.Arrays.toString(buffer) + "\n");
        }

        /**
         * Determines which kind of message is on the packet and elaborates the
         * proper answer for it.
         */
        @Override
        public void run () {

            ControlMessage message;

            message = PacketChecker.checkPacket(buffer);

            if (message == null) {

                logger.logWarning("Unknown message received."
                            + "\n\tFrom " + packet.getAddress()
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
                return;
            }

            /* Determines the nature of the packet, looking at the header */
            switch (message) {

                case ACK:
                    handleACK();
                    break;

                case BYE:
                    handleBYE();
                    break;

                case CHECK_CON:
                    handleCHECK_CON();
                    break;
                    
                case CONT:
                    handleCONT();
                    break;
                    
                case CHNG_DF_REQ:
                    handleCHNG_DF_REQ();
                    break;

                case CHNG_DF_RESP:
                    handleCHNG_DF_RESP();
                    break;

                case HELLO:
                    handleHELLO();
                    break;

                case HOSTS_REQ:
                    handleHOSTS_REQ();
                    break;

                case HOSTS_RESP:
                    handleHOSTS_RESP();
                    break;

                case NACK:
                    handleNACK();
                    break;

                case PLAIN:
                    handlePLAIN();
                    break;

                default:
                    logger.logWarning("Unsupported message received."
                            + "\n\tFrom " + packet.getAddress()
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }

        /**
         * Handles a received {@code ACK} packet.
         *
         * <p>
         * If the message was expected, notifies the client side. If not,
         * discards the message.
         */
        private void handleACK () {

            Notification notif;
            byte newDF;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.ACK.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);

            Host sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux);

            /* Checks if it was one of the messages that the client was waiting
            for */
            if ((notif = searchNotification(packet)) != null) {

                /* If it was an answer for a DF_CHG_RESP, changes the data
                flow of the sender, as the proposal has been accepted */
                if (notif.hasArgs()) {

                    if (sender != null) {

                        newDF = notif.getArgs()[0];
                        /* Changes the data flow of the host from the list */
                        peer.addHostDF(sender, newDF);
                    }
                }

                /* The notification was on the list -> removes it (the
                attribute "received" has already been set to "true" on
                Notification.checkPacket(), called inside
                searchNotification()) */
                notifications.remove(notif);
            } else {

                /* Unknown message, maybe a duplicate of another one */
                logger.logWarning("Unexpected ACK message. Possible duplicate:"
                            + "\nFrom " + packet.getAddress().toString()
                            + ":" + packet.getPort()
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }

        /**
         * Handles a received {@code NACK} packet.
         *
         * <p>
         * Prints a warning message on console.
         */
        private void handleNACK () {

            InetAddress sourceAddr = packet.getAddress();
            Notification notif;

            /* Searches a waiting notification (if exists) that matches the
            source of this NACK and removes it */
            if ((notif = searchNotification(sourceAddr, dataFlow)) != null) {

                notifications.remove(notif);
            } else {
                /* Prints the error message */
                logger.logWarning("Unexpected NACK message received:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }



        /**
         * Handles a received {@code HOSTS_REQ} packet.
         *
         * <p>
         * If the request is accepted, sends a {@code HOSTS_RESP} packet back.
         */
        private void handleHOSTS_REQ () {

            DatagramPacket response;
            Host sender;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.HOSTS_REQ.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);

            /* Searches the sender host on the list */
            if (
                (sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux)
                ) != null) {

                /* Creates a HOSTS_RESP packet and sends it back */
                response = peer.getHostsList().genHOSTS_RESP(dataFlow);
                sender.send(response);

            } else {

                 logger.logWarning("HOSTS_REQ message received from an "
                            + "unknown sender:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }


        /**
         * Handles a received {@code HOSTS_RESP} packet.
         *
         * <p>
         * Extracts the data from the packet and adds the unknown hosts to the
         * list. The sender doesn't receive an answer back.
         */
        private void handleHOSTS_RESP () {

            Notification notif;
            ConcurrentLinkedQueue<Host> addedHosts;
            /* Gets the argument on the HOSTS_RESP message (the bytes
            representation of the hosts) */
            int args = ControlMessage.HOSTS_RESP.getLength();
            byte [] info = new byte [buffer.length - args];

            System.arraycopy(buffer, args, info, 0, info.length);

            /* Checks if it was one of the messages that the client was waiting
            for */
            if ((notif = searchNotification(packet)) != null) {

                /* The notification was on the list -> removes it (the
                attribute "received" has already been set to "true" on
                Notification.checkPacket(), called inside
                searchNotification()) */
                notifications.remove(notif);

                /* Extracts and adds the information on the list */
                if ((addedHosts = peer.getHostsList().readPacket(info)) != null) {

                    if (!addedHosts.isEmpty()) {

                        logger.logWarning("New hosts discovered on the room:\n");

                        /* Shows a message listing the added hosts */
                        for (Host h : addedHosts) {

                            logger.logWarning(h.toString());
                        }
                    }
                } else {

                    /* Malformed packet */
                    logger.logWarning("Malformed HOSTS_RESP packet. "
                                + "\nFrom " + packet.getAddress() + ":"
                                + "\n\tText:" + new String(buffer)
                                + "\n\tBytes: " + Arrays.toString(buffer)
                                + "\n");
                }

            } else {

                /* Unknown message, maybe a duplicate of another one */
                logger.logWarning("Unexpected HOSTS_RESP message. "
                            + "Possible duplicate:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }


        /**
         * Handles a received {@code HELLO} packet.
         *
         * <p>
         * If the connection is accepted, sends an {@code ACK} packet back. If
         * not, sends a {@code NACK} message.
         *
         * <p>
         * Also checks if the sender peer was already on the list. If it wasn't,
         * adds it.
         */
        private void handleHELLO () {

            DatagramPacket response;
            /* Gets the argument on the HELLO message (the port where the sender
            peer will be listening) */
            int args = ControlMessage.HELLO.getLength();
            byte [] portArray = {buffer[args],
                                 buffer[args + 1],
                                 buffer[args + 2],
                                 buffer[args + 3]};

            /* Creates an object representing the sender host */
            Host sender = new Host(packet.getAddress(),
                                   Common.arrayToInt(portArray),
                                   dataFlow);

            /* Searches the sender host on the list. If the sender wasn't
            on it, adds it */
            if (peer.getHostsList().search(dataFlow,
                                           packet.getAddress(),
                                           Common.arrayToInt(portArray)
                )  == null) {

                /* Adds the host to the list */
                if (peer.getHostsList().add(sender)) {

                    /* Notifies the observer */
                    Common.connectionObserver.connectionAccepted(dataFlow);

                    /* Creates an ACK packet and sends it back */
                    response = PacketCreator.ACK(sender.getDataFlow(), port);
                    sender.send(response);

                    logger.logWarning("New host on the room: "
                                     + sender.toString());
                }
            } else {

                /* Sends an ACK packet back */
                response = PacketCreator.ACK(sender.getDataFlow(), port);
                sender.send(response);

                logger.logWarning("HELLO message received from an already "
                            + "known sender:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }


        /**
         * Handles a received {@code BYE} packet.
         *
         * <p>
         * Sends an {@code ACK} {@link ControlMessage} back and deletes the
         * sender from the known hosts list.
         */
        private void handleBYE () {

            DatagramPacket response;
            /* Creates an object representing the sender host */
            Host sender;
            String msg;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.BYE.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);

            /* Searches the sender host on the list. If the sender was on it,
            deletes it. If not, doesn't answer back */
            if (
                (sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux)
                ) != null) {

                /* Creates an ACK packet and sends it back */
                response = PacketCreator.ACK(sender.getDataFlow(), port);
                sender.send(response);

                /* Removes the sender from the known hosts list */
                peer.getHostsList().remove(sender);

                /* Shows a message */
                msg = "\n--------------------\n"
                    + "Peer disconnected: "
                    + sender.getIPaddress() + ":" + sender.getPort()
                        + " - " + logger.getName(sender)
                    +"\n--------------------\n";

                logger.logMsg(msg, sender, false);
                logger.logWarning(msg);

            } else {

                logger.logWarning("BYE message received from an unknown sender:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }



        /**
         * Handles a received {@code CHECK_CON} packet.
         *
         * <p>
         * If the sender is on the list of known hosts, sends an {@code ACK}
         * packet back. If not, nothing can be sent back.
         *
         * <p>
         * If the sender was on the list, updates the last connection date.
         */
        private void handleCHECK_CON () {

            DatagramPacket response;
            Host sender;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.CHECK_CON.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);

            /* Searches the sender on its list. If its not found, returns
            without sending an answer back */
            if ((sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux)
                ) != null) {

                /* As the sender is known, creates an ACK packet sends it */
                response = PacketCreator.ACK(sender.getDataFlow(), port);
                sender.send(response);
            } else {

                logger.logWarning("CHECK_CON message received from an unknown "
                        + "sender:"
                        + "\nFrom " + packet.getAddress() + ":"
                        + "\n\tText:" + new String(buffer)
                        + "\n\tBytes: " + Arrays.toString(buffer)
                        + "\n");
            }
        }



        /**
         * Handles a received {@code CHNG_DF_REQ} packet.
         *
         * <p>
         * If the sender is known, checks if the proposed new data flow Id is
         * being used. If it's available, returns a {@code DF_CHNG_RESP} without
         * arguments, meaning that the request has been accepted and waits for
         * an incoming {@code ACK} to end the negotiation. If it isn't
         * available, sends back a {@cde DF_CHNG_RESP} with another proposed
         * data flow ID.
         */
        private void handleCHNG_DF_REQ () {

            DatagramPacket response;
            Host sender;
            Notification expectedAnswer;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.CHNG_DF_REQ.getLength(),
                             aux, 0, aux.length);
            int senderPort = Common.arrayToInt(aux);

            byte [] argsAnswer;
            /* Gets the second argument on the message (the proposed data
            flow). 4 extra bytes must be added to get this argument because the
            first one is the port number */
            int args = ControlMessage.CHNG_DF_REQ.getLength() + 4;
            byte proposedDF = buffer[args];

            /* Searches the sender host on the list */
            if (
                (sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     senderPort)
                ) != null) {

                /* If the proposed data flow is Common.RESERVED_DATA_FLOW,
                it means that the sender doesn't care about which port to
                choose, so this peer proposes it */
                if (proposedDF == Common.RESERVED_DATA_FLOW) {

                    proposedDF =  peer.findFreeDataFlow();

                    /* If no more data flow ID are available, sends a NACK
                    message back */
                    if (proposedDF == Common.RESERVED_DATA_FLOW) {

                        response = PacketCreator.NACK(dataFlow);
                        sender.send(response);
                        return;
                    }

                    /* The other peer must answer with an ACK. If the answer
                    doesn't comes back, the negotiation ends */
                    argsAnswer = new byte[] {proposedDF};

                    expectedAnswer = new Notification(sender.getIPaddress(),
                                                      dataFlow,
                                                      ControlMessage.ACK,
                                                      argsAnswer);

                    response = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                          false,
                                                          proposedDF,
                                                          port);

                    /* Sends the message */
                    notifications.add(expectedAnswer);
                    sender.send(response);

                    return;
                }


                /* If the proposed data flow is available, accepts. If not,
                makes a counteroffer */
                if (peer.isAvailable(proposedDF)) {

                    /* ACCEPTS THE PROPOSED ID.
                    The other peer must answer with an ACK. If the answer
                    doesn't comes back, the negotiation ends */
                    argsAnswer = new byte[] {proposedDF};

                    expectedAnswer = new Notification(sender.getIPaddress(),
                                                      dataFlow,
                                                      ControlMessage.ACK,
                                                      argsAnswer);

                    response = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                          true,
                                                          proposedDF,
                                                          port);
                } else {

                    /* REJECTS THE PROPOSED ID.
                    The other peer may answer with an ACK if the counteroffer
                    is accepted. If the answer
                    doesn't comes back, the negotiation ends */
                    proposedDF = peer.findFreeDataFlow();

                    /* If no more data flow ID are available, sends a NACK
                    message back */
                    if (proposedDF == Common.RESERVED_DATA_FLOW) {

                        response = PacketCreator.NACK(dataFlow);
                        sender.send(response);
                        return;
                    }

                    argsAnswer = new byte[] {proposedDF};
                    expectedAnswer = new Notification(sender.getIPaddress(),
                                                      dataFlow,
                                                      ControlMessage.ACK,
                                                      argsAnswer);

                    response = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                          false,
                                                          proposedDF,
                                                          port);
                }

                /* Sends the message */
                notifications.add(expectedAnswer);
                sender.send(response);
            } else {

                 logger.logWarning("CHNG_DF_REQ message received from an "
                                    + "unknown sender:"
                                    + "\nFrom " + packet.getAddress() + ":"
                                    + "\n\tText:" + new String(buffer)
                                    + "\n\tBytes: " + Arrays.toString(buffer)
                                    + "\n");
            }
        }

        /**
         * Handles a received {@code CHNG_DF_RESP} packet.
         *
         * <p>
         * If the sender is known, sends the proper response back, and that
         * means to check if the proposal was accepted or rejected and send an
         * {@code ACK}, or another {@code CHNG_DF_RESP} if the counteroffer data
         * flow ID is already used on this peer.
         */
        private void handleCHNG_DF_RESP () {

            DatagramPacket response;
            Host sender;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.CHNG_DF_RESP.getLength(),
                             aux, 0, aux.length);
            int senderPort = Common.arrayToInt(aux);

            Notification notification;
            byte [] argsACK;
            /* Gets the second argument on the message (the proposed data
            flow). 4 extra bytes must be added to get this argument because the
            first one is the port number */
            int args = ControlMessage.CHNG_DF_RESP.getLength() + 4;
            byte proposedDF;

            /* Searches the sender host on the list */
            if (
                (sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     senderPort)
               ) != null) {

                /* If the request has been accepted, sends an ACK back and
                changes the data flow ID on the hosts list. If there's any
                argument, it must begin on packet[args] -> (length > args) */
                if (packet.getLength() <= args) {

                    /* No args -> request ACCEPTED.
                       Creates an ACK packet */
                    response = PacketCreator.ACK(dataFlow, port);

                    /* Searches the expected notification and deletes it after
                    getting the arguments (the new data flow id) */
                    notification = searchNotification(packet);

                    if (!notification.hasArgs()) {

                        logger.logError("Error while negotiating a new "
                                + "data flow ID.\n");
                        return;
                    }

                    proposedDF = notification.getArgs()[0];

                    /* Changes the data flow of the host from the list */
                    peer.addHostDF(sender, proposedDF);

                    /* Finnaly, sends the ACK message */
                    sender.send(response);

                } else {

                    /* With args -> request REJECTED
                       If the argument is Common.RESERVED_DATA_FLOW, it means
                       that the other peer can't handle another connection */
                    if ((proposedDF = buffer [args]) == Common.RESERVED_DATA_FLOW) {

                        return;
                    }

                    /* If the proposed flow ID is free on this peer, sends an
                    ACK packet. If not, sends another CHNG_DF_RESP with another
                    proposed ID */
                    if (peer.isAvailable(proposedDF)) {

                        /* Changes the host's data flow and sends an
                        ACK message */
                        response = PacketCreator.ACK(dataFlow, port);

                        /* Searches the expected notification and deletes it
                        after getting the arguments (the new data flow id) */
                        notification = searchNotification(packet);

                        if (notification == null || !notification.hasArgs()) {

                            logger.logError("Error while negotiating a new "
                                    + "data flow ID.\n");
                            return;
                        }

                        /* Changes the data flow of the host from the list */
                        peer.addHostDF(sender, proposedDF);

                        /* Finnaly, sends the ACK message */
                        sender.send(response);

                        removeNotification(notification);

                    } else {
                        /* Selects another available ID */
                        do {
                            proposedDF++;
                        } while (!peer.isAvailable(proposedDF) &&
                                  proposedDF < 127);

                        /* Creates another CHNG_DF_RESP message and updates the
                        expected notification (it should be an ACK with the
                        updated parameters) */
                        notification = searchNotification(sender.getIPaddress(),
                                                          dataFlow);
                        removeNotification(notification);
                        argsACK = new byte [] {proposedDF};

                        notification = new Notification (sender.getIPaddress(),
                                                         dataFlow,
                                                         ControlMessage.ACK,
                                                         argsACK);

                        response = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                              false,
                                                              proposedDF,
                                                              port);

                        /* Sends the message and adds te notification to the
                        list */
                        sender.send(response);
                        notifications.add(notification);
                    }
                }

            } else {

                logger.logWarning("Unexpected CHNG_DF_RESP:"
                            + "\nFrom " + packet.getAddress() + ":"
                            + "\n\tText:" + new String(buffer)
                            + "\n\tBytes: " + Arrays.toString(buffer)
                            + "\n");
            }
        }


        /**
         * Handles a received {@code PLAIN} packet.
         *
         * <p>
         * If the sender is known, shows the packet and sends an {@code ACK}
         * back. If it isn't, shows the message with a warning note and doesn't
         * send anything back.
         */
        private void handlePLAIN () {

            DatagramPacket response;
            Host sender;

            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.PLAIN.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);

            /* Gets the second argument on the PLAIN message (the plain text),
            being aware that the first argument (the port) uses 4 bytes */
            byte [] msgAux = parsePlain();

            /* Searches the sender on its list. If its not found, returns
            without sending an answer back */
            if ((sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux)
                ) != null) {

                /* Shows the message on screen */
                logger.logMsg(new String (msgAux), sender, true);

                /* As the sender is known, creates an ACK packet sends it */
                response = PacketCreator.ACK(sender.getDataFlow(), port);
                sender.send(response);
            } else {

                /* Unknown sender */
                logger.logWarning("PLAIN message from an unknown source."
                        + "\nMessage: "
                        + "\nFrom " + packet.getAddress() + ":"
                        + "\n\tText:" + new String(buffer)
                        + "\n\tBytes: " + Arrays.toString(buffer)
                        + "\n");
            }
        }
        
        /**
         * Parses the given plaintext message. If there's a CONT message at 
         * the end of the buffer, strips it and creates a notification.
         * 
         * @return 
         *              A byte array with the plaintext message, ready to
         *          be printed.
         */
        private byte [] parsePlain () {
            
            /* Gets the second argument on the PLAIN message (the plain text),
            being aware that the first argument (the port) uses 4 bytes */
            int args = ControlMessage.PLAIN.getLength();
            byte [] msgAux;
            
            /* If there is more data left, creates a notification */
            if (PacketChecker.hasMoreData (buffer)) {
              
                /* Avoids showing the last 4 bytes ("CONT") */
                msgAux = new byte [packet.getLength() - args - 4 - 4];

                System.arraycopy (packet.getData(),
                                  args + 4,
                                  msgAux,
                                  0,
                                  msgAux.length);
                
            } else {
                
                msgAux = new byte [packet.getLength() - args - 4];

                System.arraycopy (packet.getData(),
                                  args + 4,
                                  msgAux,
                                  0,
                                  msgAux.length);
                
                /* Appends a carry return to the end of the message */
                msgAux [msgAux.length - 2] = '\r';
                msgAux [msgAux.length - 1] = '\n';
            }
            
            return msgAux;
        }

        
        /**
         * Handles a received {@code CONT} packet.
         *
         * <p>
         * If the sender is known, shows the packet and sends an {@code ACK}
         * back. If it isn't, shows the message with a warning note and doesn't
         * send anything back.
         */
        private void handleCONT () {

            DatagramPacket response;
            Host sender;
            
            byte [] aux = new byte [4];
            System.arraycopy(buffer, ControlMessage.CONT.getLength(),
                             aux, 0, aux.length);
            int portAux = Common.arrayToInt(aux);
            
            /* Searches the sender on its list. If its not found, returns
            without sending an answer back */
            if ((sender = peer.getHostsList().search(dataFlow,
                                                     packet.getAddress(),
                                                     portAux)
                ) != null) {

                /* Shows the message on screen */
//                logger.logMsg(new String (msgAux), sender, true);

                /* As the sender is known, creates an ACK packet sends it */
                response = PacketCreator.ACK(sender.getDataFlow(), port);
                sender.send(response);
            } else {

                /* Unknown sender */
                logger.logWarning("CONT message from an unknown source."
                        + "\nMessage: "
                        + "\nFrom " + packet.getAddress() + ":"
                        + "\n\tText:" + new String(buffer)
                        + "\n\tBytes: " + Arrays.toString(buffer)
                        + "\n");
            }
        }
    }
}

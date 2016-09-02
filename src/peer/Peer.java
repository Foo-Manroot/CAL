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
import packets.PacketCreator;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents a node of the chat room.
 * It will create a thread to take care of the user input (the message) and
 * another one to act as a server.
 */
public class Peer {
    
    /**
     * List with all the known peers.
     */
    private final HostsList hostsList = new HostsList(this);
    
    /**
     * Server-side of this peer.
     */
    private final ServerThread server;
    
    /**
     * List of all the clients handling different connections.
     */
    private final ConcurrentLinkedQueue<ClientHandler> clients;
    
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     * 
     * @param port
     *              Port where the socket will be created.
     */
    public Peer (int port) {
        
        /* Creates the server thread and starts it */
        server = new ServerThread(port, this);
        server.start();
        
        if (server.getSocket() != null) {
                   
            logger.logWarning("Peer listening on "
                            + server.getSocket().getLocalAddress()
                            + ":" + server.getSocket().getLocalPort() + "\n");
        } else {
            
            logger.logError("Error trying to start peer on port " + port + "\n");
        }
        
        clients = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Constructor.
     * 
     * <p>
     * This method starts the peer on the first available port.
     */
    public Peer () {
        
        /* Creates the server thread and starts it */
        server = new ServerThread(this);
        server.start();
        
        if (server.getSocket() != null) {
                   
            logger.logWarning("Peer listening on "
                            + server.getSocket().getLocalAddress()
                            + ":" + server.getPort() + "\n");
        } else {
            
            logger.logError("Error trying to start peer on port " +
                             server.getPort() + "\n");
        }
        
        clients = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Closes all open connections and streams.
     * 
     * @return 
     *              <i>true</i> if all the known peers acknowledged the 
     *          {@code BYE} message and the streams were correctly closed.
     */
    public boolean close () {
        
        return (disconnect() && server.close());
    }
    
    
    
    /**
     * Sends a request to another host to begin a conversation.
     * 
     * @param host 
     *              Destination host of the request.
     * 
     * @return 
     *              <i>true</i> if the connection was accepted and the host has
     *          been added to the list correctly; <i>false</i> if the connection
     *          has been rejected or the host already was on the list.
     */
    public boolean connect (Host host) {
        
        return joinChatRoom(host, host.getDataFlow());
    }
    
    
    /**
     * Closes all open connections.
     * 
     * 
     * @return 
     *              <i>true</i> if all the known peers acknowledged the message.
     */
    public boolean disconnect () {
        
        boolean retVal = true;
        ArrayList<Byte> knownDF = new ArrayList<>();
        
        /* Searches all the different data flows that this peer knows */
        for (Host h : hostsList.getHosts()) {
            
            if (!knownDF.contains(h.getDataFlow())) {
                
                knownDF.add(h.getDataFlow());
            }
        }
        
        /* Leaves all the conversations */
        for (Byte b : knownDF) {
            
            retVal = (leaveChatRoom(b))?
                        retVal :
                        false;
        }
        
        return retVal;
    }
    
    
    /**
     * Tries to join a given chat room. If the attempt failed, this method 
     * returns <i>false</i>. If the connection was correctly achieved, this 
     * method returns <i>true</i>.
     * 
     * @param host 
     *              One of the known hosts in the chat room.
     * 
     * @param chatRoom 
     *              ID of the chat room.
     * 
     * 
     * @return 
     *              <i>false</i> if the attempt failed, <i>true</i> if the 
     *          connection was correctly achieved.
     */
    public boolean joinChatRoom (Host host, byte chatRoom) {
        
        DatagramPacket packet;
        boolean retVal;
        Notification notif = new Notification(host.getIPaddress(),
                                              chatRoom,
                                              ControlMessage.ACK);
        
        /* Searchs the host on the list to avoid duplicates */
        if (hostsList.search(chatRoom,
                             host.getIPaddress(),
                             host.getPort()
            ) != null) {
            
            return false;
        }
        
        /* Creates a HELLO packet and sends it to the destination host */
        packet = PacketCreator.HELLO(chatRoom, server.getPort());
        
        /* Sends a message and waits for the response */
        retVal = host.send(packet, notif, this, 4);
        
        /* If the destination answered with an ACK message, adds the host to 
        the list */
        if (notif.isReceived()) {
            
            hostsList.add(host);
        }
        
        /* Removes the notification from the list */
        server.removeNotification(notif);
        
        /* Asks this new peer for a list of another peers on the rooms and 
        adds them to the list, too */
        packet = PacketCreator.HOSTS_REQ(chatRoom, server.getPort());
        notif = new Notification(host.getIPaddress(),
                                 chatRoom, 
                                 ControlMessage.HOSTS_RESP);
        
        retVal = (retVal)? 
                    host.send(packet, notif, this, 1) 
                  : false;
        
        /* Removes the notification from the list */
        server.removeNotification(notif);
        
        return retVal;
    }
    
    /**
     * Ends the data exchange with the hosts in the given chat room and notifies
     * it to the hosts in that room before deleting them from the list.
     * 
     * @param chatID 
     *              Identifier of the data flow from that room.
     * 
     * 
     * @return 
     *              <i>true</i> if all the known peers acknowledged the message.
     */
    public boolean leaveChatRoom (byte chatID) {
        
        boolean retVal = true;        
        /* Creates a list with all the host in that room */
        ArrayList<Host> hosts = hostsList.search(chatID);
        DatagramPacket packet;
        Notification waitedResponse;
        
        /* Sends a message to each host trying to end the conversation. If,
        after 4 tries, the host didn't responded, it's removed from the list
        anyways */
        for (Host h : hosts) {
            
            /* Sets the parameters and sends the packet */
            packet = PacketCreator.BYE(chatID, server.getPort());
            waitedResponse = new Notification(h.getIPaddress(),
                                              h.getDataFlow(),
                                              ControlMessage.ACK);
            
            /* If the answer didn't came back, sets the return value to false */
            retVal = (h.send(packet, waitedResponse, this, 4))?
                        retVal :
                        false;
            
            /* Removes the notification from the list */
            server.removeNotification(waitedResponse);
            
            /* Removes the host form the list */
            hostsList.remove(h);
        }
        
        /* Searches for the client that was controlling that data flow and 
        stops it */
        for (ClientHandler c : clients) {
            
            if (c.getDataFlow() == chatID) {
                
                c.endExecution();
                clients.remove(c);
            }
        }
        
        return retVal;
    }
    
    
    /**
     * Tries to have a private conversation with the given host. 
     * If the attempt failed, this method returns <i>false</i>. If the
     * connection was correctly achieved, this method returns <i>true</i>.
     * 
     * @param host 
     *              The host with whom the private conversation will be done.
     * 
     * 
     * @return 
     *              <i>false</i> if the attempt failed, <i>true</i> if the 
     *          connection was correctly achieved.
     */
    public boolean startConversation (Host host) {
        
        boolean retVal;
        byte proposedFlow = findFreeDataFlow();
        
        if (proposedFlow == Common.RESERVED_DATA_FLOW) {
            
            return false;
        }
        
        /* Starts a negotiation with the given host on the known dataFlow
        and tries to change it */
        DatagramPacket packet = PacketCreator.CHNG_DF_REQ(host.getDataFlow(),
                                                          proposedFlow,
                                                          server.getPort());
        
        Notification expectedAnswer = new Notification(host.getIPaddress(),
                                                       host.getDataFlow(),
                                                       ControlMessage.CHNG_DF_RESP,
                                                       new byte []{proposedFlow});
        
        retVal = host.send(packet, expectedAnswer, this, 4);
        
        /* Removes the notification from the list */
        server.removeNotification(expectedAnswer);
        
        return retVal;
    }
    
    /**
     * Ends the data exchange with the given host.
     * 
     * @param host 
     *              The host whose conversation will end.
     * 
     * 
     * @return 
     *              <i>true</i> if the destination peer correctly received the
     *          message and answered with an {@code ACK} {@link ControlMessage}.
     */
    public boolean endConversation (Host host) {
        
         boolean retVal = true;      
        /* Creates a list with all the host in that room */
        DatagramPacket packet;
        Notification waitedResponse;
        
        /* Sends a message to the host trying to end the conversation. If,
        after 4 tries, the host didn't responded, it's removed from the list
        anyways */
        packet = PacketCreator.BYE(host.getDataFlow(), server.getPort());
        waitedResponse = new Notification(host.getIPaddress(),
                                          host.getDataFlow(),
                                          ControlMessage.ACK);

        /* If the answer didn't came back, sets the return value to false */
        retVal = (host.send(packet, waitedResponse, this, 4))?
                    retVal :
                    false;

        /* Removes the host form the list */
        hostsList.remove(host);
        
        /* Removes the notification from the list */
        server.removeNotification(waitedResponse);
        
        /* Searches for the client that was controlling that data flow and 
        stops it */        
        for (ClientHandler c : clients) {
            
            if (c.getDataFlow() == host.getDataFlow()) {
                
                c.endExecution();
                clients.remove(c);
            }
        }
        
        return retVal;
    }
    
    
    /**
     * Asks the given host to share its information about the rest of the hosts
     * that it knows on the same chat room.
     * 
     * @param receiver 
     *              Host that's going to be asked about the host it knows.
     * 
     * 
     * @return 
     *              <i>true</i> if the host sent a {@code HOSTS_RESP}
     *          {@link ControlMessage} back; <i>false</i> otherwise.
     */
    public boolean updateHosts (Host receiver) {
         
        boolean retVal;
        DatagramPacket packet = PacketCreator.HOSTS_REQ(receiver.getDataFlow(),
                                                        server.getPort());
        
        Notification notification = new Notification(receiver.getIPaddress(),
                                                     receiver.getDataFlow(),
                                                     ControlMessage.HOSTS_RESP);
                
        retVal = receiver.send(packet, notification, this, 1);
        
        /* Removes the notification from the list */
        server.removeNotification(notification);
        
        return retVal;
    }
    
    /**
     * Checks the connection of all the hosts on the list.
     * 
     * @return 
     *              <i>true</i> if all the hosts sent an ACK message back;
     *          <i>false</i> if any of them didn't.
     */
    public boolean checkConnection () {
        
        boolean retVal = true;
        StringBuilder msg = new StringBuilder();
        
        for (Host h : hostsList.getHosts()) {
            
            msg.append("Checking connection with:\n ").
                    append(h.toString());
            
            if (!h.checkConnection(this)) {
                
                msg.append("\t-Failure.\n\n");
                retVal = false;
            } else {
                
                msg.append("\t-Success.\n\n");
            }
        }
        
        logger.logWarning(new String (msg));
        
        return retVal;
    }
    
    /**
     * Creates a new client to control the given data flow and adds it to the 
     * list. If another client for that data flow already exists, this method
     * doesn't do anything.
     * 
     * @param dataFlow
     *              The data flow that the created client will control.
     */
    public void createClient (byte dataFlow) {
        
        ClientHandler client;
        
        /* If a client for that data flow already exists, returns */
        for (ClientHandler c : clients) {
            
            if (c.getDataFlow() == dataFlow) {
                
                return;
            }
        }
        
        /* Creates, starts and adds the client to the list */
        client = new ClientHandler(this, dataFlow);
        new Thread(client).start();

        clients.add(client);
    }
    
    /**
     * Adds the given client to the list only if no other clean was created with
     * the same data flow. 
     * 
     * <p>
     * This method starts the thread of the client (invoking 
     * <i>ClientHandler.start()</i>) before adding it to the list.
     * 
     * @param client 
     *              The client to be added.
     */
    public void addClient (ClientHandler client) {
        
        /* If a client for that data flow already exists, returns */
        for (ClientHandler c : clients) {
            
            if (c.getDataFlow() == client.getDataFlow()) {
                
                return;
            }
        }
        
        /* Adds the client to the list after starting it */
        client.start();
        
        clients.add(client);
    }
        
    
    /**
     * Returns the next available data flow ID, or 
     * {@code Common.RESERVED_DATA_FLOW} if all are being used.
     * 
     * @return
     *              The next available data flow ID,  or 
     *          {@code RESERVED_DATA_FLOW}.
     */
    public byte findFreeDataFlow () {
        
        /* Creates a list with all the possible values of the data flow
        (from -128 to 127). The value 127 is reserved */
        for (byte b = -128; b < 127; b++) {
            
            if (isAvailable(b)) {
                
                return b;
            }
        }
        
        return Common.RESERVED_DATA_FLOW;
    }
    
    /**
     * Returns <i>true</i> if the given data flow ID isn't being used yet. If
     * the parameter {@code dataFlow} is the same as Common.RESERVED_DATA_FLOW
     * or the ID isn't available, returns <i>false</i>.
     * 
     * @param dataFlow 
     *              The data flow to be searched.
     * 
     * 
     * @return 
     *              <i>true</i> if the data flow is available.
     */
    public boolean isAvailable (byte dataFlow) {
        
        if (dataFlow == Common.RESERVED_DATA_FLOW) {
            
            return false;
        }
        
        for (Host h : hostsList.getHosts()) {
            
            if (h.getDataFlow() == dataFlow) {
                
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Tries to change the ID of the given chat room.
     * NOT IMPLEMENTED YET (and maybe it will never be...).
     * 
     * @param oldID
     *              Old id of the chat room.
     * 
     * @param newID
     *              New id proposed for the chat room.
     * 
     * 
     * @return 
     *              <i>true</i> if the change has been successfully made.
     * 
     * @deprecated <b>NOT IMPLEMENTED YET</b>
     */
    public boolean changeRoomID (byte oldID, byte newID) {
        
        /* Searches all the hosts on the given room */
        ArrayList<Host> knownHosts = hostsList.search(newID);
        
        if (knownHosts.isEmpty()) {
            
            return false;
        }
        
        /* ---- */
        /* ---- */
        /* ---- */
        
        return false;
    }
    
    /**
     * Creates and adds a new {@link Host} element with the given data flow ID.
     * 
     * @param host 
     *             The whose parameters (except the data flow) will be used to
     *          add a new host to the list.
     * 
     * @param newDataFlow
     *              The new data flow value.
     */
    public void addHostDF (Host host, byte newDataFlow) {
        
        byte oldDataFlow = host.getDataFlow();
        Host aux;
        
        if (hostsList.search(oldDataFlow,
                             host.getIPaddress(),
                             host.getPort()
            ) == null) {
            
            return;
        }
        
        /* Changes the data flow on the peer and adds it to the list */
        aux = new Host(host.getIPaddress(), host.getPort(), newDataFlow);
        hostsList.add(aux);
        
        /* Notifies the observer */
        Common.connectionObserver.connectionAccepted(newDataFlow);
    }

    
    /**
     * Sends the message to all the hosts on the given chat room and returns 
     * a list with all the hosts that didn't answered back with an {@code ACK}
     * {@link ControlMessage}.
     * 
     * @param message 
     *              Message to be sent.
     * 
     * @param chatRoom
     *              Chat room where the message will be sent.
     * 
     * 
     * @return 
     *              A list with all the hosts that didn't answered to the 
     *          message.
     */
    public ArrayList<Host> sendMessage (String message, byte chatRoom) {
        
        ArrayList<Host> failures = new ArrayList<>();
        DatagramPacket packet;
        Notification expectedAnswer;
        
        /* Sends the message to the rest of the peers on the current
        conversation (4 tries until giving up) */
        for (Host h : hostsList.search(chatRoom)) {

            packet = PacketCreator.PLAIN(chatRoom,
                                         message.getBytes(),
                                         server.getPort());

            expectedAnswer = new Notification(h.getIPaddress(),
                                              h.getDataFlow(),
                                              ControlMessage.ACK);

            if (!h.send(packet, expectedAnswer, this, 4)) {

                logger.logError("Error trying to send the message \"" + 
                                message + "\" to:" + 
                                "\n" + h.toString());
                failures.add(h);
            }
            
            /* Removes the notification from the list */
            server.removeNotification(expectedAnswer);
        }
        
        return failures;
    }
    
/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */
    
    /**
     * Returns the list of known hosts by this peer.
     * 
     * @return 
     *              The value of {@code hostsList}
     */
    public HostsList getHostsList () {
        
        return hostsList;
    }
    
    /**
     * Returns the server thread of this peer.
     * 
     * @return 
     *              The value of {@code server}
     */
    public ServerThread getServer () {
        
        return server;
    }
}

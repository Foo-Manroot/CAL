package peer;

import common.Common;
import control.ControlMessage;
import control.PacketCreator;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * List for the peer to know with which hosts is communicating.
 * 
 * <p>
 * Implements {@link Serializable} so it can be stored in a file.
 */
public class HostsList implements Serializable {
    
    /**
     * Structure to store all the known hosts.
     */
    private ConcurrentLinkedQueue<Host> hosts;
    
    /**
     * Host that this list is associated to. This lets the list to avoid adding
     * the local peer to its own list, causing unnecessary traffic within the 
     * same peer.
     */
    private final Peer localPeer;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     * 
     * @param localPeer
     */
    public HostsList (Peer localPeer) {
        
        hosts = new ConcurrentLinkedQueue<>();
        
        this.localPeer = localPeer;
    }
    
    
    /**
     * Adds a new host into the list.
     * 
     * @param host
     *              Host to be added.
     * 
     * @return
     *              <i>true</i> if the element has been added correctly, 
     *          <i>false</i> otherwise.
     */
    public boolean add (Host host) {
        
        if (host == null) {
            
            return false;
        }
        
        if (hosts.contains(host)) {
            
            return false;
        }
        
        return hosts.add(host);
    }
    
    /**
     * Removes the host from the list, if exists.
     * 
     * @param host 
     *             Element to be removed from the list
     * 
     * 
     * @return 
     *              <i>false</i> if the element didn't exist on the list or 
     *          couldn't be removed, or <i>true</i> if it was removed 
     *          successfully.
     */
    public boolean remove (Host host) {
        
        if (hosts.contains(host)) {
            
            return hosts.remove(host);
        }
        
        return false;
    }
    
    
    /**
     * Searches all the hosts that belongs to the given data flow. If no hosts
     * with that characteristic is found, returns an empty list.
     * 
     * @param dataFlow 
     *              The data flow that the found hosts must share.
     * 
     * 
     * @return 
     *              A list with all the hosts that take part on a given data 
     *          flow, or an empty list if none are found.
     */
    public ArrayList<Host> search (byte dataFlow) {
        
        ArrayList<Host> list = new ArrayList<>();
        
        for (Host h : hosts) {
            
            if (h.getDataFlow() == dataFlow) {
                
                list.add(h);
            }
        }
        
        return list;
    }
    
    /**
     * Searches all the hosts that belongs to the given data flow. If no hosts
     * with that characteristic is found, returns an empty list.
     * 
     * @param dataFlow 
     *              The data flow that the found hosts must share.
     * 
     * @param adddress
     *              The address of the searched host
     * 
     * 
     * @return 
     *              The searched host.
     * 
     * 
     * @deprecated
     *              It's not recommended to used this method because it searches
     *          a host based only in its data flow and IP address. As one host
     *          may contain different instances of the same program, the peer 
     *          must be identified by its address, data flow <b>and port</b>.
     */
     public Host search (byte dataFlow, InetAddress adddress) {
        
        for (Host h : hosts) {
            
            if ((h.getDataFlow() == dataFlow) &&
                 h.getIPaddress().equals(adddress)) {
                
                return h;
            }
        }
        
        return null;
    }
     
     /**
     * Searches all the hosts that belongs to the given data flow. If no hosts
     * with that characteristic is found, returns an empty list.
     * 
     * @param dataFlow 
     *              The data flow that the found hosts must share.
     * 
     * @param adddress
     *              The address of the searched host
     * 
     * @param port 
     *              The port where the other host is listening.
     * 
     * 
     * @return 
     *              The searched host, or <i>null</i> if it hasn't been found.
     */
     public Host search (byte dataFlow, InetAddress adddress, int port) {
        
        for (Host h : hosts) {
            
            if ((h.getDataFlow() == dataFlow) &&
                 h.getIPaddress().equals(adddress) &&
                 h.getPort()== port) {
                
                return h;
            }
        }
        
        return null;
    }
    
    /**
     * Searches for a host that has the same attributes (IP address, port and 
     * data flow) as the searched one.
     * 
     * @param host 
     *              Host whose values will be searched on the list.
     * 
     * @param list
     *              List where the comparisons will be made.
     * 
     * 
     * @return 
     *              The position at which another host with the same attributes
     *          has been found, or -1 if it wasn't in the list.
     */
    public int find (Host host, ConcurrentLinkedQueue<Host> list) {
        
        int position = 0;
        
        for (Host h : list) {
            
            if (host.getDataFlow() == h.getDataFlow() &&
                host.getIPaddress().equals(h.getIPaddress()) &&
                host.getPort() == h.getPort()) {
             
                return position;
            }
            
            position++;
        }
        
        return -1;
    }
    
    
    /**
     * Generates a {@code HOSTS_RESP} packet with the information about all
     * the hosts on the given data flow.
     * 
     * @param dataFlow
     *              Flow of the hosts that will be shared.
     * 
     * 
     * @return 
     *              A completely formed {@link DatagramPacket} with the 
     *          information about the known hosts on the given dataFlow.
     */
    public DatagramPacket genHOSTS_RESP (byte dataFlow) {
        
        ByteBuffer info = ByteBuffer.allocate(0);
        DatagramPacket packet;
        /* Searches all the hosts on the given flow and gets their info. */
        ArrayList<Host> foundHosts = search(dataFlow);
        
        for (Host h : foundHosts) {
            
            byte [] hostInfo = h.getInfo();
            byte [] aux = new byte [info.capacity() + hostInfo.length];
            
            /* Copies the two arrays (info and hostInfo) into the new one */
            System.arraycopy(info.array(), 0, aux, 0, info.array().length);
            System.arraycopy(hostInfo, 0, aux, info.array().length, hostInfo.length);
            
            /* Rebuilds the previous byte buffer with the new information */
            info = ByteBuffer.wrap(aux);
        }
        
        packet = PacketCreator.HOSTS_RESP(dataFlow, info.array());
        
        return packet;
    }
    
    
    /**
     * Reads the array of bytes returned with the {@code HOSTS_RESP} 
     * {@link ControlMessage} and adds the unknown hosts into the list.
     * 
     * <p>
     * The array of bytes required (the parameter {@code packet}) can be 
     * obtained by calling the method <i>DatagramPacket.getData()</i> and 
     * extracting only the utile charge of the packet (getting rid of the 
     * header). With a {@code HOSTS_RESP} packet it can be done by extracting
     * the data from <i>HOSTS_RESP.getLength()</i> to <i>buffer.length</i>.
     * 
     * @param packet 
     *              Array of bytes returned with the response to a 
     *          {@code HOSTS_REQ}, containing the needed attributes to create a
     *          new {@link Host} and add it to the list (if it wasn't already)
     * 
     * 
     * @return 
     *              A list with all the added hosts, or <i>null</i> if the 
     *          packet wasn't correctly formed.
     */
    public ConcurrentLinkedQueue<Host> readPacket (byte [] packet) {
        
        /* Divides the packet into smaller pieces of 7 bytes to create new 
        hosts and adds them to the list, if they wasn't already on it */
        byte [] buffer = new byte [9];
        //ConcurrentLinkedQueue<Host> knownHosts = hosts;
        ConcurrentLinkedQueue<Host> changes = new ConcurrentLinkedQueue<>();
        Host auxHost;
        
        /* If the packet wasn't complete, returns */
        if ((packet.length % 9) != 0) {

            return null;
        }
        
        /* Creates a new host until no more bytes are left */
        for (int i = 0; i < packet.length; i += buffer.length) {
            
            System.arraycopy(packet, i, buffer, 0, buffer.length);
            
            /* Creates the host with the given information */
            if ((auxHost = Host.newHost(buffer)) != null) {
                
                /* Checks if the unknown host is the localPeer */
                if (localPeer != null &&
                    auxHost.getPort() != localPeer.getServer().getPort() &&
                    !auxHost.getIPaddress().equals(
                            localPeer.getServer().getSocket().getLocalAddress())
                    ) {
                    
                    /* If the host was correctly created, searches it and
                    adds it */
                    if (find(auxHost, hosts) < 0) {

                        /* Sends a HELLO message. If they answer back, they're
                        added to the list */
                        if (localPeer.connect(auxHost)) {
                            
                            changes.add(auxHost);
                        }
                    }
                }
            }
        }
        
        //hosts = knownHosts;
        
        return changes;
    }
    
    /**
     * Reads the file containing all the known hosts (if exists) and appends 
     * them to the existing list or creates a new one, depending on the parameter
     * {@code append}.
     * 
     * 
     * @param append 
     *              If this parameter is <i>true</i>, the hosts contained on the
     *          file will be added to the ones already on the list. If this 
     *          parameter is <i>false</i>, the list will be overridden.
     * 
     * 
     * @return 
     *              The updated list of hosts.
     */
    public ConcurrentLinkedQueue<Host> readFile (boolean append) {
        
        ConcurrentLinkedQueue<Host> knownHosts = (append)?
                hosts :
                new ConcurrentLinkedQueue<>();
        
        ArrayList<Object> aux;
        
        /* Opens the file and, if any hostList is returned, appends the hosts 
        to the current list (or overrides it, depending on the parameter) */
        aux = Common.openDatFile(Common.FILE_PATH);
        
        if (!aux.isEmpty()) {
            
            /* The file exists and is not empty -> iterates through the lists 
            and adds all the hosts that are not on the list yet */
            for (Object o : aux) {
                
                HostsList auxList = (HostsList) o;
                
                for (Host h : auxList.getHosts()) {
                    
                    /* If the list doesn't contain this host, it's added */
                    if (find(h, knownHosts) < 0) {
                        
                        knownHosts.add(h);
                    }
                }
            }
            
        } else {
        
            /* The file doesn't exist or is empty -> no data can be retrieved */
            knownHosts = hosts;
        }
        
        /* Updates the list of hosts */
        hosts = knownHosts;
        
        return hosts;
    }
    
    /**
     * Writes the hosts list into the selected file.
     * 
     * @param path 
     *              Path to the file.
     * 
     * @param append 
     *              If this parameter is <i>true</i>, the given content will be
     *          appended to the previous one of the file (if any). If it's 
     *          <i>false</i>, the content will be overridden.
     * 
     * @return 
     *              <i>true</i> if the content was stored successfully, 
     *          <i>false</i> if any error occurred.
     */
    public boolean writeFile (String path, boolean append) {
        
        return Common.storeDatFile(Common.FILE_PATH, this, append);
    }
    
/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */
    
    /**
     * Returns all the hosts in the list.
     * 
     * @return 
     *              The value of {@code hosts}.
     */
    public ConcurrentLinkedQueue<Host> getHosts () {
        
        return hosts;
    }
    
}

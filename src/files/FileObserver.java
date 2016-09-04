/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import java.util.concurrent.ConcurrentHashMap;
import peer.Host;

/**
 * Observer for the file transfers. 
 * 
 * <p>
 * When a peer waits for another one for confirmation before the transfer to 
 * begin, this observer will take car.
 */
public class FileObserver {

    /**
     * List of all the {@link FileSharer} waiting for response.
     * 
     * <p>
     *              <br>Key -> The {@link Host} that should answer to the 
     *          transfer request.
     *              <br>Value -> The {@link FileShare} waiting for response.
     */
    private final ConcurrentHashMap<Host, FileSharer> expResponse;

    /**
     * Only one instance is allowed.
     */
    private static FileObserver instance = null;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     */
    private FileObserver () {
        
        expResponse = new ConcurrentHashMap<>();
    }
    
    /**
     * Returns the only allowed instance of {@code FileObserver}, or initialises
     * a new one and returns it.
     * 
     * 
     * @return 
     *              The only allowed instance of {@link FileObserver}.
     */
    public static FileObserver newObserver () {
        
        if (instance == null) {
            
            instance = new FileObserver();
        }
        
        return instance;
    }
    
    /**
     * Adds the given host to the list of threads waiting for a response.
     * 
     * @param host 
     *              The destination host for the file.
     * 
     * @param manager 
     *              The thread that's taking care of the file transfer.
     */
    public void addHost (Host host, FileSharer manager) {
        
        if (!expResponse.containsKey(host)) {
            
            expResponse.put(host, manager);
        }
    }
    
    /**
     * Notifies a sleeping thread that an answer from the given host has been 
     * received.
     * 
     * 
     * @param host 
     * 
     * @param answer 
     *              If <i>true</i>, the received answer is an {@code ACK} 
     *          message (and, therefore, the file won't be sent).
     */
    public void notifyAnswer (Host host, boolean answer) {

        FileSharer manager;

        if (expResponse.containsKey (host)) {
            
            manager = expResponse.remove (host);
            manager.notifyConfirmation (answer);
        }
    }
    
    /**
     * Searches the given host on the hosts list.
     * 
     * 
     * @param host
     *              Host to search on the list.
     * 
     * 
     * @return 
     *              <i>true</i> if the host is in the list.
     */
    public boolean containsHost (Host host) {
        
        return expResponse.containsKey (host);
    }
}

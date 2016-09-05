/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import java.io.File;
import java.util.Date;
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
    private final ConcurrentHashMap <Host, FileSharer> expResponse;

    /**
     * Only one instance is allowed.
     */
    private static FileObserver instance = null;
    
    /**
     * Stores the written bytes on every observed file.
     * 
     * <p>
     *              <br>Key -> The host that's sending the file.
     *              <br>Value -> An {@link Entry} with the file path and the 
     *          written bytes.
     */
    private final ConcurrentHashMap<Host, Entry> observedFiles;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     */
    private FileObserver () {
        
        expResponse = new ConcurrentHashMap<>();
        observedFiles = new ConcurrentHashMap<>();
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
    
    /**
     * When a new file is received, it should be added to this list to track 
     * the transfer progress.
     *
     * 
     * @param sender 
     *              The host that sends the file.
     * 
     * @param filePath 
     *              A string with the path of the file.
     * 
     * 
     * @return 
     *              <i>true</i> if the file has been added correctly, or 
     *          <i>false</i> if it was already on the list.
     */
    public boolean addFile (Host sender, String filePath) {
        
//        File file = new File (filePath);
//        int index = 2;
        
        Entry entry;
        
        if (observedFiles.containsKey (sender)) {
            
            return false;
        }
        
//        /* If the file already exists, creates a new one with a number
//        appended to the file name */
//        while (file.exists()) {
//            
//            filePath += "_" + index;
//            file = new File (filePath);
//            
//            index++;
//        }
        
        /* Adds an entry with 0 on the "writtenBytes" field */
        entry = new Entry (filePath, 0);
        
        /* Puts the values on the list */
        observedFiles.put (sender, entry);
        
        return true;
    }
    
    /**
     * Writes the given array of bytes into the desired file.
     * 
     * @param sender 
     *              The host that sends the file.
     * 
     * @param data 
     *              The array of bytes to append to the file.
     * 
     *
     * @return 
     *              The new size of the file, in bytes, or -1 if there wasn't 
     *          any entry with the given host on the list..
     */
    public long writeToFile (Host sender, byte [] data) {
        
        Entry entry;
        long fileLength;
        String path;
        
        /* Searches the file on the list. If it's not found, creates an entry */
        if (!observedFiles.containsKey (sender)) {
            
            return -1;
        }
        
        /* Gets the file path */
        entry = observedFiles.get(sender);
        
        path = entry.getFilePath();
        
        /* Writes the data onto the file */
        fileLength = FileSharer.writeFile (path, data);
        
        /* Updates the value of writtenBytes and the entry on the list */
        entry.setWrittenBytes (fileLength);
        
        observedFiles.put (sender, entry);
        
        return fileLength;
    }
}

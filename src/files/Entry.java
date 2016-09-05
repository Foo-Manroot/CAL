/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

/**
 * Entry for the list of written bytes per file, on {@link FileObserver}.
 */
public class Entry {

    private final String filePath;
    private long writtenBytes;

    /**
     * Constructor.
     * 
     * @param filePath 
     *              String with the file path.
     * 
     * @param writtenBytes 
     *              Long number with the written bytes on th file.
     */
    public Entry (String filePath, long writtenBytes) {
        
        this.filePath = filePath;
        this.writtenBytes = writtenBytes;
    }

    public String getFilePath () {
        
        return filePath;
    }

    public long getWrittenBytes () {
        
        return writtenBytes;
    }
    
    /**
     * Sets a new value for {@code writtenBytes}.
     * 
     * @param newValue 
     *              The new value for {@code writtenBytes}.
     */
    public void setWrittenBytes (long newValue) {
     
        writtenBytes = newValue;
    }
}

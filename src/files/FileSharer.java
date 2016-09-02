/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import static common.Common.BUFF_SIZE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import peer.Host;

/**
 * This class implements some methods to load, send and receive files through 
 * the net.
 */
public class FileSharer {
    
    /**
     * Reads and sends the indicated file to the destination host.
     * 
     * 
     * @param path 
     *              A string with the path to the file to be sent.
     * 
     * @param destination 
     *              
     * 
     * @return 
     *              <br>0 on success.
     *              <br>-1  if the file hasn't been found.
     *              <br>-2 if an IOException has been thrown and caught.
     */
    public static int sendFile (String path, Host destination) {
        
        try {
            RandomAccessFile f = new RandomAccessFile(path, "r");
        
            byte b [];
            long size = f.length();
            int offset = 0;
            int read;

            /* Main loop for the file to be read and stored into the buffer */
            while (offset < size) {

                b = new byte [BUFF_SIZE];

                f.seek(offset);
                read = f.read(b, 0, b.length);

                /* Packs the data on a datagram and sends it */

                offset += b.length;
            }
            
        } catch (FileNotFoundException ex) {
        
            System.out.println("FileNotFoundException: " + ex.getMessage());
            return -1;
            
        } catch (IOException ex) {
            
            System.out.println("IOException: " + ex.getMessage());
            return -2;
        }
        
        return 0;
    }
}

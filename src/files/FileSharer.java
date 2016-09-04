/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;


import static packets.ControlMessage.*;

import common.Common;
import control.Notification;
import gui.main.PeerGUI;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.util.ArrayList;
import packets.PacketCreator;
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
            int buff_size;
            ArrayList<java.net.DatagramPacket>  packets;
            Notification expectedAnswer;

            /* Sends all the data on smaller packets */
            while (offset < size) {

                b = new byte [Common.BUFF_SIZE - 4 - DATA.getLength()];

                f.seek(offset);
                read = f.read(b, 0, b.length);

                /* If the read data is smaller than the maximum buffer size,
                creates a smaller auxiliar buffer to avoid sending unnecessary
                data */
                buff_size = (read > (Common.BUFF_SIZE - 4 - DATA.getLength()))? 
                            Common.BUFF_SIZE - 4 - DATA.getLength()
                            : read;
                
                byte aux [] = new byte [buff_size];
                
                System.arraycopy(b, 0,
                                 aux, 0,
                                 aux.length);
                
                packets = PacketCreator.DATA ((byte) 1, aux, 1234);

                expectedAnswer =  new Notification(destination.getIPaddress(),
                                                   destination.getDataFlow(),
                                                   ACK);
                
                /* Sends the generated packets, one by one */
                for (DatagramPacket d : packets) {

                    destination.send (d, expectedAnswer, PeerGUI.peer, 4);
                }

                offset += read;
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

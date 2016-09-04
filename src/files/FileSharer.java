/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;


import static packets.ControlMessage.*;
import static common.Common.logger;

import common.Common;
import control.Notification;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.util.ArrayList;
import packets.PacketCreator;
import peer.Host;
import peer.Peer;

/**
 * This class implements some methods to load, send and receive files through 
 * the net.
 */
public class FileSharer extends Thread {
    
    /**
     * A string with the path to the file to be sent.
     */
    private final String path;
    
    /**
     * The peer that sends the data.
     */
    private final Peer origin;
    
    /**
     * The host where the data ill be sent.
     */
    private final Host destination;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     * 
     * 
     * @param path 
     *              A string with the path to the file to be sent.
     * 
     * @param origin 
     *              The peer that sends the data.
     * 
     * @param destination 
     *              The host where the data ill be sent.
     */
    public FileSharer (String path, Peer origin, Host destination) {
        
        this.path = path;
        this.origin = origin;
        this.destination = destination;
    }
    
    @Override
    public void run () {
        
        /* Tries to get the confirmation of the destination host */
        //getConfirmation();
        
        /* If confirmation has been given, sends the file */
        sendFile();
    }
    
    /**
     * Reads and sends the indicated file to the destination host.
     *              
     * 
     * @return 
     *              <br>0 on success.
     *              <br>-1  if the file hasn't been found.
     *              <br>-2 if an IOException has been thrown and caught.
     *              <br>-3 if the other host refused the file transfer.
     */
    private int getConfirmation () {
        
        DatagramPacket confirmation;
        Notification expectedAnswer;
        byte [] info = ("File:"
                        + genFileInfo(new File (path))).getBytes();

        /* Asks the other host for confirmation */
        confirmation = PacketCreator.INFO(destination.getDataFlow(),
                                          info,
                                          origin.getServer().getPort());

        expectedAnswer = new Notification(destination.getIPaddress(), 
                                          destination.getDataFlow(),
                                          ACK);

        if (confirmation == null) {

            return -2;
        }


        destination.send (confirmation, expectedAnswer, origin, 1);
            
        
        return 0;
    }
    
    /**
     * Reads and sends the indicated file to the destination host.
     *              
     * 
     * @return 
     *              <br>0 on success.
     *              <br>-1  if the file hasn't been found.
     *              <br>-2 if an IOException has been thrown and caught.
     */
    public int sendFile () {
        
        try {
            RandomAccessFile f = new RandomAccessFile(path, "r");
            Notification expectedAnswer;
            
            byte b [];
            long size = f.length();
            
            int offset = 0;
            int read;
            
            int buff_size;
            ArrayList<java.net.DatagramPacket>  packets;
            
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

                packets = PacketCreator.DATA (destination.getDataFlow(),
                                              aux,
                                              origin.getServer().getPort());

                expectedAnswer =  new Notification(destination.getIPaddress(),
                                                   destination.getDataFlow(),
                                                   ACK);

                /* Sends the generated packets, one by one */
                for (DatagramPacket d : packets) {

                    destination.send (d, expectedAnswer, origin, 4);
                }

                offset += read;
            }

        } catch (FileNotFoundException ex) {

            logger.logError ("FileNotFoundException: " + ex.getMessage());
            return -1;

        } catch (IOException ex) {

            logger.logError ("IOException: " + ex.getMessage());
            return -2;
        }
        
        return 0;
    }
    
    /**
     * Generates a string with the information of the file.
     * 
     * @param file 
     *              The file whose information is going to be returned.
     * 
     * 
     * @return 
     *              A string with the information of the file.
     */
    public static String genFileInfo (File file) {
        
        return "\nFile information: \n"
                + "\tName: " + file.getName() + "\n"
                + "\tSize: " + file.length() + " Bytes\n";
    }
}

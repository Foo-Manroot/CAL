/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;


import static packets.ControlMessage.*;
import static common.Common.logger;
import static common.Common.fileObserver;

import common.Common;
import control.Notification;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;
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

    /**
     * While this attribute s <i>false</i>, no answer has been received.
     */
    private boolean answerReceived;

    /**
     * If the destination host accepted the transfer, this attribute will be
     * <i>true</i>.
     */
    private boolean confirmed;

    /**
     * Maximum wait time (in <b>milliseconds</i>) before giving up and
     * assuming that the destination host rejected the file transfer.
     *
     * <p>
     * 300000 milliseconds = 300 seconds (5 minutes).
     */
    private final long MAX_WAIT_TIME = 300000;
    
    /**
     * Date when the request was sent.
     */
    private Date startDate;
    
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

        answerReceived = false;
        confirmed = false;
        startDate = new Date ();
    }
    
    
    @Override
    public void run () {

        /* Tries to get the confirmation of the destination host */
        getConfirmation ();

        startDate = new Date ();
        checkAnswer ();

        if (answerReceived) {

            if (confirmed) {
                /* If confirmation has been given, sends the file */
                if (sendFile () < 0) {

                    logger.logWarning ("Error sending the file to "
                                       + destination.toString() + "\n");
                }
            } else {

                logger.logWarning ("The following host rejected the file"
                                   + " transference: " + destination.toString()
                                   + "\nFile: " + path + "\n");
            }
        } else {

            logger.logWarning ("The following host didn't answer the file"
                               + " transfer proposal: "
                               +  destination.toString()
                               + "\nFile: " + path + "\n");
        }
    }

    /**
     * Blocks the thread until it's notified or the maximum wait time
     * is reached.
     */
    private synchronized void checkAnswer () {

        /* Waits until an answer is received or the max. wait time is reached */
        while ((!answerReceived) &&
               ((new Date().getTime() - startDate.getTime()) < MAX_WAIT_TIME)) {

            try {

                wait (MAX_WAIT_TIME);

            } catch (InterruptedException ex) {

                logger.logError ("InterruptedException at "
                                + "FileSharer.checkAnswer(): "
                                + ex.getMessage());
            }
        }
    }

    /**
     * If the petition has been accepted, sends the file.
     *
     *
     * @param confirmed
     *              If the other peer accepted the transfer (indicated by
     *          {@code confirmed} being <i>true</i>), sends the file.
     */
    public synchronized void notifyConfirmation (boolean confirmed) {

        answerReceived = true;
        this.confirmed = confirmed;

        notifyAll ();
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
    private void getConfirmation () {

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

        /* Sends the packet and waits for confirmation */
        destination.send (confirmation, expectedAnswer, origin, 1);

        fileObserver.addHost(destination, this);
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
    private int sendFile () {

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
     * Writes the given array of bytes into the desired file.
     * 
     * @param path 
     *              A string with the path to the file. If it don't exists, a 
     *          new file is created.
     * 
     * @param bytes 
     *              The array of bytes to append to the file.
     * 
     * 
     * @return 
     *              The new size of the file, in bytes.
     */
    public static long writeFile (String path, byte [] bytes) {
        
        File file = new File (path);
        
        try (FileOutputStream stream = new FileOutputStream (file, true)) {
            
            stream.write(bytes);
            
        } catch (IOException ex) {
            
            logger.logError("IOException at FileSharer.writeFile (): "
                            + ex.getMessage());
        }
        
        return file.length ();
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

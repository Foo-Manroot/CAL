/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Miguel
 */
public class NotificationTest {
    
    private Notification instance;
    
    private final DatagramPacket correctPacket;
    private final DatagramPacket wrongPacket;
    
    private final byte sourceDataFlow;
    private final InetAddress sourceIP;
    private final ControlMessage message;
    
    public NotificationTest() throws UnknownHostException {
        
        sourceIP = InetAddress.getLocalHost();
        sourceDataFlow = 1;
        message = ControlMessage.ACK;
        
        instance = new Notification(sourceIP, sourceDataFlow, message);
        
        correctPacket = PacketCreator.ACK(sourceDataFlow);
        correctPacket.setAddress(sourceIP);
        
        wrongPacket = PacketCreator.NACK(sourceDataFlow);
        wrongPacket.setAddress(sourceIP);
    }
    
    @Before
    public void setUp () {
        
        instance = new Notification(sourceIP, sourceDataFlow, message);
    }

    /**
     * Test of checkPacket method, of class Notification.
     */
    @Test
    public void testCheckPacket() {
        System.out.println("checkPacket");
        
        assertTrue(instance.checkPacket(correctPacket));
        
        assertFalse(instance.checkPacket(wrongPacket));
    }

    /**
     * Test of isReceived method, of class Notification.
     */
    @Test
    public void testIsReceived() {
        System.out.println("isReceived");
        
        assertFalse(instance.isReceived());
        
        instance.checkPacket(correctPacket);
        
        assertTrue(instance.isReceived());
    }
}

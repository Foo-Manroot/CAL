/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import peer.Host;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Miguel
 */
public class PacketCheckerTest {
    
    private final int port = 1234;

    /**
     * Test of ACK method, of class PacketChecker.
     */
    @Test
    public void testCheckACK() {
        System.out.println("checkACK");
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.ACK(dataFlow, port).getData();
        
        boolean result = PacketChecker.ACK(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of NACK method, of class PacketChecker.
     */
    @Test
    public void testCheckNACK() {
        System.out.println("checkNACK");
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.NACK(dataFlow).getData();
        
        boolean result = PacketChecker.NACK(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of HOSTS_REQ method, of class PacketChecker.
     */
    @Test
    public void testCheckHOSTS_REQ() {
        System.out.println("checkHOSTS_REQ");
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.HOSTS_REQ(dataFlow, port).getData();
        
        boolean result = PacketChecker.HOSTS_REQ(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of HOSTS_RESP method, of class PacketChecker.
     * 
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testCheckHOSTS_RESP() throws UnknownHostException {
        System.out.println("checkHOSTS_RESP");
        
        byte dataFlow = 1;
        int port = 1234;
        Host host = new Host(InetAddress.getLocalHost(), port, dataFlow);
        
        byte[] buffer = PacketCreator.HOSTS_RESP(dataFlow, host.getInfo()).getData();
        
        boolean result = PacketChecker.HOSTS_RESP(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of HELLO method, of class PacketChecker.
     */
    @Test
    public void testCheckHELLO() {
        System.out.println("checkHELLO");
        
        byte dataFlow = 1;
        
        byte[] buffer = PacketCreator.HELLO(dataFlow, 1234).getData();
        
        boolean result = PacketChecker.HELLO(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of BYE method, of class PacketChecker.
     */
    @Test
    public void testCheckBYE() {
        System.out.println("checkBYE");
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.BYE(dataFlow, port).getData();
        
        boolean result = PacketChecker.BYE(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of CHECK_CON method, of class PacketChecker.
     */
    @Test
    public void testCheckCHECK_CON() {
        System.out.println("checkCHECK_CON");
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.CHECK_CON(dataFlow, port).getData();
        
        boolean result = PacketChecker.CHECK_CON(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of CHNG_DF_REQ method, of class PacketChecker.
     */
    @Test
    public void testCheckCHNG_DF_REQ() {
        System.out.println("checkCHNG_DF_REQ");
        
        byte dataFlow = 1;
        byte proposedFlow = 2;
        byte[] buffer = PacketCreator.CHNG_DF_REQ(dataFlow,
                                                  proposedFlow,
                                                  port).getData();
        
        boolean result = PacketChecker.CHNG_DF_REQ(buffer);
        
        assertTrue(result);
    }

    /**
     * Test of CHNG_DF_RESP method, of class PacketChecker.
     */
    @Test
    public void testCheckCHNG_DF_RESP() {
        System.out.println("checkCHNG_DF_RESP");
        
        byte dataFlow = 1;
        byte accepted = 1;
        byte[] buffer = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                   accepted,
                                                   dataFlow,
                                                   port).getData();
        
        boolean result = PacketChecker.CHNG_DF_RESP(buffer);
        
        assertTrue(result);
        
        accepted = 0;
        buffer = PacketCreator.CHNG_DF_RESP(dataFlow,
                                            accepted,
                                            dataFlow,
                                            port).getData();
        
        result = PacketChecker.CHNG_DF_RESP(buffer);
        
        assertTrue(result);
    }
    
    /**
     * Test of PLAIN method, of class PacketChecker.
     */
    @Test
    public void testCheckPLAIN() {
        System.out.println("checkPLAIN");
        
        String message = "Test message.\nASdf\tg.";
        
        byte dataFlow = 1;
        byte[] buffer = PacketCreator.PLAIN(dataFlow,
                                            message.getBytes(),
                                            port).getData();        
        boolean result = PacketChecker.PLAIN(buffer);
        
        assertTrue(result);
    }
    
}

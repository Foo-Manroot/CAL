/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import common.Common;
import peer.Host;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Miguel
 */
public class PacketCreatorTest {
    
    private final int port = 1234;
    
    /**
     * Test of voidPacket method, of class PacketCreator.
     */
    @Test
    public void testNewVoidPacket() {
        System.out.println("newVoidPacket");
        int length = 10;
        
        DatagramPacket result = PacketCreator.voidPacket(length);
        
        assertArrayEquals(result.getData(), new byte [length]);
    }

    /**
     * Test of ACK method, of class PacketCreator.
     */
    @Test
    public void testNewACK() {
        System.out.println("newACK");
        byte dataFlow = 1;
        byte [] aux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'A', 'C', 'K',
                             aux [0], aux[1] , aux[2], aux[3]};
        
        DatagramPacket result = PacketCreator.ACK(dataFlow, port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of NACK method, of class PacketCreator.
     */
    @Test
    public void testNewNACK() {
        System.out.println("newNACK");
        byte dataFlow = 1;
        
        byte [] expResult = {0, dataFlow, 'N', 'A', 'C', 'K'};
        
        DatagramPacket result = PacketCreator.NACK(dataFlow);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of HOSTS_REQ method, of class PacketCreator.
     */
    @Test
    public void testNewHOSTS_REQ() {
        System.out.println("newHOSTS_REQ");
        
        byte dataFlow = 1;
        byte [] aux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'H', 'O', 'S', 'T', 'S', '_', 
                            'R', 'E', 'Q',
                             aux [0], aux[1], aux[2], aux[3]};
        
        DatagramPacket result = PacketCreator.HOSTS_REQ(dataFlow, port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of HOSTS_RESP method, of class PacketCreator.
     * 
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testNewHOSTS_RESP() throws UnknownHostException {
        System.out.println("newHOSTS_RESP");
        
        byte dataFlow = 1;
        int port = 1234;
        Host host = new Host(InetAddress.getLocalHost(), port, dataFlow);
        
        byte[] aux = ControlMessage.HOSTS_RESP.toString().getBytes();
        byte[] info = host.getInfo();
        
        byte [] expResult = new byte [ControlMessage.HOSTS_RESP.getLength()
                                        + info.length];
        expResult[0] = 0;
        expResult[1] = dataFlow;
        
        System.arraycopy(aux, 0, expResult, 2, aux.length);
        System.arraycopy(info, 0, expResult, 2 + aux.length, info.length);
        
        DatagramPacket result = PacketCreator.HOSTS_RESP(dataFlow, info);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of HELLO method, of class PacketCreator.
     */
    @Test
    public void testNewHELLO() {
        System.out.println("newHELLO");
        byte dataFlow = 1;
        int port = 1234;
        byte [] portAux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'H', 'E', 'L', 'L', 'O',
                             portAux[0], portAux[1], portAux[2], portAux[3]};
        
        DatagramPacket result = PacketCreator.HELLO(dataFlow, port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of BYE method, of class PacketCreator.
     */
    @Test
    public void testNewBYE() {
        System.out.println("newBYE");
        
        byte dataFlow = 1;
        byte aux [] = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'B', 'Y', 'E',
                             aux [0], aux[1], aux[2], aux[3]};
        
        DatagramPacket result = PacketCreator.BYE(dataFlow, port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of CHECK_CON method, of class PacketCreator.
     */
    @Test
    public void testNewCHECK_CON() {
        System.out.println("newCHECK_CON");
        byte dataFlow = 1;
        byte [] aux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'C', 'H', 'E', 'C', 'K', '_',
                            'C', 'O', 'N',
                             aux [0], aux[1], aux[2], aux[3]};
        
        DatagramPacket result = PacketCreator.CHECK_CON(dataFlow, port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of CHNG_DF_REQ method, of class PacketCreator.
     */
    @Test
    public void testNewCHNG_DF_REQ() {
        System.out.println("newCHNG_DF_REQ");
        
        byte proposedFlow = 2;
        byte dataFlow = 1;
        byte [] aux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'C', 'H', 'N', 'G', '_',
                             'D', 'F', '_', 'R', 'E', 'Q', 
                             aux [0], aux[1], aux[2], aux[3],
                             proposedFlow};
        
        DatagramPacket result = PacketCreator.CHNG_DF_REQ(dataFlow,
                                                          proposedFlow,
                                                          port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of CHNG_DF_RESP method, of class PacketCreator.
     */
    @Test
    public void testNewCHNG_DF_RESP() {
        System.out.println("newCHNG_DF_RESP - Rejected proposal");
       
        byte proposedFlow = 2;
        byte accepted = 0;
        byte dataFlow = 1;
        byte [] aux = Common.intToArray(port);
        
        byte [] expResult = {0, dataFlow, 'C', 'H', 'N', 'G', '_',
                             'D', 'F', '_', 'R', 'E', 'S', 'P', 
                             aux [0], aux[1], aux[2], aux[3],
                             proposedFlow};
        
        DatagramPacket result = PacketCreator.CHNG_DF_RESP(dataFlow,
                                                           accepted,
                                                           proposedFlow,
                                                           port);
        
        assertArrayEquals(expResult, result.getData());
        
        System.out.println("newCHNG_DF_RESP - Accepted proposal");
        accepted = 1;
        expResult = new byte [] {0, dataFlow, 'C', 'H', 'N', 'G', '_',
                                'D', 'F', '_', 'R', 'E', 'S', 'P',
                                aux [0], aux[1], aux[2], aux[3]};
        
        result = PacketCreator.CHNG_DF_RESP(dataFlow,
                                            accepted,
                                            proposedFlow,
                                            port);
        
        assertArrayEquals(expResult, result.getData());
    }

    /**
     * Test of PLAIN method, of class PacketCreator.
     */
    @Test
    public void testNewPLAIN() {
        System.out.println("newPLAIN");
        
        byte dataFlow = 1;
        String message = "This is a test message.\n";
        byte[] plaintext = message.getBytes();
        
        byte[] aux = ControlMessage.PLAIN.toString().getBytes();
        byte [] auxPort = Common.intToArray(port);
        
        byte [] expResult = new byte [ControlMessage.PLAIN.getLength()
                                        + plaintext.length + 4];
        expResult[0] = 0;
        expResult[1] = dataFlow;
        
        System.arraycopy(aux, 0, expResult, 2, aux.length);
        System.arraycopy(auxPort, 0, expResult, 2 + aux.length, auxPort.length);
        System.arraycopy(plaintext, 0, expResult,
                         auxPort.length + aux.length + 2, plaintext.length);
        
        DatagramPacket result = PacketCreator.PLAIN(dataFlow,
                                                    plaintext,
                                                    port);
        
        assertArrayEquals(expResult, result.getData());
    }
    
}

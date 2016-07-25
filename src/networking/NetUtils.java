/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking;

import common.Common;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * This class has some methods to ease some tasks as getting the network 
 * interfaces address.
 */
public class NetUtils {

    /**
     * Gets the addresses of all the active interfaces.
     *
     * @return
     *              A list with all the addresses of the active interfaces.
     */
    public static ArrayList<InetAddress> getInterfaces() {
        ArrayList<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            /* Adds the wildcard address -> 0.0.0.0 */
            addresses.add(new InetSocketAddress(0).getAddress());
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                /* The inactive interfaces are omitted */
                if (!iface.isUp()) {
                    continue;
                }
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    addresses.add(addr.getAddress());
                }
            }
        } catch (SocketException ex) {
            Common.logger.logError("Exception at Common.getInterfaces(): " + ex.getMessage());
        }
        return addresses;
    }
    
}

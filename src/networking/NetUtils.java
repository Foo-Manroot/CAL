/*
 * CAL.
 *  A P2P chat program that lets you communicate without any infrastructure.
 *
 *   Copyright (C) 2015  Foo-Manroot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package networking;

import common.Common;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
            /* Adds the wildcard addresses -> 0.0.0.0 and 0:0:0:0:0:0:0:0 */
            addresses.add(new InetSocketAddress(0).getAddress());
            addresses.add(InetAddress.getByName("::"));
            
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
        } catch (SocketException | UnknownHostException ex) {
            Common.logger.logError("Exception at Common.getInterfaces(): " + ex.getMessage());
        }
        return addresses;
    }
    
}

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
package gui;

import static common.Common.logger;

import common.Common;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import peer.Host;

/**
 * GUI application that shows a little dialog to set the connection settings.
 */
public class ConnectionDialog {
    
    private static Host host;
    
    /**
     * Shows a dialog asking for the needed parameters to create a new host.
     * 
     * @param resources 
     *              Resource bundle with the strings to put on this dialog.
     * 
     * @return 
     *              A host with all the necessary information, or <i>null</i> 
     *          if the action has been cancelled.
     */
    public static Host showInputDialog (ResourceBundle resources) {
                
        Tooltip toolTip;
        GridPane layout = new GridPane();
        layout.setMinSize(365, 191);
        
        Stage stage = new Stage();
        
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle (resources.getString("conn_settings_title"));
        
        /* Label and text area for the IP address */
        Label ipAddrLabel = new Label (resources.getString("conn_settings_IPAddr"));
        
        TextField ipAddrAux = new TextField();
        
        toolTip = new Tooltip(resources.getString("conn_settings_IPAddr_toolTip"));
        ipAddrLabel.setTooltip(toolTip);
        
        /* Label and spinner for the connection port */
        Label portLabel = new Label(resources.getString("conn_settings_port"));
        
        Spinner portAux = new Spinner(0, 65535, 2000, 1);
        portAux.setEditable(true);
        
        toolTip = new Tooltip(resources.getString("conn_settings_port_toolTip"));
        portLabel.setTooltip(toolTip);
        
        /* Label and spinner for the chat room ID */
        Label chatRoomIDLabel = new Label(resources.getString("conn_settings_chatRoom"));

        Spinner chatRoomAux = new Spinner(-128, 127, -128, 1);
        chatRoomAux.setEditable(true);
        
        toolTip = new Tooltip(resources.getString("conn_settings_chatRoom_toolTip"));
        chatRoomIDLabel.setTooltip(toolTip);
        
        /* "Accept" and "cancel" buttons */
        Button acceptButton = new Button(resources.getString("accept_button"));
        Button cancelButton = new Button(resources.getString("cancel_button"));

        /* Sets the action on each button */
        acceptButton.setOnAction(e -> {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            
            /* If the host has been successfully created, closes the window. 
            If not, shows a warning message */
            if (createHost(ipAddrAux.getText(), portAux, chatRoomAux)) {
                
                stage.close();
            } else {
                
                alert.setContentText(resources.getString("conn_settings_error"));
                alert.show();
            }
        });
        
        cancelButton.setOnAction(e -> {
            
            /* Sets the attribute to null and closes the window */
            host = null;
            stage.close();
        });
        
        stage.setOnCloseRequest(e -> {
        
            e.consume();
            /* Sets the attribute to null and closes the window */
            host = null;
            stage.close();
        });

        /* Adds everything to the layout and shows the window */        
        layout.addRow(0, ipAddrLabel, ipAddrAux);
        layout.addRow(1, portLabel, portAux);
        layout.addRow(2, chatRoomIDLabel, chatRoomAux);
        layout.addRow(3, acceptButton, cancelButton);
        
        layout.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(layout);
        
        stage.setScene(scene);
        stage.showAndWait();

        /* Returns the new host */
        return host;
    }
    
    /**
     * Tries to initialise the attribute {@code host} with the information on 
     * the strings. If any data is not correct, returns <i>false</i>. If the 
     * host has been successfully created, returns <i>true</i>.
     */
    private static boolean createHost (String textIP, Spinner port, Spinner chatRoom) {
        
        /* Regular expression to validate an IPv4 and IPv6 addresses */
        String regexIPv4 = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
                         + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        /* (From David M. Syzdek's answer on 
        https://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses#53500) */
        String regexIPv6 = "(" +
                            "([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|" +
                            "([0-9a-fA-F]{1,4}:){1,7}:|" +
                            "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                            "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
                            "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                            "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
                            "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
                            "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
                            ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
                            "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                            "::(ffff(:0{1,4}){0,1}:){0,1}" +
                            "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
                            "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" +
                            "([0-9a-fA-F]{1,4}:){1,4}:" +
                            "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
                            "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])" +
                            ")";
        int index;
        
        int portAux = (int) port.getValue();
        byte chatRoomAux = (byte) ((int) chatRoom.getValue());
        byte [] IPaddr = new byte [4];
        
        /* Checks the IP text syntax */
        if (
            (
            !textIP.matches(regexIPv4) &&
            !textIP.matches(regexIPv6)
            ) ||
            (chatRoomAux == Common.RESERVED_DATA_FLOW)
            ) {
            
            return false;
        }
        
        try {
            /* Creates a new Host */
            host = new Host (InetAddress.getByName(textIP),
                             portAux,
                             chatRoomAux);
            
        } catch (UnknownHostException ex) {
            
            logger.logError("Exception at ConnectionDialog.createHost(): " + 
                            ex.getMessage());
            return false;
        }
        
        
        return true;
    }
}

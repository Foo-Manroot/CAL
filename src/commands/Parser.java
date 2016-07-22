/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commands;

import static common.Common.logger;
import static gui.PeerGUI.peer;

import common.Common;
import java.util.ArrayList;
import peer.Host;

/**
 * This class implements some methods to support the execution of some commands.
 */
public class Parser {
    
    /**
     * Tries to determine if the given string can be a bit of any known command,
     * even if the exact one is unknown.
     *
     * @param portion
     *              The known portion of the possible command (<b>with</b> the
     *          escape character).
     *
     *
     * @return
     *              <i>true</i> if <b>at least one</b> command starts with the
     *          given string.
     */
    public static boolean isCommand (String portion) {

        String input;

        /* Checks the string (if it's empty, has the escape character...) */
        if (portion.isEmpty() ||
            portion.trim().length() <= 1 ||
            !portion.trim().startsWith(String.valueOf(Common.escapeChar))) {

            return false;
        }

        input = portion.trim().substring(1);

        /* Gets all the possible values on the complete list of commands */
        for (Command c : Command.values()) {

            if (c.name().toUpperCase().startsWith (input.toUpperCase())) {

                return true;
            }
        }

        return false;
    }
    
    /**
     * Prints a list with all the known hosts.
     */
    public static void showHostsList () {
        
        StringBuilder message = new StringBuilder();
        ArrayList<Host>  aux;
        
        message.append("\n-------------------------");
        message.append("\nAll hosts by room: \n");
        
        /* Goes through all the rooms, printing all the hosts in each one */
        for (byte room = Byte.MIN_VALUE; room < Byte.MAX_VALUE; room++) {
            
            aux = peer.getHostsList().search(room);
            
            if (!aux.isEmpty()) {
                
                message.append("\n->Hosts on room ")
                        .append(room)
                        .append(": \n");
                
                /* Prints all the hosts on the room */
                for (Host h : aux) {
                    
                    message.append("·")
                            .append(h.toString());
                }
            }
        }
        
        /* Does the same with the only left value (omitted on the loop to avoid
        overflow and, therefore, an infinite loop) */
        aux = peer.getHostsList().search(Byte.MAX_VALUE);
            
        if (!aux.isEmpty()) {

            message.append("\n->Hosts on room ")
                    .append(Byte.MAX_VALUE)
                    .append(": \n");

            /* Prints all the hosts on the room */
            for (Host h : aux) {

                message.append("·")
                        .append(h.toString());
            }
        }
        
        message.append("-------------------------\n");
        
        /* Prints the message */
        logger.logMsg(new String (message));
    }

/* ------------------------------- */
/* ---- END OF STATIC METHODS ---- */
/* ------------------------------- */

    /**
     * Tries to complete the command from the given string.
     *
     *
     * @param portion
     *              The known portion of the possible command (<b>without</b>
     *          the escape character).
     *
     *
     * @return
     *              The string portion to complete the command, or
     *          {@code "UNKNOWN"}, if no command could be correctly predicted.
     *
     *          <p>
     *          Every returned string is in <b>LOWERCASE LETTERS</b>.
     */
    public String completeCommand (String portion) {
        
        ArrayList<Command> possibilities = new ArrayList<>();
        Command completed;

        /* Gets all the possible values on the complete list of commands */
        for (Command c : Command.values()) {

            if (c.name().toUpperCase().startsWith(portion.toUpperCase())) {

                possibilities.add(c);
            }
        }

        /* If the list has only one possibility, returns it; if not, returns
        the UNKNOWN command */
        if (possibilities.size() == 1) {

            /* Arranges the string so the returned element completes the
            already introduced string */
            completed = possibilities.get(0);

            /* Returns only the completed portion */
            return completed.name().substring(
                                            portion.length(),
                                            completed.name().length()
                                            ).toLowerCase();
        }

        return Command.UNKNOWN.name().toLowerCase();
    }

    /**
     * Tries to execute the given command.
     *
     * @param command
     *              The command command to execute.
     *
     * @return
     *              <i>true</i> if the command has been executed correctly;
     *          <i>false</i> if any problem occurred or the command was unknown.
     */
    public boolean executeCommand (Command command) {

        byte chatRoom = Common.currentRoom;
        
        if (command.equals(Command.UNKNOWN)) {
            
            return false;
        }
        
        Common.logger.logWarning("Command executed: " + command.name() + "\n");

        switch (command) {

            case HELP:

                HELP ();
                return true;

            case HOSTS:
                
                HOSTS (chatRoom);
                return true;
                
            case LEAVE:
                
                return LEAVE (chatRoom);
                
            case EXIT:
                return EXIT ();

            default:
                return false;
        }
    }
    
    /**
     * Prints a list with all the known hosts on the room.
     * 
     * @param chatRoom
     *              The id of the chat room to show.
     */
    private void HOSTS (byte chatRoom) {
        
        StringBuilder message = new StringBuilder();
        ArrayList<Host> aux = peer.getHostsList().search(chatRoom);
            
        message.append("-------------------------\n");
        
        if (!aux.isEmpty()) {

            message.append("\n-Hosts on current room (")
                    .append(chatRoom)
                    .append("): \n");

            /* Prints all the hosts on the room */
            for (Host h : aux) {

                message.append("·")
                        .append(h.toString());
            }
        }
        
        message.append("-------------------------\n");
        
        /* Prints the message */
        logger.logMsg(new String (message), chatRoom);
    }
    
    /**
     * Prints a list with all the available commands.
     */
    private void HELP () {
        
        StringBuilder message = new StringBuilder();
        
        message.append("\n-------------------------");
        message.append("\nHelp message: ")
                .append("\n\tTo execute a command, use the escape character, '")
                    .append(Common.escapeChar)
                    .append("', and type the desired command. To try to "
                            + "automatically complete the command, use the "
                            + "TAB key.\n");
        
        /* Prints the available commands */
        message.append("\nList of available commands: \n");
        
        for (Command c : Command.values()) {
            
            if (!c.equals(Command.UNKNOWN)) {
                
                /* Prints the command and its description */
                message.append("\t")
                        .append(Common.escapeChar)
                        .append(c.name().toLowerCase())
                        .append(": ")
                        .append(c.getDescription())
                        .append("\n");
            }
        }
        
        message.append("-------------------------\n");
        
        /* Prints the message */
        logger.logMsg(new String (message));
    }
    
    /**
     * Closes all active connections with every peer.
     */
    private boolean EXIT () {
        
        logger.logMsg("Exiting all rooms...\n");
        
        return peer.disconnect();
    }
    
    /**
     * Closes all the active connections with the peers on this room.
     * 
     * @param chatRoom 
     *              The ID of the room to leave.
     */
    private boolean LEAVE (byte chatRoom) {
        
        logger.logMsg("Leaving the room...\n");

        return peer.leaveChatRoom(chatRoom);
    }
}

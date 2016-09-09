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
package commands;

import static common.Common.logger;
import static gui.main.PeerGUI.peer;

import common.Common;
import files.FileSharer;
import gui.files.FileShareGUI;
import gui.main.PeerGUI;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import peer.Host;

/**
 * This class implements some methods to support the execution of some commands.
 */
public class Parser {

    /**
     * Number of times that the "tab" key has been pressed without a correctly
     * guessed command to be completed. That means that more (of none) commands
     * fits the starting portion of text.
     *
     * <p>
     * If this counter reaches 2, the method {@code Parser.completeCommand()}
     * should show a list with all the possible commands, if necessary.
     */
    private int tabsCount = 0;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */

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
        StringBuilder suggestionMsg = new StringBuilder();

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

            /* Resets the counter */
            tabsCount = 0;

            /* Returns only the completed portion */
            return completed.name().substring(
                                            portion.length(),
                                            completed.name().length()
                                            ).toLowerCase();
        }

        /* If there are more possibilities and the counter has reached the
        limit, shows the list with all the possibilities */
        if (possibilities.size() > 1 &&
            ++tabsCount >= 2) {

            suggestionMsg.append("\n-------------------------\n");
            
            suggestionMsg.append("Possible commands: \n");
            
            for (Command c : possibilities) {
                
                suggestionMsg.append(Common.escapeChar)
                                .append(c.name().toLowerCase())
                                .append("\t\t");
            }
            
            suggestionMsg.append("\n-------------------------\n");
            
            logger.logMsg(new String (suggestionMsg), Common.currentRoom);
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

        logger.logWarning("Command executed: " + command.name() + "\n");

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
                
            case SEND:
                return SEND (chatRoom);

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
        
        boolean retVal;

        logger.logMsg("Exiting all rooms...\n");
        
        
        retVal = peer.disconnect();
        
        logger.logMsg("Completed.\n");

        return retVal;
    }

    /**
     * Closes all the active connections with the peers on this room.
     *
     * @param chatRoom
     *              The ID of the room to leave.
     */
    private boolean LEAVE (byte chatRoom) {

        boolean retVal;
        
        logger.logMsg("Leaving the room...\n", chatRoom);

        retVal = peer.leaveChatRoom(chatRoom);
        
        logger.logMsg("Completed.\n", chatRoom);
        
        return retVal;
    }
    
    
    /**
     * Shows a window to select the file to send.
     * 
     * @param chatRoom
     *              The ID of the room where the receiver hosts are.
     */
    private boolean SEND (byte chatRoomID) {
        
        ThreadPoolExecutor pool;
    
        /* Creates up to 20 threads */
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool (20);
        
        File selectedFile = FileShareGUI.selectFile();
        
        if (selectedFile == null) {
            
            return true;
        }
        
        if (!selectedFile.exists()) {
            
            logger.logError("The selected file doesn't exists.\n");
            return false;
        }
        
        /* Sends the file to all the hosts on the room */
        for (Host h : PeerGUI.peer.getHostsList().search(chatRoomID)) {
            
            /* Creates a thread to wait for the destination to accept
            the transfer */
            FileSharer sharer = new FileSharer (selectedFile.getAbsolutePath(),
                                                PeerGUI.peer,
                                                h);

            pool.execute (sharer);
        }
        
        return true;
    }
}

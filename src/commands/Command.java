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

/**
 * Enumeration of all the supported commands.
 */
public enum Command {

    /**
     * Prints a little help on the screen.
     *//**
     * Prints a little help on the screen.
     */
    HELP ("Prints a little help message on the screen and exits."),

    /**
     * Shows a list with all the hosts currently on the room (or in every room,
     * depending on the given arguments).
     */
    HOSTS ("Shows a list with information about the hosts. If no parameters "
            + "are given, it shows only information about the hosts on the "
            + "current room. To show all the rooms, the parameter \"all\" "
            + "must be added."),

    /**
     * Closes all the active connections with the peers on this room.
     */
    LEAVE ("Leaves the current chat room."),
    
    /**
     * Closes all active connections with every peer.
     */
    EXIT ("Disconnects the user from all the rooms."),
    
    
    /**
     * Opens a window to select a file to send.
     */
    SEND ("Opens a window to select a file to send."),


    UNKNOWN ("Unknown command.");

/* ------------------------------------------ */
/* ---- END OF ENUM ELEMENTS DECLARATION ---- */
/* ------------------------------------------ */

    private final String description;

/* ----------------- */
/* ---- METHODS ---- */
/* ----------------- */

    /**
     * Constructor.
     *
     * @param description
     *              A description for the command to show when needed.
     */
    private Command (String description) {

        this.description = description;
    }
    
    /**
     * Returns the available description to get information about the command.
     * 
     * @return 
     *              The value of {@code description}
     */
    public String getDescription () {
        
        return description;
    }

    /**
     *  Returns the value that corresponds with the given string. 
     * 
     * <p>
     * Case <b>insensitive.</b>
     * 
     * 
     * @param value
     *              The string whose value represents any command.
     * 
     * 
     * @return
     *              The correspondent command, or {@code Command.UNKNOWN}, if 
     *          the given string doesn't corresponds to any command.
     */
    public static Command getCommand (String value) {
        
        for (Command c : values()) {
            
            if (c.name().equalsIgnoreCase(value)) {
                
                return c;
            }
        }
        
        return UNKNOWN;
    }
}

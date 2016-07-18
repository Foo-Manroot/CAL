/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commands;

/**
 * Enumeration of all the supported commands.
 */
public enum Command {
    
    /**
     * Prints a little help on the screen.
     */
    HELP ("Prints a little help message on the screen and exits."),
    
    /**
     * Shows a list with all the hosts currently on the room.
     */
    HOSTS ("Shows a list with all the hosts currently on the room."),
    
    
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

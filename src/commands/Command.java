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
    
    HELP,
    UNKNOWN;
    
/* ----------------- */    
/* ---- METHODS ---- */
/* ----------------- */ 

    /**
     *  Returns the value that corresponds with the given string. 
     * 
     * <p>
     * Case insensitive.
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

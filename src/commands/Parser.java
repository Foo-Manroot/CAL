/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commands;

import common.Common;
import java.util.ArrayList;

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
            portion.length() <= 1 ||
            !portion.startsWith(String.valueOf(Common.escapeChar))) {
            
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
        
        Common.logger.logWarning("Command executed: " + command.name() + "\n");
        
        switch (command) {
            
            case HELP:
                
                Common.logger.logMsg("Help message..."
                                     + "\nBla bla bla"
                                     + "\n");
                return true;
                
                
            default:
                return false;
        }
    }
}

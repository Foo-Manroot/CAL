package common;

import gui.PeerGUI;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import peer.Host;

/**
 *  Implements some methods to make the logging task easier
 */
public class Logger {

    /**
     * Text areas where the messages will be written
     */
    private final ConcurrentLinkedQueue<TextFlow> textAreasMSG;
    
    /**
     * Text areas where the error messages will be written
     */
    private final ConcurrentLinkedQueue<TextFlow> textAreasERROR;
    
    /**
     * Text areas where the warning messages will be written
     */
    private final ConcurrentLinkedQueue<TextFlow> textAreasWARNING;
    
    /**
     * Streams where the messages will be written
     */
    private final ConcurrentLinkedQueue<OutputStream> streamsMSG;
    
    /**
     * Streams where the error messages will be written
     */
    private final ConcurrentLinkedQueue<OutputStream> streamsERROR;
    
    /**
     * Streams where the warning messages will be written
     */
    private final ConcurrentLinkedQueue<OutputStream> streamsWARNING;
    
    /**
     * Files where the messages will be written
     */
    private final ConcurrentLinkedQueue<File> filesMSG;
    
    /**
     * Files where the error messages will be written
     */
    private final ConcurrentLinkedQueue<File> filesERROR;
    
    /**
     * Files where the warning messages will be written
     */
    private final ConcurrentLinkedQueue<File> filesWARNING;
    
    
    /**
     * Only one instance of this object allowed, so no interferences between 
     * loggers can be made.
     */
    private static Logger instance;
    
    
    /**
     * List of all the known hosts and their identifier colours.
     * 
     * <p>
     * Used to set different colours to different hosts.
     * 
     * <pre>
     * Key ->  Host
     * Value -> Colour
     * </pre>
     */
    private final ConcurrentHashMap<Host, Color> hostsColours;
    
    /**
     * Last host that sent a message.
     */
    private Host lastSender = null;
    
/* -------------------------------------- */
/* ---- END OF ATTRIBUTE DECLARATION ---- */
/* -------------------------------------- */
    
    /**
     * Constructor.
     * 
     * <p>
     * Adds {@code STDERR} and {@code STDOUT} to the streams.
     */
    private Logger () {
        
        textAreasMSG = new ConcurrentLinkedQueue<>();
        textAreasERROR = new ConcurrentLinkedQueue<>();
        textAreasWARNING = new ConcurrentLinkedQueue<>();
        
        streamsMSG = new ConcurrentLinkedQueue<>();
        streamsERROR = new ConcurrentLinkedQueue<>();
        streamsWARNING = new ConcurrentLinkedQueue<>();
        
        filesMSG = new ConcurrentLinkedQueue<>();
        filesERROR = new ConcurrentLinkedQueue<>();
        filesWARNING = new ConcurrentLinkedQueue<>();
        
        hostsColours = new ConcurrentHashMap<>();
        
        /* Adds STDOUT and STDERR */
        streamsERROR.add(new DataOutputStream(System.err));
        streamsWARNING.add(new DataOutputStream(System.err));
        streamsMSG.add(new DataOutputStream(System.out));
    }
    
    /**
     * Initialises the only allowed instance of this object and returns it.
     * If an instance already existed, returns it.
     * 
     * @return 
     *              The only allowed instance.
     */
    public static Logger newLogger () {
        
        if (instance == null) {
            
            return (instance = new Logger());
        }
        
        return instance;
    }
    
    /**
     * Chooses a new colour randomly.
     * 
     * @return 
     *              A new dark colour.
     */
    public static Color chooseColour () {
        
        double r, g, b;
        
        /* Chooses a dark colour, so it can be seen on the white background */
        do {
            
            r = Math.random();
            g = Math.random();
            b = Math.random();
            
        } while ((r + g + b) >= 1.5);
        
        return new Color(r, g, b, 1);
    }
    
/* ------------------------- */
/* ---- LOGGING METHODS ---- */
/* ------------------------- */  
    
    /**
     * Prints the given message in all the elements belonging 
     * {@code streamsMSG}, {@code filesMSG} and {@code textAreasMSG}.
     * 
     * @param message 
     *              Message to be printed.
     */
    public void logMsg (String message) {
        
        BufferedWriter output;
        String textAreaID;
        
        /* The sender is unknown, so the attribute is set to "null" */
        lastSender = null;
        
        for (OutputStream outStr : streamsMSG) {
            
            try {
                output = new BufferedWriter(new OutputStreamWriter(outStr));
                output.write(message);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (File outFile : filesMSG) {
            
            try {
                output = new BufferedWriter(new FileWriter(outFile));
                output.write(message);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (TextFlow outArea : textAreasMSG) {
            
            /* Only appends the message on the text areas with the proper ID */
            textAreaID = outArea.getId();
            
            /* The ID of the text area should be "msgTextAreaX", being 'X' the
            data flow ID */
            if (textAreaID != null && textAreaID.startsWith("msgTextArea")) {
                
                /* Notifies the GUI thread to add the text */
                Platform.runLater(() -> {
                    
                    Text text = new Text(message);
                    outArea.getChildren().add(text);
                });
            }
        }
    }
    
    /**
     * Prints the given message in all the elements belonging 
     * {@code streamsMSG}, {@code filesMSG} and {@code textAreasMSG}.
     * 
     * 
     * @param message 
     *              Message to be printed.
     * 
     * @param host 
     *              Host that originated the  message.
     * 
     * @param format 
     *              If <i>true</i>, formats the given text so the sender can be 
     *          displayed correctly. The format is the following:
     *      <pre>
     *      SenderAddress:SenderPort:
     *               Message
     *               Message
     *               (...)
     *      </pre>
     */
    public void logMsg (String message, Host host, boolean format) {
        
        BufferedWriter output;
        String textAreaID;
        byte dataFlowAux;
        String msg;
        
        if (format) {
            /* If the sender is the same as the one who sent the last message,
            just prints the message on a new line. */
            if (lastSender != null &&
                host.getDataFlow() == lastSender.getDataFlow() &&
                host.getIPaddress().equals(lastSender.getIPaddress()) &&
                host.getPort() == lastSender.getPort()) {

                msg = message;
            } else {
                
                msg = host.getIPaddress() + ":" + host.getPort() 
                      + "\n\t" + message;
                
                /* Updates the last sender */
                lastSender = host;
            }
        } else {
            
            msg = message;
        }
        
        
        for (OutputStream outStr : streamsMSG) {
            
            try {
                output = new BufferedWriter(new OutputStreamWriter(outStr));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (File outFile : filesMSG) {
            
            try {
                output = new BufferedWriter(new FileWriter(outFile));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (TextFlow outArea : textAreasMSG) {
            
            /* Only appends the message on the text areas with the proper ID */
            textAreaID = outArea.getId();
            
            /* The ID of the text area should be "msgTextAreaX", being 'X' the
            data flow ID */
            if (textAreaID != null && textAreaID.startsWith("msgTextArea")) {
                
                /* Checks the data flow ID */
                dataFlowAux = Byte.valueOf(
                                textAreaID.substring(
                                            "msgTextArea".length(),
                                            textAreaID.length()));
                
                if (host.getDataFlow() == dataFlowAux) {
                    
                    /* Notifies the GUI thread to add the text */
                    Platform.runLater(() -> {
                        
                        Color colour;
                        /* Adds a context menu so a new connection can be 
                        done with the selected host */
                        MenuItem connectMenu = new MenuItem(
                                ResourceBundle.getBundle(Common.resourceBundle)
                                        .getString("private_conv_menu")
                                );
                        
                        connectMenu.setOnAction(e -> {
                            
                            Alert alert = new Alert (Alert.AlertType.ERROR);
                            String text = ResourceBundle
                                            .getBundle(Common.resourceBundle)
                                            .getString("error_private_conv");
                            
                            /* Starts a new conversation */
                            if (!PeerGUI.peer.startConversation(host)) {
                                
                                logError(text);
                                alert.setContentText(text);
                                alert.show();
                            }
                        });
                        
                        ContextMenu context = new ContextMenu(connectMenu);
                        Text text;
                        
                        /* Searches the host on the list to get its colour. If
                        it wasn't there, adds it. */
                        if (!hostsColours.containsKey(host)) {

                            colour = addHost(host);
                        } else {

                            colour = hostsColours.get(host);
                        }
                        
                        text = new Text(msg);
                        
                        text.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                            
                            if (e.getButton() == MouseButton.SECONDARY) {
                                
                                context.show(text, e.getScreenX(), e.getScreenY());
                            }
                        });
                        
                        text.setFill(colour);
                        
                        outArea.getChildren().add(text);
                    });
                }
            }
        }
    }
    
    /**
     * Prints the given message in all the elements belonging 
     * {@code streamsMSG}, {@code filesMSG} and {@code textAreasMSG}.
     * 
     * @param message 
     *              Message to be printed.
     * 
     * @param dataFlow 
     *              Data flow at which this message belongs to.
     */
    public void logMsg (String message, byte dataFlow) {
        
        BufferedWriter output;
        String textAreaID;
        byte dataFlowAux;
        
        /* The sender is unknown, so the attribute is set to "null" */
        lastSender = null;
        
        for (OutputStream outStr : streamsMSG) {
            
            try {
                output = new BufferedWriter(new OutputStreamWriter(outStr));
                output.write(message);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (File outFile : filesMSG) {
            
            try {
                output = new BufferedWriter(new FileWriter(outFile));
                output.write(message);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logMSG(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (TextFlow outArea : textAreasMSG) {
            
            /* Only appends the message on the text areas with the proper ID */
            textAreaID = outArea.getId();
            
            /* The ID of the text area should be "msgTextAreaX", being 'X' the
            data flow ID */
            if (textAreaID != null && textAreaID.startsWith("msgTextArea")) {
                
                /* Checks the data flow ID */
                dataFlowAux = Byte.valueOf(
                                textAreaID.substring(
                                            "msgTextArea".length(),
                                            textAreaID.length()));
                
                if (dataFlow == dataFlowAux) {
                    
                    /* Notifies the GUI thread to add the text */
                    Platform.runLater(() -> {
                    
                        Text text = new Text(message);
                        outArea.getChildren().add(text);
                    });
                }
            }
        }
    }
    
    /**
     * Prints the given message in all the elements belonging
     * {@code streamsWARNING} and {@code textAreasWARNING}.
     * 
     * @param message 
     *              Message to be printed.
     */
    public void logWarning (String message) {
        
        BufferedWriter output;
        String msg = message + "---------------------\n";
        
        for (OutputStream outStr : streamsWARNING) {
            
            try {
                output = new BufferedWriter(new OutputStreamWriter(outStr));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logWarning(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (File outFile : filesWARNING) {
            
            try {
                output = new BufferedWriter(new FileWriter(outFile));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logError("IOException at Logger.logWarning(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (TextFlow outArea : textAreasWARNING) {
            
            /* Notifies the GUI thread to add the text */
            Platform.runLater(() -> {
                    
                Text text = new Text(msg);
                outArea.getChildren().add(text);
            });
        }
    }
        
    /**
     * Prints the given message in all the elements belonging
     * {@code streamsERROR} and {@code textAreasERROR}.
     * 
     * <p>
     * Before the actual message appends the current date.
     * 
     * @param message 
     *              Message to be printed.
     */
    public void logError (String message) {
        
        BufferedWriter output;
        String msg = "\n" + new Date().toString() + "\n" + message;
        
        for (OutputStream outStr : streamsERROR) {
            
            try {
                output = new BufferedWriter(new OutputStreamWriter(outStr));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logWarning("IOException at Logger.logERROR(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (File outFile : filesERROR) {
            
            try {
                output = new BufferedWriter(new FileWriter(outFile));
                output.write(msg);
                output.flush();
                
            } catch (IOException ex) {
                
                logWarning("IOException at Logger.logERROR(): " 
                        + ex.getMessage() + "\n");  
            }
        }
        
        for (TextFlow outArea : textAreasERROR) {
                        
            /* Notifies the GUI thread to add the text */
            Platform.runLater(() -> {

                Text text = new Text(msg);
                text.setFill(Color.RED);
                
                outArea.getChildren().add(text);
            });
        }
    }
    
    
    /**
     * Adds a new host and the colour that will represent it when it's printed
     * on the text area.
     * 
     * @param host 
     *              The host to which this colour will be linked.
     * 
     * @param colour 
     *              The colour with which the text host's name will be printed.
     */
    public void addHost (Host host, Color colour) {
     
        hostsColours.put(host, colour);
    }
    
    /**
     * Adds a new host and the colour that will represent it when it's printed
     * on the text area. The colour will be chosen randomly.
     * 
     * @param host 
     *              The host to which this colour will be linked.
     * 
     * @return
     *              The colour with which the text host's name will be printed.
     */
    public Color addHost (Host host) {
        
        Color colour = chooseColour();
        
        hostsColours.put(host, colour);
        
        return colour;
    }
    
    
    /**
     * Adds the given stream on the proper lists, depending on the parameter
     * {@code group}.
     * 
     * @param stream 
     *              Stream to be added.
     * 
     * @param group 
     *              This code determines the list (or lists) where the stream 
     *          will be added. The possible values are:
     *  <pre>
     *      2: <i>streamsMSG</i>
     *      3: <i>streamsERROR</i>
     *      5: <i>streamsWARNING</i>
     *  </pre>
     *              If the element must be added to different lists, the 
     *          parameter {@code group} should be a value divisible by one
     *          of the previous codes.
     * 
     *            <p>
     *              Example: let's say that the stream should be added to 
     *          <i>streamsERROR</i> and <i>streamsWARNING</i>. In this case, the
     *          value of {@code group} must be <i>3 * 5 = <b>15</b></i>.
     */
    public void addStream (OutputStream stream, int group) {
        
        int msgCode = 2;
        int errorCode = 3;
        int warningCode = 5;
        
        if ((group % msgCode) == 0) {
            
            streamsMSG.add(stream);
        }
        
        if ((group % errorCode) == 0) {
            
            streamsERROR.add(stream);
        }
        
        if ((group % warningCode) == 0) {
            
            streamsWARNING.add(stream);
        }
    }
    
    /**
     * Adds the given JTextArea on the proper lists, depending on the parameter
     * {@code group}.
     * 
     * @param textArea  
     *              JTextArea to be added.
     * 
     * @param group 
     *              This code determines the list (or lists) where the stream 
     *          will be added. The possible values are:
     *  <pre>
     *      2: <i>textAreasMSG</i>
     *      3: <i>textAreasERROR</i>
     *      5: <i>textAreasWARNING</i>
     *  </pre>
     *              If the element must be added to different lists, the 
     *          parameter {@code group} should be a value divisible by one
     *          of the previous codes.
     * 
     *            <p>
     *              Example: let's say that the text area should be added to 
     *          <i>textAreasERROR</i> and <i>textAreasWARNING</i>. In this case,
     *          the value of {@code group} must be <i>3 * 5 = <b>15</b></i>.
     */
    public void addTextArea (TextFlow textArea, int group) {
        
        int msgCode = 2;
        int errorCode = 3;
        int warningCode = 5;
        
        if ((group % msgCode) == 0) {
            
            textAreasMSG.add(textArea);
        }
        
        if ((group % errorCode) == 0) {
            
            textAreasERROR.add(textArea);
        }
        
        if ((group % warningCode) == 0) {
            
            textAreasWARNING.add(textArea);
        }
    }
    
    
    /**
     * Adds the given file on the proper lists, depending on the parameter
     * {@code group}.
     * 
     * @param file  
     *              File to be added.
     * 
     * @param group 
     *              This code determines the list (or lists) where the stream 
     *          will be added. The possible values are:
     *  <pre>
     *      2: <i>filesMSG</i>
     *      3: <i>filesERROR</i>
     *      5: <i>filesWARNING</i>
     *  </pre>
     *              If the element must be added to different lists, the 
     *          parameter {@code group} should be a value divisible by one
     *          of the previous codes.
     * 
     *            <p>
     *              Example: let's say that the file should be added to 
     *          <i>filesERROR</i> and <i>filesWARNING</i>. In this case, the
     *          value of {@code group} must be <i>3 * 5 = <b>15</b></i>.
     */
    public void addFile (File file, int group) {
        
        int msgCode = 2;
        int errorCode = 3;
        int warningCode = 5;
        
        if ((group % msgCode) == 0) {
            
            filesMSG.add(file);
        }
        
        if ((group % errorCode) == 0) {
            
            filesERROR.add(file);
        }
        
        if ((group % warningCode) == 0) {
            
            filesWARNING.add(file);
        }
    }
    
/* ----------------------------- */
/* ---- GETTERS AND SETTERS ---- */
/* ----------------------------- */

    /**
     * Returns a list with all the text areas where normal messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code textAreasMSG}
     */
    public ConcurrentLinkedQueue<TextFlow> getTextAreasMSG() {
        
        return textAreasMSG;
    }

    /**
     * Returns a list with all the text areas where error messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code textAreasERROR}
     */
    public ConcurrentLinkedQueue<TextFlow> getTextAreasERROR() {
        
        return textAreasERROR;
    }

    /**
     * Returns a list with all the text areas where warning messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code textAreasWARNING}
     */
    public ConcurrentLinkedQueue<TextFlow> getTextAreasWARNING() {
        
        return textAreasWARNING;
    }

    /**
     * Returns a list with all the streams where normal messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code streamsMSG}
     */
    public ConcurrentLinkedQueue<OutputStream> getStreamsMSG() {
        
        return streamsMSG;
    }

    /**
     * Returns a list with all the streams where error messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code streamsERROR}
     */
    public ConcurrentLinkedQueue<OutputStream> getStreamsERROR() {
        
        return streamsERROR;
    }

    /**
     * Returns a list with all the streams where warning messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code streamsWARNING}
     */
    public ConcurrentLinkedQueue<OutputStream> getStreamsWARNING() {
        
        return streamsWARNING;
    }

    /**
     * Returns a list with all the files where normal messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code filesMSG}}
     */
    public ConcurrentLinkedQueue<File> getFilesMSG() {
        
        return filesMSG;
    }

    /**
     * Returns a list with all the files where error messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code filesERROR}}
     */
    public ConcurrentLinkedQueue<File> getFilesERROR() {
        
        return filesERROR;
    }

    /**
     * Returns a list with all the files where warning messages will be 
     * written.
     * 
     * @return 
     *              The value of {@code filesWARNING}}
     */
    public ConcurrentLinkedQueue<File> getFilesWARNING() {
        
        return filesWARNING;
    }
}

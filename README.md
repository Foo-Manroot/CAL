# CAL
  Chat between peers, without any other infrastructure.
  
  Created by [Foo-Manroot](https://github.com/Foo-Manroot).

# Functionalities
  This chat, written in Java, has only one _.jar_ file where the functionalities for a server and a client are implemented. That means that there's no need for a server to be running, but anyone can connect to any host at any time.
  
  As no server is needed, there's no way to control the activity of a single user,and that means more privacy than a simple client-server chat. For the moment, **the messages aren't encrypted**; but it seems that the [release of Java 9](http://openjdk.java.net/jeps/219) adds  support for [DTLS](https://en.wikipedia.org/wiki/Datagram_Transport_Layer_Security), so it's possible that it'll be implemented on this chat, too (even though it's a personal project and its implementation on this chat may not be correctly done and, therefore, the encryption would be useless).
  
  Java 8 is needed for this program to be executed correctly.
  
# Build and execution
  There are two different ways to execute this program: building it from source, or downloading the pre-built _.jar_
  
## Building from source
  1. Download the source of the [the latest release](https://github.com/Foo-Manroot/CAL/releases) and uncompress the downloaded file (or simply clone the repository).
  
  2. Create the output directory for  the _.class_ files (e.g.: `mkdir build`).
  
  3. Go to the src/ directory and compile the _.java_ with `javac -verbose -d ../build */*.java` (_-verbose_ isn't necessary, but recommended).
  
  4. After creating the _.class_ files, go  to the output directory (`cd ../build`) and create the manifest file needed for the _.jar_. The manifest (named Manifest.txt in this) file should, at least, have the following line (**with a new line** after the main class declaration):
  
    > Main-Class: main.Main
  
  5. Before creating the final _.jar_, some resources must be copied to the dist/ directory: 
  
    cp -rv ../src/resources .
    
    cp -v ../src/gui/FXMLPeer.fxml gui/

  6. Now, the _.jar_ file can be finnaly created. To this end, the program _jar_ is needed. It should be located at (java_root_directory)/bin/jar. The command to execute, supposing the output _.jar_ will be named _CAL.jar_, the manifest file is _Manifest.txt_ and the current directory is build/, is the following: `(java_root_directory)/bin/jar cvfm CAL.jar Manifest.txt */*`
   
## Downloading the pre-built _.jar_

  Simply download [the latest release](https://github.com/Foo-Manroot/CAL/releases) and execute the file with `java -jar CAL.jar`

# Use
  One of the objectives of this project is to make a fairly simple and friendly interface (for the moment it has plain and inelegant interface, but it wil be changed to a prettier and easier to use one).
  
  The main interface has a main pane that occupies the majority of the interface and two little panels on the right to show error and warning messages (they can be hidden). Below there are also two buttons to establish a new connection (button 'connect') and to check the connection with all the known peers (button 'check connection').
  
  To establish a new connection, both the button 'connect' and the '+' symbol on the tab panel can be pressed. After that, a little dialog is shown to enter the details of the host with which the connection will be done. The parameters to the new connection are the following:
    * IP address: it can be IPv4 or IPv6. It hasn't been tested with IPv6, though; but it should work correctly.
    * Port: port where the other peer is listening for incoming connections (the title of the main window should be 'peer listening on X', being X the port number).
    * Chat room ID: every peer can handle up to (2^8 - 2) different connections (254, from -128 to 126). This doesn't mean that it can be connected to a maximum of 254 _peers_, but it can bee connected to a maximum of 254 _rooms_. The maximum size of each room isn't limited.
    
  Once that the connection is achieved, a new tab will be automatically added to the tabs panel and the conversation can start.
  
## Commands
  
  To add more functionalities to the chat, there are commands that can be executed adding the escape character '/' to the start of the line.
  
  Also, there has been implemented an auto-completion utility so the TAB key can be pressed and the completed command (or, with another hit of the TAB key, a possible suggestion) can be shown. An example of this functionality:
  
    /h(TAB)
    (TAB)
    Suggestions: /help /hosts
    
  Another example:
  
    /he(TAB)
    /help     # Auto-completed after the TAB key is pressed.
    
  There are a few supported commands, but there is a work in progress to add more functionalities. For the moment, the supported commands are:
  * /help: shows a message with the available commands and their explanation
  * /hosts: Shows a list with information about the hosts. If no parameters are given, it shows only information about the   hosts on the current room. To show all the rooms, the parameter "all" must be added.
  * /leave: Leaves the current chat room.
  * /exit: Disconnects the user from all the rooms.

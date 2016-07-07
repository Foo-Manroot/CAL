/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import gui.PeerGUI;

/**
 * No documentation.
 */
public class Main {
    
    /**
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
//        String message = "/127.0.0.1: asdf67y8ushdf";
//        
//        System.out.println(message.matches("^.*:"));

        PeerGUI.main(args);
     
//        Peer peer;
//        Aux aux;
//        
//        peer = new Peer(1234);
//        
//        /* Creates 3 different peers (with their GUI) */
//        for (int i = 0; i < 1; i++) {
//            
//            peer = new Peer(1000 + i);
//            
//            aux = new Aux(peer, "" + (i + 1));
//            aux.start();
//        }
//    }
//    
//    /**
//     * Inner class for testing purposes.
//     */
//    private static class Aux extends Thread {
//        
//        private final Peer peer;
//        private final String id;
//        
//        protected Aux (Peer p, String id) {
//            
//            this.peer = p;
//            this.id = id;
//        }
//        
//        @Override
//        public void run () {
//            
//            PeerTEST_OLD peerGUI = new PeerTEST_OLD(peer);
//            peerGUI = new PeerTEST_OLD(peer);
//            peerGUI.setVisible(true);
//            
//            peerGUI.setTitle("TEST - Peer " + id);
////            peerGUI.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        }
    }
}

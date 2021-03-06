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
package gui.utils;

import common.Common;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.scene.control.Labeled;

/**
 * Observer for the language changes. This object can be used to notify some
 * nodes of a language change, so its text can be set to the new one.
 */
public class LangChangeObserver {

    /**
     * List with all the nodes to be notified when the language is changed.
     *
     * <p>
     * This nodes should have an id that corresponds with their resource key.
     */
    private final ConcurrentLinkedQueue<Labeled> nodes;


    /**
     * Constructor.
     */
    public LangChangeObserver () {

        nodes = new ConcurrentLinkedQueue<>();
    }

    /**
     * Changes the strings on all the nodes, based on their node ID. If the node
     * hasn't an ID or it doesn't fits with any of keys for the resources
     * strings, does nothing on that node.
     *
     * @param newLocale
     *              The locale to search the new strings.
     */
    public void languageChanged (Locale newLocale) {

        ResourceBundle resources;
        String key;

        resources = ResourceBundle.getBundle(Common.resourceBundle, newLocale);

        for (Labeled labeledNode : nodes) {

            /* Checks if the node id exists */
            if (labeledNode.getId() == null || labeledNode.getId().trim().isEmpty()) {

                continue;
            }

            key = searchKey(resources, labeledNode.getId());

            if (key == null) {

                continue;
            }

            System.out.println(labeledNode.getText());
        }
    }

    /**
     * Searches a key on the resource bundle that matches the node ID.
     *
     * @return
     *              The correspondent key if it has been found, or
     *          <i>null</i> if not.
     */
    private String searchKey (ResourceBundle resources, String nodeID) {

        ArrayList <String> matches = new ArrayList<>();

        for (String s : resources.keySet()) {

            if (nodeID.startsWith(s)) {

                matches.add(s);
            }
        }

        /* If there has been more or less than one coincidences, returns null */
        if (matches.size() != 1) {

            return null;
        }

        return matches.get(0);
    }

    /**
     * Adds a node to the list. No action will be performed with it unless its
     * ID starts exactly as one of the keys on the resources.
     *
     * <p>
     * If the added node has id "node", and the keys "nodeText" and
     * "nodeButtonText" both exist, no action can be performed, because of the
     * ambiguity of the node ID.
     *
     * @param node
     *              The node to be added. This node should have an id that
     *          corresponds with their resource key.
     */
    public void addNode (Labeled node) {

        nodes.add (node);
    }
}

package org.arbol.logic.tree.operation.treeBPlus;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TreeSearchTreeBPlus<K extends Comparable<K> & Serializable, V extends Serializable> implements TreeSearch<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeSearchTreeBPlus.class);

    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key) {

        Node<K, V> current = root;
        while (current instanceof BPlusInternalNode) {
            BPlusInternalNode<K, V> internalNode = (BPlusInternalNode<K, V>) current;
            int childIndex = current.findPosition(key);

            current = internalNode.getChild(childIndex);
        }
        // si estamos en la hoja:
        if (current instanceof BPlusLeafNode<K, V>) {
            BPlusLeafNode<K, V> leafNode = (BPlusLeafNode<K, V>) current;

            for (int i = 0; i < leafNode.size(); i++) {
                if (leafNode.getElementAt(i).key().equals(key)) {
                    return new Result.Success<>(leafNode.getElementAt(i));
                }
            }
        }


        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}
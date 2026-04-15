package org.arbol.logic.tree.operation.treeBPlusDisk;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.BPlusInternalNode;
import org.arbol.logic.structures.BPlusLeafNode;
import org.arbol.logic.structures.Node;
import org.arbol.logic.structures.NodeElement;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.utils.Result;

import java.io.Serializable;

public class TreeSearchTreeBPlusDisk<K extends Comparable<K> & Serializable, V extends Serializable>
        implements TreeSearch<K, V> {

    private final DiskOperationContext<K, V> context;

    public TreeSearchTreeBPlusDisk(DiskOperationContext<K, V> context) {
        this.context = context;
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key) {
        if (root == null) {
            return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
        }

        Node<K, V> current = root;
        while (current instanceof BPlusInternalNode<K, V> internal) {
            int childIndex = current.findPosition(key);
            Node<K, V> child = context.getChild(internal, childIndex);
            if (child == null) {
                return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
            }
            current = child;
        }

        if (current instanceof BPlusLeafNode<K, V> leaf) {
            for (int i = 0; i < leaf.size(); i++) {
                NodeElement<K, V> element = leaf.getElementAt(i);
                if (element.key().compareTo(key) == 0) {
                    return new Result.Success<>(element);
                }
            }
        }

        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}


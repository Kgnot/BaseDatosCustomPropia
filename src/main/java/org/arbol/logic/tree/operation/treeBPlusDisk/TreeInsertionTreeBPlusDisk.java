package org.arbol.logic.tree.operation.treeBPlusDisk;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.structures.node.SplitResult;
import org.arbol.logic.tree.operation.interace.TreeInsertion;
import org.arbol.utils.Result;

import java.io.Serializable;

public class TreeInsertionTreeBPlusDisk<K extends Comparable<K> & Serializable, V extends Serializable>
        implements TreeInsertion<K, V> {

    private final DiskOperationContext<K, V> context;

    public TreeInsertionTreeBPlusDisk(DiskOperationContext<K, V> context) {
        this.context = context;
    }

    @Override
    public Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute(
            Node<K, V> root,
            NodeElement<K, V> element,
            int maxSize
    ) {
        return insertRecursive(root, element);
    }

    private Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertRecursive(
            Node<K, V> node,
            NodeElement<K, V> element
    ) {
        if (node instanceof BPlusInternalNode<K, V> internal) {
            int index = node.findPosition(element.key());
            Node<K, V> child = context.getChild(internal, index);

            if (child == null) {
                throw new IllegalStateException("No se pudo cargar hijo en indice " + index + " desde nodo " + internal.getPageId());
            }

            Result<SplitResult<K, V>, NodeError.DuplicateKeyError> childResult = insertRecursive(child, element);
            if (childResult.isFailure()) {
                return childResult;
            }

            SplitResult<K, V> childSplit = childResult.unwrap();
            if (childSplit.newNode() != null) {
                Node<K, V> splitNewNode = childSplit.newNode();
                if (splitNewNode.getPageId() < 0) {
                    splitNewNode.setPageId(context.allocatePageId());
                }

                internal.addElementAt(index, childSplit.promotedElement());
                internal.insertChildAt(index + 1, splitNewNode);

                if (internal.isFull()) {
                    SplitResult<K, V> internalSplit = internal.split();
                    if (internalSplit.newNode().getPageId() < 0) {
                        internalSplit.newNode().setPageId(context.allocatePageId());
                    }
                    context.saveNode(internal);
                    context.saveNode(internalSplit.newNode());
                    return new Result.Success<>(internalSplit);
                }

                context.saveNode(internal);
                context.saveNode(splitNewNode);
            }

            return new Result.Success<>(new SplitResult<>(null, null));
        }

        if (node.containsKey(element.key())) {
            return new Result.Failure<>(new NodeError.DuplicateKeyError(element.key()));
        }

        node.insertInOrder(element);

        if (node.isFull()) {
            SplitResult<K, V> leafSplit = node.split();
            Node<K, V> splitRight = leafSplit.newNode();
            if (splitRight.getPageId() < 0) {
                splitRight.setPageId(context.allocatePageId());
            }

            if (node instanceof BPlusLeafNode<K, V> leftLeaf && splitRight instanceof BPlusLeafNode<K, V> rightLeaf) {
                leftLeaf.setNextLeaf(rightLeaf);
            }

            context.saveNode(node);
            context.saveNode(splitRight);
            return new Result.Success<>(leafSplit);
        }

        context.saveNode(node);
        return new Result.Success<>(new SplitResult<>(null, null));
    }
}


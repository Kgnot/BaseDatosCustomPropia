package org.arbol.logic.tree;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.logic.tree.operation.interace.TreeInsertion;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.logic.tree.operation.treeBPlusDisk.DiskOperationContext;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeDeleteTreeBPlusDisk;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeInsertionTreeBPlusDisk;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeSearchTreeBPlusDisk;
import org.arbol.logic.structures.BPlusInternalNode;
import org.arbol.logic.structures.BPlusLeafNode;
import org.arbol.logic.structures.Node;
import org.arbol.logic.structures.NodeElement;
import org.arbol.logic.structures.SplitResult;
import org.arbol.logic.structures.Tree;
import org.arbol.utils.Result;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

public class TreeBPlusDisk<K extends Comparable<K> & Serializable, V extends Serializable> extends Tree<K, V> {

    private final int maxSize;
    private final DiskOperationContext<K, V> context;

    private TreeSearch<K, V> treeSearch;
    private TreeInsertion<K, V> treeInsertion;
    private TreeDelete<K, V> treeDelete;

    public TreeBPlusDisk(String dataFileName, int maxSize) {
        super();
        this.maxSize = maxSize;
        this.context = new DiskOperationContext<>(dataFileName, maxSize);
        initializeOperations();
        loadOrCreateRoot();
    }

    private void initializeOperations() {
        this.treeSearch = new TreeSearchTreeBPlusDisk<>(context);
        this.treeInsertion = new TreeInsertionTreeBPlusDisk<>(context);
        this.treeDelete = new TreeDeleteTreeBPlusDisk<>();
    }

    private void loadOrCreateRoot() {
        byte[] rootData = context.readPage(0);
        if (rootData == null) {
            BPlusLeafNode<K, V> newRoot = new BPlusLeafNode<>(maxSize);
            newRoot.setPageId(0L);
            this.root = newRoot;
            context.saveNode(newRoot);
            context.initializeForNewTree();
            return;
        }

        this.root = context.deserializeNode(rootData);
        this.root.setPageId(0L);
        context.initializeFromStorage();
    }

    @Override
    public Result<Void, NodeError.DuplicateKeyError> insert(NodeElement<K, V> element) {
        if (root == null) {
            BPlusLeafNode<K, V> leafRoot = new BPlusLeafNode<>(maxSize);
            leafRoot.setPageId(0L);
            root = leafRoot;
        }

        Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertResult =
                treeInsertion.execute(root, element, maxSize);
        if (insertResult.isFailure()) {
            return insertResult.map(split -> null);
        }

        SplitResult<K, V> splitResult = insertResult.unwrap();

        if (splitResult.newNode() != null) {
            Node<K, V> oldRoot = root;
            if (oldRoot.getPageId() == 0L) {
                oldRoot.setPageId(context.allocatePageId());
            }
            context.saveNode(oldRoot);

            Node<K, V> newRight = splitResult.newNode();
            if (newRight.getPageId() < 0) {
                newRight.setPageId(context.allocatePageId());
            }
            context.saveNode(newRight);

            BPlusInternalNode<K, V> newRoot = new BPlusInternalNode<>(maxSize);
            newRoot.setPageId(0L);
            newRoot.addElement(splitResult.promotedElement());
            newRoot.addChild(oldRoot);
            newRoot.addChild(newRight);

            this.root = newRoot;
            context.saveNode(newRoot);
        } else {
            context.saveNode(root);
        }

        return new Result.Success<>(null);
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key) {
        return treeSearch.execute(root, key);
    }

    @Override
    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return treeDelete.execute(root, key, maxSize);
    }

    @Override
    public String toString() {
        if (root == null) {
            return "TreeBPlusDisk(empty)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TreeBPlusDisk (maxSize=").append(maxSize).append(")\n");

        Queue<Node<K, V>> queue = new ArrayDeque<>();
        queue.add(root);
        int level = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            sb.append("Level ").append(level).append(": ");

            for (int i = 0; i < size; i++) {
                Node<K, V> node = queue.poll();
                if (node == null) {
                    continue;
                }

                sb.append("[p").append(node.getPageId()).append("] ").append(node).append(" ");

                if (node instanceof BPlusInternalNode<K, V> internal) {
                    for (int childIndex = 0; childIndex < internal.getChildPageIds().size(); childIndex++) {
                        Node<K, V> child = context.getChild(internal, childIndex);
                        if (child != null) {
                            queue.add(child);
                        }
                    }
                }
            }

            sb.append('\n');
            level++;
        }

        return sb.toString();
    }
}



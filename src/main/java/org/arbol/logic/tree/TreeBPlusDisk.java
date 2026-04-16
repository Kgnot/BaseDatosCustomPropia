package org.arbol.logic.tree;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.logic.tree.operation.interace.TreeInsertion;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.logic.tree.operation.treeBPlusDisk.DiskOperationContext;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeDeleteTreeBPlusDisk;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeInsertionTreeBPlusDisk;
import org.arbol.logic.tree.operation.treeBPlusDisk.TreeSearchTreeBPlusDisk;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.structures.node.SplitResult;
import org.arbol.logic.structures.Tree;
import org.arbol.utils.Result;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            // En B+ interno solo almacenamos la clave separadora.
            newRoot.addElement(new NodeElement<>(splitResult.promotedElement().key(), null));
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

    public List<NodeElement<K, V>> findAll() {
        return findAll(0, Integer.MAX_VALUE);
    }

    public List<NodeElement<K, V>> findAll(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset no puede ser negativo");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit no puede ser negativo");
        }
        if (root == null || limit == 0) {
            return Collections.emptyList();
        }

        Node<K, V> current = root;
        while (current instanceof BPlusInternalNode<K, V> internal) {
            current = context.getChild(internal, 0);
            if (current == null) {
                return Collections.emptyList();
            }
        }

        if (!(current instanceof BPlusLeafNode<?, ?>)) {
            return Collections.emptyList();
        }
        BPlusLeafNode<K, V> leaf = (BPlusLeafNode<K, V>) current;

        List<NodeElement<K, V>> result = new ArrayList<>();
        int skipped = 0;

        while (true) {
            for (NodeElement<K, V> element : leaf.getNodeElements()) {
                if (skipped < offset) {
                    skipped++;
                    continue;
                }

                result.add(element);
                if (result.size() >= limit) {
                    break;
                }
            }

            if (result.size() >= limit) {
                break;
            }

            long nextLeafPageId = leaf.getNextLeafPageId();
            if (nextLeafPageId < 0) {
                break;
            }

            Node<K, V> nextNode = context.getNodeByPageId(nextLeafPageId);
            if (!(nextNode instanceof BPlusLeafNode<?, ?>)) {
                break;
            }
            leaf = (BPlusLeafNode<K, V>) nextNode;
        }

        return result;
    }

    public void close() {
        context.close();
    }
}



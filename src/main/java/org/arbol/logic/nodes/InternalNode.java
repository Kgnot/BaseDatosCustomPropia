package org.arbol.logic.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class InternalNode<K extends Comparable<K>, V>
        extends Node<K, V> {
    // logger para debug
    private static final Logger logger =
            LoggerFactory.getLogger(InternalNode.class);
    // los hijos que puede tener
    private List<Node<K, V>> children;


    public InternalNode(int maxSize) {
        super(maxSize);
        this.children = new ArrayList<>();
    }

    public InternalNode(int maxSize,
                        List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
        this.children = new ArrayList<>();
    }


    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void addChild(Node<K, V> node) {
        this.children.add(node);
    }

    public List<Node<K, V>> getChildren() {
        return this.children;
    }

    public Node<K, V> getChild(int index) {
        logger.debug("Tamaño de los hijos: {} ", children.size());
        return this.children.get(index);
    }


    public int findChildIndex(K key) {
        int index = 0;
        while (index < nodeElements.size()
                && key.compareTo(nodeElements.get(index).key()) > 0) {
            index++;
        }
        return index;
    }


    @Override
    public SplitResult<K, V> split() {
        logger.info("Estoy haciendo split");
        int indexMedio = nodeElements.size() / 2;
        var nodeElement = nodeElements.get(indexMedio);
        var subListIzq =
                new ArrayList<>(nodeElements.subList(0, indexMedio));
        var subListDer =
                new ArrayList<>(nodeElements.subList(indexMedio + 1,
                        nodeElements.size()));
        Node<K, V> nodoIzq =
                Node.createInternalNodeWithElements(maxSize, subListIzq);
        Node<K, V> nodoDer =
                Node.createInternalNodeWithElements(maxSize, subListDer);
        if (hasChildren()) {
            logger.info("Dentro de el split si hay hijos");
            int childSplit = indexMedio + 1;
            ((InternalNode<K, V>) nodoIzq).children =
                    new ArrayList<>(children.subList(0, childSplit));
            ((InternalNode<K, V>) nodoDer).children =
                    new ArrayList<>(children.subList(childSplit,
                            children.size()));
        }
        this.nodeElements = subListIzq;
        this.children = ((InternalNode<K, V>) nodoIzq).children;
        return new SplitResult<>(nodeElement, nodoDer);
    }

    public void insertElementAt(int index, NodeElement<K,V> element) {
        nodeElements.add(index, element);
    }

    public void insertChildAt(int index, Node<K,V> child) {
        children.add(index, child);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InternalNode{");
        sb.append("keys=[");

        for (int i = 0; i < nodeElements.size(); i++) {
            sb.append(nodeElements.get(i).key());
            if (i < nodeElements.size() - 1) sb.append(", ");
        }

        sb.append("]");

        if (children != null && !children.isEmpty()) {
            sb.append(", children=").append(children.size());
        } else {
            sb.append(", children=0");
        }

        sb.append("}");

        return sb.toString();
    }
}

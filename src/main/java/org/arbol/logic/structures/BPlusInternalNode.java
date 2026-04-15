package org.arbol.logic.structures;

import java.util.ArrayList;
import java.util.List;

public final class BPlusInternalNode<K extends Comparable<K>, V> extends Node<K, V> {

    private List<Node<K, V>> children;

    public BPlusInternalNode(int maxSize) {
        super(maxSize);
        this.children = new ArrayList<>();
    }

    public BPlusInternalNode(int maxSize, List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
        this.children = new ArrayList<>();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    public void setChild(int index, Node<K, V> child) {
        this.children.set(index, child);
    }

    public void addChild(Node<K, V> node) {
        this.children.add(node);
    }

    public void insertChildAt(int index, Node<K, V> child) {
        this.children.add(index, child);
    }

    public Node<K, V> getChild(int index) {
        return this.children.get(index);
    }

    public void removeChild(int index) {
        this.children.remove(index);
    }

    public List<Node<K, V>> getChildren() {
        return children;
    }

    /**
     * SPLIT DE NODO INTERNO B+ TREE
     * Aquí también se promociona el elemento del medio, pero la lógica
     * de división de hijos cambia respecto a B-Tree estándar para mantener la consistencia.
     */
    @Override
    public SplitResult<K, V> split() {
        int indexMedio = nodeElements.size() / 2;
        var promotedElement = nodeElements.get(indexMedio);

        // Izquierda: elementos desde 0 hasta indexMedio (excluyente en B+)
        var subListIzq = new ArrayList<>(nodeElements.subList(0, indexMedio));
        // Derecha: elementos desde indexMedio (incluyente) hasta el final
        var subListDer = new ArrayList<>(nodeElements.subList(indexMedio, nodeElements.size()));

        BPlusInternalNode<K, V> nodoIzq = new BPlusInternalNode<>(maxSize, subListIzq);
        BPlusInternalNode<K, V> nodoDer = new BPlusInternalNode<>(maxSize, subListDer);

        // Dividir hijos
        // Si tenemos M elementos, tenemos M+1 hijos.
        // El hijo en posición indexMedio sube con la clave? No, en B+ el hijo medio va a la derecha.
        int childSplitIndex = indexMedio + 1;

        nodoIzq.children = new ArrayList<>(children.subList(0, childSplitIndex));
        nodoDer.children = new ArrayList<>(children.subList(childSplitIndex, children.size()));

        // Reflejar el split en este nodo (ahora representa la izquierda)
        this.nodeElements = subListIzq;
        this.children = nodoIzq.children;

        return new SplitResult<>(promotedElement, nodoDer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InternalNode{keys=[");
        for (int i = 0; i < nodeElements.size(); i++) {
            sb.append(nodeElements.get(i).key());
            if (i < nodeElements.size() - 1) sb.append(", ");
        }
        sb.append("], children=").append(children.size()).append("}");
        return sb.toString();
    }
}
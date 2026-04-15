package org.arbol.logic.structures;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class BPlusInternalNode<K extends Comparable<K> & Serializable, V extends Serializable> extends Node<K, V> {

    private List<Node<K, V>> children;
    private List<Long> childPageIds; // ids de hijos en disco

    public BPlusInternalNode(int maxSize) {
        super(maxSize);
        this.children = new ArrayList<>();
        this.childPageIds = new ArrayList<>();
    }

    public BPlusInternalNode(int maxSize, List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
        this.children = new ArrayList<>();
        this.childPageIds = new ArrayList<>();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    public void setChild(int index, Node<K, V> child) {
        while (this.children.size() <= index) {
            this.children.add(null);
        }
        this.children.set(index, child);
        setChildPageIdAt(index, child != null ? child.getPageId() : -1L);
    }

    public void addChild(Node<K, V> node) {
        this.children.add(node);
        this.childPageIds.add(node != null ? node.getPageId() : -1L);
    }

    public void insertChildAt(int index, Node<K, V> child) {
        this.children.add(index, child);
        this.childPageIds.add(index, child != null ? child.getPageId() : -1L);
    }

    public Node<K, V> getChild(int index) {
        return this.children.get(index);
    }

    public void removeChild(int index) {
        this.children.remove(index);
        if (index < this.childPageIds.size()) {
            this.childPageIds.remove(index);
        }
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
        nodoIzq.childPageIds = new ArrayList<>(childPageIds.subList(0, childSplitIndex));
        nodoDer.childPageIds = new ArrayList<>(childPageIds.subList(childSplitIndex, childPageIds.size()));

        // Reflejar el split en este nodo (ahora representa la izquierda)
        this.nodeElements = subListIzq;
        this.children = nodoIzq.children;
        this.childPageIds = nodoIzq.childPageIds;

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

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            // 1 = nodo interno
            oos.writeInt(1);
            oos.writeInt(maxSize);
            oos.writeLong(pageId);

            // cantidad de claves
            oos.writeInt(nodeElements.size());

            // serializar claves
            for (NodeElement<K, V> element : nodeElements) {
                oos.writeObject(element.key());
                oos.writeObject(element.value());
            }

            // cantidad de hijos
            oos.writeInt(childPageIds.size());

            // serializamos ids de hijos para cargarlos bajo demanda
            for (Long childPageId : childPageIds) {
                oos.writeLong(childPageId != null ? childPageId : -1L);
            }

            oos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error serializando nodo interno B+", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(byte[] data) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            int nodeType = ois.readInt();

            if (nodeType != 1) {
                throw new IllegalStateException("El bloque no corresponde a un nodo interno");
            }

            this.maxSize = ois.readInt();
            this.pageId = ois.readLong();

            // leer claves
            int size = ois.readInt();
            this.nodeElements.clear();

            for (int i = 0; i < size; i++) {
                K key = (K) ois.readObject();
                V value = (V) ois.readObject();
                this.nodeElements.add(new NodeElement<>(key, value));
            }

            // leer hijos
            int childCount = ois.readInt();
            this.children.clear();
            this.childPageIds.clear();

            for (int i = 0; i < childCount; i++) {
                this.childPageIds.add(ois.readLong());
                this.children.add(null);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error deserializando nodo interno B+", e);
        }
    }

    public List<Long> getChildPageIds() {
        return childPageIds;
    }

    public void setChildPageIdAt(int index, long childPageId) {
        while (this.childPageIds.size() <= index) {
            this.childPageIds.add(-1L);
        }
        this.childPageIds.set(index, childPageId);
    }

    public void addChildPageId(long childPageId) {
        this.childPageIds.add(childPageId);
    }

    public void insertChildPageIdAt(int index, long childPageId) {
        this.childPageIds.add(index, childPageId);
    }

    public long getChildPageIdAt(int index) {
        if (index < 0 || index >= childPageIds.size()) {
            return -1L;
        }
        return childPageIds.get(index);
    }

    public void setChildPageIds(List<Long> childPageIds) {
        this.childPageIds = new ArrayList<>(childPageIds);
    }

    public void syncChildPageIdsFromChildren() {
        this.childPageIds.clear();
        for (Node<K, V> child : children) {
            this.childPageIds.add(child != null ? child.getPageId() : -1L);
        }
    }

    public void clearChildrenReferences() {
        this.children.clear();
        for (int i = 0; i < childPageIds.size(); i++) {
            this.children.add(null);
        }
    }

}
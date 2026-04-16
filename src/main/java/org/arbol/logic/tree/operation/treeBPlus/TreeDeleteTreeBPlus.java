package org.arbol.logic.tree.operation.treeBPlus;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TreeDeleteTreeBPlus<K extends Comparable<K> & Serializable, V extends Serializable> implements TreeDelete<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeDeleteTreeBPlus.class);

    @Override
    public Result<Void, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key, int maxSize) {
        if (root == null) {
            return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
        }
        // Iniciamos la recursión. La raíz no tiene padre.
        Node<K, V> newRoot = deleteNode(root, null, -1, key, maxSize);
        // Caso especial: Si la raíz es interna y quedó con 0 claves (pero 1 hijo), bajamos de nivel
        if (newRoot instanceof BPlusInternalNode<K, V> internal && newRoot.isEmpty()) {
            logger.info("La raíz se quedó vacía, bajando un nivel.");
            if (!internal.getChildren().isEmpty()) {
                newRoot = internal.getChild(0); // El único hijo se convierte en la nueva raíz
            }
        }
        return new Result.Success<>(null);
    }
    private Node<K, V> deleteNode(Node<K, V> node, Node<K, V> parent, int indexInParent, K key, int maxSize) {

        // CASO 1: Estamos en un Nodo Interno
        if (node instanceof BPlusInternalNode<K, V> internal) {

            // Encontrar por qué hijo bajar
            int childIndex = node.findPosition(key);
            Node<K, V> child = internal.getChild(childIndex);

            // Llamada recursiva
            Node<K, V> updatedChild = deleteNode(child, node, childIndex, key, maxSize);

            // Si el hijo cambió (por ejemplo, se fusionó con su hermano y desapareció)
            if (updatedChild != child) {
                internal.setChild(childIndex, updatedChild);
            }

            // Verificar si este nodo interno sufrió underflow porque eliminamos una clave separadora tras una fusión de hijos
            if (parent != null && internal.size() < getMinSize(maxSize)) {
                return handleInternalUnderflow(internal, parent, indexInParent, maxSize);
            }

            return internal;
        }

        // CASO 2: Estamos en una Hoja
        else if (node instanceof BPlusLeafNode<K, V> leaf) {

            // Intentar borrar la clave
            boolean deleted = removeFromLeaf(leaf, key);

            if (!deleted) {
                return leaf; // No se encontró, no pasa nada
            }

            // Si se borró, verificar underflow
            if (parent != null && leaf.size() < getMinSize(maxSize)) {
                return handleLeafUnderflow(leaf, parent, indexInParent, maxSize);
            }

            return leaf;
        }

        return node;
    }

    // --- MÉTODOS DE AYUDA PARA HOJAS ---

    private boolean removeFromLeaf(BPlusLeafNode<K, V> leaf, K key) {
        for (int i = 0; i < leaf.size(); i++) {
            if (leaf.getElementAt(i).key().equals(key)) {
                leaf.removeElementAt(i);
                logger.debug("Clave {} eliminada de la hoja.", key);
                return true;
            }
        }
        return false;
    }

    private Node<K, V> handleLeafUnderflow(BPlusLeafNode<K, V> leaf, Node<K, V> parent, int indexInParent, int maxSize) {
        logger.debug("Underflow en hoja. Intentando rebalancear.");

        // 1. Intentar pedir prestado al hermano IZQUIERDO
        if (indexInParent > 0) {
            BPlusLeafNode<K, V> leftSibling = (BPlusLeafNode<K, V>) ((BPlusInternalNode<K, V>) parent).getChild(indexInParent - 1);
            if (leftSibling.size() > getMinSize(maxSize)) {
                return borrowFromLeftSibling(leaf, leftSibling, parent, indexInParent - 1);
            }
        }

        // 2. Intentar pedir prestado al hermano DERECHO
        if (indexInParent < ((BPlusInternalNode<K, V>) parent).getChildren().size() - 1) {
            BPlusLeafNode<K, V> rightSibling = (BPlusLeafNode<K, V>) ((BPlusInternalNode<K, V>) parent).getChild(indexInParent + 1);
            if (rightSibling.size() > getMinSize(maxSize)) {
                return borrowFromRightSibling(leaf, rightSibling, parent, indexInParent);
            }
        }

        // 3. Si no hay prestamistas, FUSIONAR (Merge)
        // Preferimos fusionar con el IZQUIERDO
        if (indexInParent > 0) {
            BPlusLeafNode<K, V> leftSibling = (BPlusLeafNode<K, V>) ((BPlusInternalNode<K, V>) parent).getChild(indexInParent - 1);
            return mergeLeaves(leftSibling, leaf, parent, indexInParent - 1);
        }
        // Si no hay izquierdo, fusionar con el DERECHO
        else {
            BPlusLeafNode<K, V> rightSibling = (BPlusLeafNode<K, V>) ((BPlusInternalNode<K, V>) parent).getChild(indexInParent + 1);
            // Fusionamos 'leaf' dentro de 'rightSibling' (o viceversa)
            return mergeLeaves(leaf, rightSibling, parent, indexInParent);
        }
    }

    private Node<K, V> borrowFromLeftSibling(BPlusLeafNode<K, V> leaf, BPlusLeafNode<K, V> leftSibling, Node<K, V> parent, int separatorIndexInParent) {
        // 1. Tomar el último elemento del hermano izquierdo
        NodeElement<K, V> borrowed = leftSibling.getElementAt(leftSibling.size() - 1);
        leftSibling.removeElementAt(leftSibling.size() - 1);

        // 2. Insertarlo al principio de la hoja actual
        leaf.insertInOrder(borrowed);

        // 3. actualizamos el separador del padre
        // En B+, el separador que apunta a 'leaf' debe ser la nueva clave más pequeña de 'leaf'.
        NodeElement<K, V> newSeparator = leaf.getElementAt(0);
        parent.setElementAt(separatorIndexInParent + 1, newSeparator); // +1 porque en el padre, index es al hijo izquierdo

        logger.debug("Préstamo desde hermano izquierdo exitoso.");
        return leaf;
    }

    private Node<K, V> borrowFromRightSibling(BPlusLeafNode<K, V> leaf, BPlusLeafNode<K, V> rightSibling, Node<K, V> parent, int separatorIndexInParent) {
        // 1. Tomar el primer elemento del hermano derecho
        NodeElement<K, V> borrowed = rightSibling.getElementAt(0);
        rightSibling.removeElementAt(0);
        // 2. Insertarlo al final de la hoja actual
        leaf.insertInOrder(borrowed);
        // El separador que apunta a 'rightSibling' cambia a la nueva primera clave de 'rightSibling'
        NodeElement<K, V> newSeparator = rightSibling.getElementAt(0);
        parent.setElementAt(separatorIndexInParent + 1, newSeparator);

        logger.debug("Préstamo desde hermano derecho exitoso.");
        return leaf;
    }

    private Node<K, V> mergeLeaves(BPlusLeafNode<K, V> left, BPlusLeafNode<K, V> right, Node<K, V> parent, int leftIndexInParent) {
        // Mover todos los elementos de derecha a izquierda
        for (int i = 0; i < right.size(); i++) {
            left.addElement(right.getElementAt(i));
        }
        // Enlazar punteros
        left.setNextLeaf(right.getNextLeaf());
        parent.removeElementAt(leftIndexInParent); // Eliminar el separador
        ((BPlusInternalNode<K, V>) parent).removeChild(leftIndexInParent + 1); // Eliminar el puntero a la hoja derecha

        logger.debug("Fusión de hojas completada.");
        return left; // Devolvemos la hoja izquierda fusionada
    }

    // --- MÉTODOS DE AYUDA PARA NODOS INTERNOS ---

    private Node<K, V> handleInternalUnderflow(BPlusInternalNode<K, V> node, Node<K, V> parent, int indexInParent, int maxSize) {
        logger.debug("Underflow en nodo interno.");

        BPlusInternalNode<K, V> parentNode = (BPlusInternalNode<K, V>) parent;

        // 1. Intentar prestar de IZQUIERDA
        if (indexInParent > 0) {
            BPlusInternalNode<K, V> leftSibling = (BPlusInternalNode<K, V>) parentNode.getChild(indexInParent - 1);
            if (leftSibling.size() > getMinSize(maxSize)) {
                return internalBorrowLeft(node, leftSibling, parentNode, indexInParent - 1);
            }
        }

        // 2. Intentar prestar de DERECHA
        if (indexInParent < parentNode.getChildren().size() - 1) {
            BPlusInternalNode<K, V> rightSibling = (BPlusInternalNode<K, V>) parentNode.getChild(indexInParent + 1);
            if (rightSibling.size() > getMinSize(maxSize)) {
                return internalBorrowRight(node, rightSibling, parentNode, indexInParent);
            }
        }

        // 3. FUSIONAR
        if (indexInParent > 0) {
            BPlusInternalNode<K, V> leftSibling = (BPlusInternalNode<K, V>) parentNode.getChild(indexInParent - 1);
            return mergeInternalNodes(leftSibling, node, parentNode, indexInParent - 1);
        } else {
            BPlusInternalNode<K, V> rightSibling = (BPlusInternalNode<K, V>) parentNode.getChild(indexInParent + 1);
            return mergeInternalNodes(node, rightSibling, parentNode, indexInParent);
        }
    }

    private Node<K, V> internalBorrowLeft(BPlusInternalNode<K, V> node, BPlusInternalNode<K, V> leftSibling, BPlusInternalNode<K, V> parent, int parentKeyIndex) {
        // 1. Bajar el separador del padre al nodo actual (como primera clave)
        NodeElement<K, V> parentSeparator = parent.getElementAt(parentKeyIndex);
        node.addElementAt(0, parentSeparator);

        // 2. Subir la última clave del hermano izquierdo al padre
        NodeElement<K, V> siblingLastKey = leftSibling.getElementAt(leftSibling.size() - 1);
        parent.setElementAt(parentKeyIndex, siblingLastKey);
        leftSibling.removeElementAt(leftSibling.size() - 1);

        // 3. Mover el último hijo del hermano izquierdo al nodo actual (al principio)
        Node<K, V> siblingLastChild = leftSibling.getChild(leftSibling.getChildren().size() - 1);
        node.insertChildAt(0, siblingLastChild);
        leftSibling.removeChild(leftSibling.getChildren().size() - 1);

        return node;
    }

    private Node<K, V> internalBorrowRight(BPlusInternalNode<K, V> node, BPlusInternalNode<K, V> rightSibling, BPlusInternalNode<K, V> parent, int parentKeyIndex) {
        // 1. Bajar el separador del padre al nodo actual (como última clave)
        NodeElement<K, V> parentSeparator = parent.getElementAt(parentKeyIndex);
        node.addElement(parentSeparator);

        // 2. Subir la primera clave del hermano derecho al padre
        NodeElement<K, V> siblingFirstKey = rightSibling.getElementAt(0);
        parent.setElementAt(parentKeyIndex, siblingFirstKey);
        rightSibling.removeElementAt(0);

        // 3. Mover el primer hijo del hermano derecho al nodo actual (al final)
        Node<K, V> siblingFirstChild = rightSibling.getChild(0);
        node.addChild(siblingFirstChild);
        rightSibling.removeChild(0);

        return node;
    }

    private Node<K, V> mergeInternalNodes(BPlusInternalNode<K, V> left, BPlusInternalNode<K, V> right, BPlusInternalNode<K, V> parent, int parentKeyIndex) {
        // 1. Bajar el separador del padre a la izquierda
        NodeElement<K, V> separator = parent.getElementAt(parentKeyIndex);
        left.addElement(separator);

        // 2. Mover todas las claves del nodo derecho a la izquierda
        for (int i = 0; i < right.size(); i++) {
            left.addElement(right.getElementAt(i));
        }

        // 3. Mover todos los hijos del nodo derecho a la izquierda
        for (Node<K, V> child : right.getChildren()) {
            left.addChild(child);
        }

        // 4. Eliminar el separador y el puntero del padre
        parent.removeElementAt(parentKeyIndex);
        parent.removeChild(parentKeyIndex + 1);

        return left;
    }

    private int getMinSize(int maxSize) {
        return (maxSize + 1) / 2;
    }
}


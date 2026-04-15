package org.arbol.logic.tree.operation.treeB;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.utils.Result;
import org.arbol.logic.structures.InternalNode;
import org.arbol.logic.structures.Node;
import org.arbol.logic.structures.NodeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeDeleteTreeB<K extends Comparable<K>, V> implements TreeDelete<K,V> {
    private static final Logger logger = LoggerFactory.getLogger(TreeDeleteTreeB.class);
    private int invariante;

    public Result<Void, NodeError.NodeNotFoundError> execute
            (Node<K, V> root, K key, int maxSize) {
        this.invariante = (maxSize - 1) / 2; // calculamos la invariante
        // caso base
        return deleteRecursive(root, key, maxSize);
    }

    private Result<Void, NodeError.NodeNotFoundError> deleteRecursive(
            Node<K, V> node, K key, int maxSize) {
        // caso base:
        if (node == null) {
            return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
        }
        int pos = node.findPosition(key);
        boolean found = pos < node.size() &&
                node.getElementAt(pos).key().compareTo(key) == 0;
        // si es una hoja
        if (!node.hasChildren()) {
            if (!found) {
                return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
            }
            logger.debug("Se encontro y se eliminará : {}", key);
            node.removeElementAt(pos);
            return new Result.Success<>(null);
        }
        // ahora si es interno y tenemos la clave
        if (found) {
            logger.debug("Clave {} encontrada en nodo interno", key);
            return deleteFromInternalNode(node, pos, key, maxSize);
        }
        //Si no esta en el nodo pero podría estar en un hijo:
        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        Node<K, V> child = internal.getChild(pos);
        logger.debug("Recursión hacia hijo en:  {}", pos);
        Result<Void, NodeError.NodeNotFoundError> result = deleteRecursive(child, key, maxSize);
        if (result.isFailure()) {
            return result;
        }
        // ahora mirar si el hijo no cumple la invariante
        if (child.size() < invariante) {
            logger.debug("El hijo en la posición {} no cumple la invariante, se intentará balancear", pos);
            balance(internal, pos, maxSize);
        }
        return new Result.Success<>(null);
    }


    private Result<Void, NodeError.NodeNotFoundError> deleteFromInternalNode(
            Node<K, V> node, int pos, K key, int maxSize) {

        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        Node<K, V> leftChild = internal.getChild(pos);
        Node<K, V> rightChild = internal.getChild(pos + 1);

        // Opción 1: Si el hijo izquierdo tiene suficientes claves, usar PREDECESOR
        if (leftChild.size() >= invariante + 1) {
            logger.debug("Usando predecesor del hijo izquierdo");
            NodeElement<K, V> predecessor = getPredecessor(leftChild);
            node.setElementAt(pos, predecessor);
            return deleteRecursive(leftChild, predecessor.key(), maxSize);
        }

        // Opción 2: Si el hijo derecho tiene suficientes claves, usar SUCESOR
        if (rightChild.size() >= invariante + 1) {
            logger.debug("Usando sucesor del hijo derecho");
            NodeElement<K, V> successor = getSuccessor(rightChild);
            node.setElementAt(pos, successor);
            return deleteRecursive(rightChild, successor.key(), maxSize);
        }

        // Opción 3: Ninguno tiene suficientes claves, hacer MERGE
        logger.debug("Mergeando hijos en posición {}", pos);
        mergeWithRightSibling(internal, pos, maxSize);
        return deleteRecursive(leftChild, key, maxSize);
    }

    // Para predecesor:
    private NodeElement<K, V> getPredecessor(Node<K, V> node) {
        if (!node.hasChildren()) {
            return node.getElementAt(node.size() - 1);
        }

        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        return getPredecessor(internal.getChild(internal.getChildren().size() - 1));
    }

    //    para sucesor:
    private NodeElement<K, V> getSuccessor(Node<K, V> node) {
        if (!node.hasChildren()) {
            return node.getElementAt(0);
        }

        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        return getSuccessor(internal.getChild(0));
    }

    private void balance(InternalNode<K, V> parent, int childPos, int maxSize) {
        // intenta balancear izquirda
        if (childPos > 0) {
            Node<K, V> leftSibling = parent.getChild(childPos - 1);
            if (leftSibling.size() > invariante) {
                logger.debug("Redistribuyendo desde hermano izquierdo");
                redistributeFromLeftSibling(parent, childPos);
                return;
            }
        }

        // Intentar redistribuir con hermano derecho
        if (childPos < parent.getChildren().size() - 1) {
            Node<K, V> rightSibling = parent.getChild(childPos + 1);
            if (rightSibling.size() > invariante) {
                logger.debug("Redistribuyendo desde hermano derecho");
                redistributeFromRightSibling(parent, childPos);
                return;
            }
        }

        // No se puede redistribuir, hacer MERGE
        if (childPos > 0) {
            logger.debug("Mergeando con hermano izquierdo");
            mergeWithLeftSibling(parent, childPos, maxSize);
        } else {
            logger.debug("Mergeando con hermano derecho");
            mergeWithRightSibling(parent, childPos, maxSize);
        }
    }

    // redistribución desde hermano izquierdo
    private void redistributeFromLeftSibling(InternalNode<K, V> parent, int childPos) {
        Node<K, V> child = parent.getChild(childPos);
        Node<K, V> leftSibling = parent.getChild(childPos - 1);

        // Bajar clave del padre al inicio del hijo
        NodeElement<K, V> parentElement = parent.getElementAt(childPos - 1);
        child.addElementAt(0, parentElement);

        // Subir última clave del hermano al padre
        NodeElement<K, V> fromSibling = leftSibling.getElementAt(leftSibling.size() - 1);
        leftSibling.removeElementAt(leftSibling.size() - 1);
        parent.setElementAt(childPos - 1, fromSibling);

        // Si tienen hijos, mover también el último hijo del hermano
        if (child.hasChildren() && leftSibling.hasChildren()) {
            InternalNode<K, V> childInternal = (InternalNode<K, V>) child;
            InternalNode<K, V> siblingInternal = (InternalNode<K, V>) leftSibling;

            Node<K, V> childToMove = siblingInternal.getChildren().removeLast();
            childInternal.getChildren().addFirst(childToMove);
        }
    }

    private void redistributeFromRightSibling(InternalNode<K, V> parent, int childPos) {
        Node<K, V> child = parent.getChild(childPos);
        Node<K, V> rightSibling = parent.getChild(childPos + 1);

        // Bajar clave del padre al final del hijo
        NodeElement<K, V> parentElement = parent.getElementAt(childPos);
        child.addElement(parentElement);

        // Subir primera clave del hermano al padre
        NodeElement<K, V> fromSibling = rightSibling.getElementAt(0);
        rightSibling.removeElementAt(0);
        parent.setElementAt(childPos, fromSibling);

        // Si tienen hijos, mover también el primer hijo del hermano
        if (child.hasChildren() && rightSibling.hasChildren()) {
            InternalNode<K, V> childInternal = (InternalNode<K, V>) child;
            InternalNode<K, V> siblingInternal = (InternalNode<K, V>) rightSibling;

            Node<K, V> childToMove = siblingInternal.getChildren().removeFirst();
            childInternal.addChild(childToMove);
        }
    }

    // merge con hermano izquierd
    private void mergeWithLeftSibling(InternalNode<K, V> parent, int childPos, int maxSize) {
        Node<K, V> child = parent.getChild(childPos);
        Node<K, V> leftSibling = parent.getChild(childPos - 1);

        // Bajar la clave separadora del padre al hermano izquierdo
        NodeElement<K, V> separator = parent.getElementAt(childPos - 1);
        parent.removeElementAt(childPos - 1);
        leftSibling.addElement(separator);

        // Copiar todas las claves del hijo al hermano izquierdo
        for (int i = 0; i < child.size(); i++) {
            leftSibling.addElement(child.getElementAt(i));
        }

        // Si tienen hijos, copiar también los hijos
        if (child.hasChildren() && leftSibling.hasChildren()) {
            InternalNode<K, V> childInternal = (InternalNode<K, V>) child;
            InternalNode<K, V> siblingInternal = (InternalNode<K, V>) leftSibling;

            for (Node<K, V> grandchild : childInternal.getChildren()) {
                siblingInternal.addChild(grandchild);
            }
        }

        // Eliminar el hijo del padre
        parent.getChildren().remove(childPos);
        logger.debug("Merge completado: hermano izquierdo ahora tiene {} elementos",
                leftSibling.size());
    }

    // ahora con el derecho
    private void mergeWithRightSibling(InternalNode<K, V> parent, int childPos, int maxSize) {
        Node<K, V> child = parent.getChild(childPos);
        Node<K, V> rightSibling = parent.getChild(childPos + 1);

        // Bajar la clave separadora del padre al hijo
        NodeElement<K, V> separator = parent.getElementAt(childPos);
        parent.removeElementAt(childPos);
        child.addElement(separator);

        // Copiar todas las claves del hermano derecho al hijo
        for (int i = 0; i < rightSibling.size(); i++) {
            child.addElement(rightSibling.getElementAt(i));
        }

        // Si tienen hijos, copiar también los hijos
        if (child.hasChildren() && rightSibling.hasChildren()) {
            InternalNode<K, V> childInternal = (InternalNode<K, V>) child;
            InternalNode<K, V> siblingInternal = (InternalNode<K, V>) rightSibling;

            for (Node<K, V> grandchild : siblingInternal.getChildren()) {
                childInternal.addChild(grandchild);
            }
        }

        // Eliminar el hermano derecho del padre
        parent.getChildren().remove(childPos + 1);
        logger.debug("Merge completado: hijo ahora tiene {} elementos",
                child.size());
    }

}


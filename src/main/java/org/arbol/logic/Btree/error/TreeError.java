package org.arbol.logic.Btree.error;

public sealed interface TreeError {

    record EmptyTreeError() implements TreeError {
    }
}

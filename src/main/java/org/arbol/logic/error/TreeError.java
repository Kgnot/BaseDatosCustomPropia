package org.arbol.logic.error;

public sealed interface TreeError {

    record EmptyTreeError() implements TreeError {
    }
}

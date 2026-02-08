package org.arbol.logic.error;

import java.util.function.Function;

public sealed interface Result<V, E> {

    record Success<V, E>(V value) implements Result<V, E> {
    }

    record Failure<V, E>(E error) implements Result<V, E> {
    }

    // métodos para mapear el error
    default boolean isSuccess() {
        return this instanceof Success<V, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<V, E>;
    }

    // Intentamos mapear el valor, si es un error, lo dejamos igual
    default <U> Result<U, E> map(Function<V, U> mapper) {
        return switch (this) {
            case Success<V, E>(V value) -> new Success<U, E>(mapper.apply(value));
            case Failure<V, E>(E error) -> new Failure<U, E>(error);
        };
    }

    // intentamos obtener el valor
    default V unwrap() {
        return switch (this) {
            case Success<V, E>(V value) -> value;
            case Failure<V, E>(E error) -> throw new RuntimeException("Error de unwrap: " + error);
        };
    }

}

package org.arbol.logic.structures.table;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.tree.TreeBPlusDisk;
import org.arbol.utils.Result;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Table<K extends Comparable<K> & Serializable, V extends Serializable> {

    protected TreeBPlusDisk<K, V> tree;
    protected String name;

    public Table(String name, int order) {
        this.name = name;
        this.tree = new TreeBPlusDisk<>(name, order);
    }

    public Result<Void, NodeError.DuplicateKeyError> insert(K key, V value) {
        return tree.insert(new NodeElement<>(key, value));
    }

    public Result<V, NodeError.NodeNotFoundError> select(K key) {
        var res = tree.search(key);
        return res.isSuccess()
                ? new Result.Success<>(res.unwrap().value())
                : new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }

    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return tree.delete(key);
    }

    public List<V> findAll() {
        return tree.findAll().stream()
                .map(NodeElement::value)
                .toList();
    }

    public List<V> findAll(int offset, int limit) {
        return tree.findAll(offset, limit).stream()
                .map(NodeElement::value)
                .toList();
    }

    public String getName() {
        return name;
    }

    public void close() {
        tree.close();
    }

    // Busquedas dinamicas:
    /*
     * Escanea la tabla completa en disco y filtra usando un Predicado.
     * Útil para tablas grandes sin hacer un findAll() que ocupa RAM.
     */
    public List<V> scan(Predicate<V> filter) {
        List<V> result = new ArrayList<>();
        tree.scan(record -> {
            if (filter.test(record)) {
                result.add(record);
            }
        });
        return result;
    }

    // Ahora ejecutamos una acción por cada registro
    public void scan(Consumer<V> action) {
        tree.scan(action);
    }

    public void scanEntries(Consumer<NodeElement<K, V>> action) {
        tree.scanElements(action);
    }

    // Busqueda dinamica por nombre del atributo: Si no es PK, será lenta
    // TODO, mirar como hacer Indexación aquí.
    public List<V> findByField(String fieldName, Object value) {
        List<V> result = new ArrayList<>();
        tree.scan(record -> {
            try {
                Method getter = record.getClass().getMethod(fieldName); // esto ya que son records
                Object fieldValue = getter.invoke(record);

                // hacemos la comparación simple:
                if (fieldValue != null && fieldValue.equals(value)) {
                    result.add(record);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
                try {
                    //esto por si no es record, si no clase
                    Field field = record.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(record);
                    if (value.equals(fieldValue)) {
                        result.add(record);
                    }
                } catch (Exception ex) {
                    // Ignorar registros que no coincidan
                }
            }
        });
        return result;
    }
}

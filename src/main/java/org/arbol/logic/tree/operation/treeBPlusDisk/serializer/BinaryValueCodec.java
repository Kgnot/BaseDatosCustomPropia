package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.database.models.Route;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.Trips;
import org.arbol.database.models.key.StopTimesKey;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

final class BinaryValueCodec {

    private static final byte TYPE_NULL = 0;
    private static final byte TYPE_STRING = 1;
    private static final byte TYPE_INTEGER = 2;
    private static final byte TYPE_LONG = 3;
    private static final byte TYPE_DOUBLE = 4;
    private static final byte TYPE_DATE = 5;
    private static final byte TYPE_STOP_TIMES_KEY = 6;
    private static final byte TYPE_STOP = 7;
    private static final byte TYPE_ROUTE = 8;
    private static final byte TYPE_TRIPS = 9;
    private static final byte TYPE_STOP_TIMES = 10;

    private BinaryValueCodec() {
    }

    static int sizeOfObject(Object value) {
        if (value == null) {
            return 1;
        }
        if (value instanceof String s) {
            return 1 + sizeOfStringPayload(s);
        }
        if (value instanceof Integer) {
            return 1 + Integer.BYTES;
        }
        if (value instanceof Long) {
            return 1 + Long.BYTES;
        }
        if (value instanceof Double) {
            return 1 + Double.BYTES;
        }
        if (value instanceof Date) {
            return 1 + Long.BYTES;
        }
        if (value instanceof StopTimesKey key) {
            return 1 + sizeOfStringPayload(key.tripId()) + Integer.BYTES;
        }
        if (value instanceof Stop stop) {
            return 1
                    + sizeOfStringPayload(stop.stopId())
                    + sizeOfStringPayload(stop.stopCode())
                    + sizeOfStringPayload(stop.stopName())
                    + Double.BYTES
                    + Double.BYTES;
        }
        if (value instanceof Route route) {
            return 1
                    + sizeOfStringPayload(route.routeId())
                    + sizeOfStringPayload(route.shortName())
                    + sizeOfStringPayload(route.longName())
                    + sizeOfStringPayload(route.desc())
                    + Integer.BYTES
                    + sizeOfStringPayload(route.routeColor())
                    + sizeOfStringPayload(route.routeTextColor())
                    + Integer.BYTES;
        }
        if (value instanceof Trips trips) {
            return 1
                    + sizeOfStringPayload(trips.routeId())
                    + sizeOfStringPayload(trips.serviceId())
                    + sizeOfStringPayload(trips.shapeId());
        }
        if (value instanceof StopTimes stopTimes) {
            return 1
                    + sizeOfStringPayload(stopTimes.arrivalTime())
                    + Long.BYTES
                    + sizeOfStringPayload(stopTimes.stopId())
                    + Integer.BYTES;
        }

        throw new IllegalArgumentException("Tipo no soportado para serializacion binaria: " + value.getClass().getName());
    }

    static void writeObject(ByteBuffer buffer, Object value) {
        if (value == null) {
            buffer.put(TYPE_NULL);
            return;
        }
        if (value instanceof String s) {
            buffer.put(TYPE_STRING);
            writeStringPayload(buffer, s);
            return;
        }
        if (value instanceof Integer i) {
            buffer.put(TYPE_INTEGER);
            buffer.putInt(i);
            return;
        }
        if (value instanceof Long l) {
            buffer.put(TYPE_LONG);
            buffer.putLong(l);
            return;
        }
        if (value instanceof Double d) {
            buffer.put(TYPE_DOUBLE);
            buffer.putDouble(d);
            return;
        }
        if (value instanceof Date date) {
            buffer.put(TYPE_DATE);
            buffer.putLong(date.getTime());
            return;
        }
        if (value instanceof StopTimesKey key) {
            buffer.put(TYPE_STOP_TIMES_KEY);
            writeStringPayload(buffer, key.tripId());
            buffer.putInt(key.stopSequence());
            return;
        }
        if (value instanceof Stop stop) {
            buffer.put(TYPE_STOP);
            writeStringPayload(buffer, stop.stopId());
            writeStringPayload(buffer, stop.stopCode());
            writeStringPayload(buffer, stop.stopName());
            buffer.putDouble(stop.stopLat());
            buffer.putDouble(stop.stopLon());
            return;
        }
        if (value instanceof Route route) {
            buffer.put(TYPE_ROUTE);
            writeStringPayload(buffer, route.routeId());
            writeStringPayload(buffer, route.shortName());
            writeStringPayload(buffer, route.longName());
            writeStringPayload(buffer, route.desc());
            buffer.putInt(route.agencyId());
            writeStringPayload(buffer, route.routeColor());
            writeStringPayload(buffer, route.routeTextColor());
            buffer.putInt(route.routeType());
            return;
        }
        if (value instanceof Trips trips) {
            buffer.put(TYPE_TRIPS);
            writeStringPayload(buffer, trips.routeId());
            writeStringPayload(buffer, trips.serviceId());
            writeStringPayload(buffer, trips.shapeId());
            return;
        }
        if (value instanceof StopTimes stopTimes) {
            buffer.put(TYPE_STOP_TIMES);
            writeStringPayload(buffer, stopTimes.arrivalTime());
            buffer.putLong(stopTimes.departureTime().getTime());
            writeStringPayload(buffer, stopTimes.stopId());
            buffer.putInt(stopTimes.timepoint());
            return;
        }

        throw new IllegalArgumentException("Tipo no soportado para serializacion binaria: " + value.getClass().getName());
    }

    static Object readObject(ByteBuffer buffer) {
        byte type = buffer.get();
        return switch (type) {
            case TYPE_NULL -> null;
            case TYPE_STRING -> readStringPayload(buffer);
            case TYPE_INTEGER -> buffer.getInt();
            case TYPE_LONG -> buffer.getLong();
            case TYPE_DOUBLE -> buffer.getDouble();
            case TYPE_DATE -> new Date(buffer.getLong());
            case TYPE_STOP_TIMES_KEY -> new StopTimesKey(readStringPayload(buffer), buffer.getInt());
            case TYPE_STOP -> new Stop(
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    buffer.getDouble(),
                    buffer.getDouble()
            );
            case TYPE_ROUTE -> new Route(
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    buffer.getInt(),
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    buffer.getInt()
            );
            case TYPE_TRIPS -> new Trips(
                    readStringPayload(buffer),
                    readStringPayload(buffer),
                    readStringPayload(buffer)
            );
            case TYPE_STOP_TIMES -> new StopTimes(
                    readStringPayload(buffer),
                    new Date(buffer.getLong()),
                    readStringPayload(buffer),
                    buffer.getInt()
            );
            default -> throw new IllegalStateException("Tag de tipo binario desconocido: " + type);
        };
    }

    private static int sizeOfStringPayload(String value) {
        if (value == null) {
            return 4;
        }
        return 4 + value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static void writeStringPayload(ByteBuffer buffer, String value) {
        if (value == null) {
            buffer.putInt(-1);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    private static String readStringPayload(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length < 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}


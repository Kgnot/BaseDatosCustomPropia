package org.arbol.logic.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class StorageManager {
    private static final int PAGE_SIZE = 4096; // 4kb
    private static final int MAGIC = 0x42505431; // "BPT1"
    private static final int HEADER_SIZE = 13; // magic(4) + flag(1) + originalLen(4) + payloadLen(4)
    private static final byte FLAG_RAW = 0;
    private static final byte FLAG_COMPRESSED = 1;
    // Por defecto desactivado para priorizar throughput de ingestión.
    private static final boolean COMPRESSION_ENABLED =
            Boolean.parseBoolean(System.getProperty("arbol.storage.compress", "false"));
    private final String filePath;
    private final RandomAccessFile raf;
    private boolean closed;

    public StorageManager(String fileName) {
        this.filePath = "data/" + fileName + ".dat";
        ensureFileExists();
        try {
            this.raf = new RandomAccessFile(filePath, "rw");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el archivo de storage", e);
        }
    }

    private void ensureFileExists() {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!path.toFile().exists()) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized byte[] readPage(long pageId) {
        try {
            long offset = pageId * PAGE_SIZE;
            if (offset >= raf.length()) return null; // aun no existe

            raf.seek(offset);
            byte[] pageData = new byte[PAGE_SIZE];
            raf.readFully(pageData);

            ByteBuffer buffer = ByteBuffer.wrap(pageData);
            int magic = buffer.getInt();

            // Compatibilidad hacia atrás: páginas antiguas sin cabecera.
            if (magic != MAGIC) {
                return pageData;
            }

            byte flag = buffer.get();
            int originalLen = buffer.getInt();
            int payloadLen = buffer.getInt();

            if (originalLen < 0 || payloadLen < 0 || payloadLen > PAGE_SIZE - HEADER_SIZE) {
                throw new RuntimeException("Cabecera de página corrupta");
            }

            byte[] payload = new byte[payloadLen];
            buffer.get(payload);

            if (flag == FLAG_RAW) {
                return payload;
            }

            if (flag == FLAG_COMPRESSED) {
                Inflater inflater = new Inflater();
                try {
                    inflater.setInput(payload);
                    byte[] out = new byte[originalLen];
                    int len = inflater.inflate(out);
                    if (len != originalLen) {
                        throw new RuntimeException("No se pudo descomprimir completamente la página");
                    }
                    return out;
                } catch (DataFormatException e) {
                    throw new RuntimeException("Página comprimida corrupta", e);
                } finally {
                    inflater.end();
                }
            }

            throw new RuntimeException("Flag de compresión desconocido: " + flag);
        } catch (IOException e) {
            throw new RuntimeException("Problema leyendo la pagina", e);
        }
    }

    public synchronized void writePage(long pageId, byte[] data) {
        if (data.length > PAGE_SIZE - HEADER_SIZE) {
            throw new IllegalArgumentException(
                    "La pagina serializada excede el tamaño de pagina (" + data.length + " > " + PAGE_SIZE + ")"
            );
        }

        try {
            long offset = pageId * PAGE_SIZE;
            raf.seek(offset);

            byte[] payload = data;
            byte flag = FLAG_RAW;

            if (COMPRESSION_ENABLED) {
                byte[] compressed = compress(data);
                if (compressed.length < data.length) {
                    payload = compressed;
                    flag = FLAG_COMPRESSED;
                }
            }

            byte[] pageData = new byte[PAGE_SIZE];
            ByteBuffer buffer = ByteBuffer.wrap(pageData);
            buffer.putInt(MAGIC);
            buffer.put(flag);
            buffer.putInt(data.length);
            buffer.putInt(payload.length);
            buffer.put(payload);

            raf.write(pageData);
        } catch (IOException e) {
            throw new RuntimeException("Problema escribiendo la pagina", e);
        }
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public synchronized long getTotalPages() {
        try {
            long length = raf.length();
            if (length == 0) {
                return 0;
            }
            return (length + PAGE_SIZE - 1) / PAGE_SIZE;
        } catch (IOException e) {
            throw new RuntimeException("Problema obteniendo total de paginas", e);
        }
    }

    private byte[] compress(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        try {
            deflater.setInput(input);
            deflater.finish();
            byte[] out = new byte[input.length + 64];
            int compressedLen = deflater.deflate(out);
            return Arrays.copyOf(out, compressedLen);
        } finally {
            deflater.end();
        }
    }

    public synchronized void close() {
        if (closed) {
            return;
        }
        try {
            raf.close();
            closed = true;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cerrar el archivo de storage", e);
        }
    }


}

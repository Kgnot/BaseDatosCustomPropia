package org.arbol.logic.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageManager {
    private static final int PAGE_SIZE = 4096; // 4kb
    private final String filePath;

    public StorageManager(String fileName) {
        this.filePath = "data/" + fileName + ".dat";
        ensureFileExists();
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

    public byte[] readPage(long pageId) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long offset = pageId * PAGE_SIZE;
            if (offset >= raf.length()) return null; // aun no existe

            raf.seek(offset);
            byte[] data = new byte[PAGE_SIZE];
            raf.readFully(data);
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Problema leyendo la pagina", e);
        }
    }

    public void writePage(long pageId, byte[] data) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            long offset = pageId * PAGE_SIZE;
            raf.seek(offset);
            byte[] pageData = new byte[PAGE_SIZE];
            int length = Math.min(data.length, PAGE_SIZE);
            System.arraycopy(data, 0, pageData, 0, length);
            raf.write(pageData);
        } catch (IOException e) {
            throw new RuntimeException("Problema escribiendo la pagina", e);
        }
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public long getTotalPages() {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long length = raf.length();
            if (length == 0) {
                return 0;
            }
            return (length + PAGE_SIZE - 1) / PAGE_SIZE;
        } catch (IOException e) {
            throw new RuntimeException("Problema obteniendo total de paginas", e);
        }
    }


}

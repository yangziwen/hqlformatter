package net.yangziwen.hqlformatter.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;

public class FileUtils {

    private FileUtils() {}

    public static <T> T handleFileTree(File dir, FileFilter filter, LineHandler<T> handler) {
        if (dir == null) {
            throw new IllegalArgumentException("dir cannot be null!");
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(String.format("file %s is not a directory", dir.getAbsolutePath()));
        }
        for (File file : dir.listFiles(filter)) {
            if (file.isDirectory()) {
                handleFileTree(file, filter, handler);
            } else {
                handleFile(file, handler);
            }
        }
        return handler.getResult();
    }

    public static <T> T handleFiles(List<File> files, LineHandler<T> handler) {
        for (File file : files) {
            handleFile(file, handler);
        }
        return handler.getResult();
    }

    public static <T> T handleFile(File file, LineHandler<T> handler) {
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                handler.handle(file, reader.getLineNumber(), line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(reader);
        }
        return handler.getResult();
    }

    public static interface LineHandler<T> {

        void handle(File file, int lineNumber, String line);

        T getResult();

    }

}

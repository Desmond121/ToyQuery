package edu.uob.tables;

import edu.uob.exceptions.TableException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TableIO {
    public static final String FILE_SUFFIX = ".tab";

    public static void save(Table table, File file) throws TableException {
        try {
            Files.writeString(file.toPath(), table.toString());
        } catch (IOException e) {
            throw new TableException.WriteTableFailedException(file.toString());
        }

    }

    public static Table load(File file) throws TableException {
        try {
            return new Table(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new TableException.ReadTableFailedException(file.toString());
        }
    }
}

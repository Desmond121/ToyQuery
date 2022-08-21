package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.ParserException;
import edu.uob.exceptions.QueryException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CommandDROP extends Command {
    public CommandDROP(String name, String type) throws ParserException {
        if (type.equalsIgnoreCase("table")) {
            this.tableName = name;
        }
        else if (type.equalsIgnoreCase("database")) {
            this.databaseName = name;
        }
        else throw new ParserException.InvalidTokenException(type);
    }

    @Override
    public String query(DBServer server) throws QueryException {
        String result = "[ERROR] Failed to get name of table or database.";
        if (databaseName != null) {
            result = dropDatabase(getDatabaseFile(server));
            if (server.getDatabaseName().equals(databaseName)) {
                server.setDatabaseName(null); // reset USE.
            }
        }
        else if (tableName != null) {
            result = dropTable(getTableFile(server));
        }
        return result;
    }

    private String dropTable(File tableFile) {
        String result = "[ERROR] Table not exist.";
        if (tableFile.exists()) {
            result = "[ERROR] Failed to delete table.";
            if (tableFile.delete()) {
                result = "[OK]";
            }
        }
        return result;
    }

    private String dropDatabase(File databaseFile) {
        File[] files;
        String result = "[ERROR] Database not exist.";
        if ((files = databaseFile.listFiles()) != null) { // is directory.
            try {
                // remove all table.
                for (File file : files) {
                    Files.delete(file.toPath());
                }
                // remove database
                Files.delete(databaseFile.toPath());
                result = "[OK]";
            } catch (IOException e) {
                result = "[ERROR] Failed to delete database.";
            }
        }
        return result;
    }
}

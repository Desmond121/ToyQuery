package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.ParserException;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.List;

public class CommandCREATE extends Command {
    private List<String> attributeList;

    public CommandCREATE(String name, String type, List<String> attributeList) throws ParserException {
        if (type.equalsIgnoreCase("table")) {
            this.tableName = name;
            this.attributeList = attributeList;
        }
        else if (type.equalsIgnoreCase("database")) {
            this.databaseName = name;
        }
        else throw new ParserException.InvalidTokenException(type);
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException {
        String result = "[ERROR] Failed to get name of table or database.";
        if (databaseName != null) {
            result = createDatabase(getDatabaseFile(server));
        }
        if (tableName != null) {
            result = createTable(getTableFile(server));
        }
        return result;
    }

    private String createDatabase(File newDatabase) {
        String result = "[ERROR] Database " + newDatabase.getName() + " exist.";
        if (!newDatabase.isDirectory()) { // not exist
            result = "[ERROR] Failed to create database.";
            if (newDatabase.mkdir()) { // tried but failed.
                result = "[OK]";
            }
        }
        return result;
    }

    private String createTable(File tableFile) throws TableException {
        String result = "[ERROR] Table " + tableFile + " already existed.";
        if (!tableFile.exists()) {
            Table table = getNewTable();
            TableIO.save(table, tableFile);
            result = "[OK]";
        }
        return result;
    }

    private Table getNewTable() throws TableException {
        Table table = new Table();
        if (attributeList != null) {
            for (String attribute : attributeList) {
                table.addAttribute(attribute);
            }
        }
        return table;
    }
}

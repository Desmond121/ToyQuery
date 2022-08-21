package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandALTER extends Command {
    private final String alterationType;

    public CommandALTER(String tableName, String alterationType, String attributeName) {
        this.tableName = tableName;
        this.alterationType = alterationType;
        this.attributeList = new ArrayList<>(List.of(attributeName));
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException {
        File tableFile = getTableFile(server);
        Table table = TableIO.load(tableFile);
        String result = "[ERROR] Alteration type should be \"ADD\" or \"DROP\".";
        if (alterationType.equalsIgnoreCase("DROP")) {
            dropAttribute(table, tableFile);
            result = "[OK]";
        }
        if (alterationType.equalsIgnoreCase("ADD")) {
            addAttribute(table, tableFile);
            result = "[OK]";
        }
        return result;
    }

    private void dropAttribute(Table table, File tableFile) throws TableException {
        table.dropAttribute(attributeList.get(0));
        TableIO.save(table, tableFile);
    }

    private void addAttribute(Table table, File tableFile) throws TableException {
        table.addAttribute(attributeList.get(0));
        TableIO.save(table, tableFile);
    }
}

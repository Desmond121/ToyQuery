package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.List;

public class CommandINSERT extends Command {

    public CommandINSERT(String tableName, List<String> valueList) {
        this.tableName = tableName;
        this.valueList = valueList;
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException {
        File tableFile = getTableFile(server);
        Table table = TableIO.load(tableFile);
        // this method will check whether number of values is correct.
        table.insertRecord(valueList);
        TableIO.save(table, tableFile);
        return "[OK]";
    }
}

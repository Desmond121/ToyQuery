package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.List;
import java.util.Set;

public class CommandDELETE extends Command {
    public CommandDELETE(String tableName, List<String> conditionTokens) {
        this.tableName = tableName;
        this.conditionTokens = conditionTokens;
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException, ConditionException, ValueException {
        File tableFile = getTableFile(server);
        Table table = TableIO.load(tableFile);
        // solve condition.
        Set<Integer> idSet = getConditionResult(table);
        // delete records
        for (Integer id : idSet) {
            table.deleteRecord(id);
        }
        // save table
        TableIO.save(table, tableFile);
        return "[OK] " + idSet.size() + " record(s) deleted.";
    }
}

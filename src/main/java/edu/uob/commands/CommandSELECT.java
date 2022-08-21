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

public class CommandSELECT extends Command {
    private boolean hasId;

    public CommandSELECT(String tableName, List<String> attributes, List<String> conditionTokens) {
        this.tableName = tableName;
        this.attributeList = attributes;
        this.conditionTokens = conditionTokens;


    }

    @Override
    public String query(DBServer server) throws QueryException, TableException, ConditionException, ValueException {
        File tableFile = getTableFile(server);
        Table table = TableIO.load(tableFile);
        // check <WildAttribute>
        getWildAttributeList(table);
        // set flag and remove id;
        if (attributeList.contains("id")) {
            hasId = true;
            attributeList.remove("id");
        }
        // solve condition.
        Set<Integer> idSet = getConditionResult(table);
        // get result table
        Table resultTable = getResultTable(table, idSet);
        // get query result
        String result = hasId ? resultTable.toString() : resultTable.toStringNoId();
        return "[OK] " + idSet.size() + " record(s) found.\n" + result;
    }
}

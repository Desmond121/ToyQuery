package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandUPDATE extends Command {
    public CommandUPDATE(String tableName, List<String> attributeList, List<String> valueList, List<String> conditionTokens) {
        this.tableName = tableName;
        this.attributeList = attributeList;
        this.valueList = valueList;
        this.conditionTokens = conditionTokens;
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException, ConditionException, ValueException {
        File tableFile = getTableFile(server);
        Table table = TableIO.load(tableFile);
        // check duplicate
        checkAttributeListDuplicate();
        // solve condition.
        Set<Integer> idSet = getConditionResult(table);
        // update values
        for (Integer id : idSet) {
            for (int i = 0; i < valueList.size(); i++) {
                table.setValue(attributeList.get(i), valueList.get(i), id);
            }
        }
        TableIO.save(table, tableFile);
        return "[OK] Attributes of " + idSet.size() + " record(s) have been updated.";
    }

    // SELECT, CREATE, UPDATE involve <attributeList>, which cannot be duplicate.
    // However, SELECT and CREATE use table.addAttribute() method which check duplication.
    // so method checkAttributeListDuplicate() is only applied to UPDATE.
    private void checkAttributeListDuplicate() throws TableException {
        HashSet<String> noDuplicate = new HashSet<>();
        for (String attribute : attributeList) {
            if (!noDuplicate.add(attribute)) {
                throw new TableException.AttributeDuplicatedException(attribute);
            }
        }
    }
}

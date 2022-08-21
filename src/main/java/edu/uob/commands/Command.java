package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.conditions.ConditionSolver;
import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Command {
    protected List<String> attributeList;
    protected List<String> valueList;
    protected String databaseName;
    protected String tableName;
    protected List<String> conditionTokens;

    public abstract String query(DBServer server) throws QueryException, TableException, ConditionException, ValueException;

    protected File getDatabaseFile(DBServer server, String databaseName) {
        return new File(server.getDatabaseDirectory() + File.separator + databaseName);
    }

    protected File getDatabaseFile(DBServer server) {
        return getDatabaseFile(server, this.databaseName);
    }

    protected File getTableFile(DBServer server, String tableName) throws QueryException {
        return new File(server.getDatabaseDirectory() + File.separator +
                        server.getDatabaseName() + File.separator + tableName +
                        TableIO.FILE_SUFFIX);
    }

    protected File getTableFile(DBServer server) throws QueryException {
        return getTableFile(server, this.tableName);
    }

    protected void getWildAttributeList(Table table) {
        // check <WildAttribute>
        if (attributeList.size() == 0) {
            attributeList.addAll(table.getAttributeList()); // this contain id;
        }
    }

    protected Table getResultTable(Table targetTable, Set<Integer> idSet) throws TableException {
        return getResultTable(targetTable, idSet, this.attributeList);
    }

    protected Table getResultTable(Table targetTable, Set<Integer> idSet, List<String> attributeList) throws TableException {
        Table resultTable = new Table();
        for (String attribute : attributeList) {
            resultTable.addAttribute(attribute);
        }
        for (Integer id : idSet) {
            ArrayList<String> record = new ArrayList<>();
            for (String attribute : attributeList) {
                record.add(targetTable.getValue(attribute, id));
            }
            resultTable.insertRecord(record, id);
        }
        return resultTable;
    }

    protected Set<Integer> getConditionResult(Table table) throws ConditionException, TableException, ValueException {
        Set<Integer> idSet;
        if (conditionTokens != null) {
            ConditionSolver conditionSolver = new ConditionSolver(table, conditionTokens);
            idSet = conditionSolver.solve();
        }
        else { // no condition get all ids.
            idSet = table.getIds();
        }
        return idSet;
    }
}

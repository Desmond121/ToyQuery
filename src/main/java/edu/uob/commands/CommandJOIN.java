package edu.uob.commands;

import edu.uob.DBServer;
import edu.uob.exceptions.QueryException;
import edu.uob.exceptions.TableException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;

import java.io.File;
import java.util.List;

public class CommandJOIN extends Command {
    private final String anotherTableName;
    private final String leftAttribute;
    private final String rightAttribute;

    public CommandJOIN(String leftTableName, String RightTableName, String leftAttribute, String rightAttribute) {
        this.tableName = leftTableName;
        this.anotherTableName = RightTableName;
        this.leftAttribute = leftAttribute;
        this.rightAttribute = rightAttribute;
    }

    @Override
    public String query(DBServer server) throws QueryException, TableException {
        File leftTableFile = getTableFile(server, tableName);
        File rightTableFile = getTableFile(server, anotherTableName);
        Table leftTable = TableIO.load(leftTableFile);
        Table rightTable = TableIO.load(rightTableFile);
        // get join table;
        Table resultTable = joinTable(leftTable, rightTable);
        // remove foreign key.
        if (!leftAttribute.equals("id"))
            resultTable.dropAttribute(leftAttribute);
        if (!rightAttribute.equals("id"))
            resultTable.dropAttribute(rightAttribute);
        return "[OK]\n" + resultTable;
    }

    private void importAttributes(Table target, Table source) throws TableException {
        for (String attributes : source.getAttributeList()
                                       .stream()
                                       .filter((V) -> !V.equals("id"))
                                       .toList()) {
            target.addAttribute(attributes);
        }
    }

    private Table joinTable(Table left, Table right) throws TableException {
        Table result = new Table();
        // import all attributes.
        importAttributes(result, left);
        importAttributes(result, right);
        // check whether each foreign key of left is equal with right one.
        for (Integer leftId : left.getIds()) {
            String leftForeignKey = left.getValue(leftAttribute, leftId);
            for (Integer rightId : right.getIds()) {
                String rightForeignKey = right.getValue(rightAttribute, rightId);
                // if equal, combine this row
                if (leftForeignKey.equals(rightForeignKey)) {
                    List<String> values = left.getValues(leftId);
                    values.addAll(right.getValues(rightId));
                    result.insertRecord(values);
                }
            }
        }
        return result;
    }
}

package edu.uob.conditions;

import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.TableException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Table;

import java.util.List;
import java.util.Set;

public class ConditionSolver {
    private final Table table;
    private final List<String> tokens;
    private int currentIndex;

    public ConditionSolver(Table targetTable, List<String> tokens) {
        this.table = targetTable;
        this.tokens = tokens;
        this.currentIndex = 0;
    }

    // parser has checked all tokens, no checking in solver.
    public Set<Integer> solve() throws ConditionException, ValueException, TableException {
        if (tokens.get(currentIndex).equals("(")) {
            currentIndex++;
            // <condition>
            Set<Integer> leftResult = solve();
            // ")"
            currentIndex++;
            // "AND" or "OR"
            LogicOperator logic = getLogicOperator();
            // "("
            currentIndex++;
            // <condition>
            Set<Integer> rightResult = solve();
            // ")"
            currentIndex++;
            // result
            return calculateLogic(leftResult, rightResult, logic);
        }
        // <AttributeName> <Operator> <Value>
        Condition condition = new Condition(tokens.get(currentIndex++),
                                            tokens.get(currentIndex++),
                                            tokens.get(currentIndex++));
        return findIdsMetCondition(condition);

    }

    private LogicOperator getLogicOperator() {
        String token = tokens.get(currentIndex++);
        return token.equalsIgnoreCase("AND") ? LogicOperator.AND : LogicOperator.OR;
    }

    // find ids met each condition expression.
    private Set<Integer> findIdsMetCondition(Condition condition) throws TableException, ConditionException {
        return table.getIdsByMethod(condition.getAttribute(),
                                    condition.getReferenceValue(),
                                    condition.getOperatorMethod());
    }

    private Set<Integer> calculateLogic(Set<Integer> left, Set<Integer> right, LogicOperator logic) {
        switch (logic) {
            case AND -> left.retainAll(right);
            case OR -> left.addAll(right);
        }
        return left;
    }

    private enum LogicOperator {
        AND, OR
    }
}


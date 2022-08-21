package edu.uob;

import edu.uob.conditions.ConditionSolver;
import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.TableException;
import edu.uob.exceptions.ValueException;
import edu.uob.syntax.Tokenizer;
import edu.uob.tables.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionTests {
    Table table;
    List<String> tokens;
    ConditionSolver cs;
    Set<Integer> result;

    @BeforeEach
    void init() throws TableException {
        table = new Table("""
                                  id\tname\tage\tbool\tstring
                                  1\tdesmond\t22\ttrue\tthis is a string
                                  2\tpeter\t35\tfalse\tnull
                                  3\tparker\t5\ttrue\tnull
                                  4\ttony\t50\tfalse\tshortString
                                  5\tpeaky\t66\tfalse\tnull
                                  6\tblinder\tnull\tnull\tnull
                                  """);
    }

    @Test
    void testSingleCondition() throws ConditionException, TableException, ValueException {
        // number
        tokens = Tokenizer.getTokens("age<=40");
        cs = new ConditionSolver(table, tokens);
        result = cs.solve();
        assertTrue(result.containsAll(List.of(1, 2, 3)));
        assertEquals(3, result.size());
        // string
        tokens = Tokenizer.getTokens("name like 'pe'");
        cs = new ConditionSolver(table, tokens);
        result = cs.solve();
        assertTrue(result.containsAll(List.of(2, 5)));
        assertEquals(2, result.size());
        // null
        tokens = Tokenizer.getTokens("string != Null");
        cs = new ConditionSolver(table, tokens);
        result = cs.solve();
        assertTrue(result.containsAll(List.of(1, 4)));
        assertEquals(2, result.size());
        // bool
        tokens = Tokenizer.getTokens("bool == false");
        cs = new ConditionSolver(table, tokens);
        result = cs.solve();
        assertTrue(result.containsAll(List.of(2, 4, 5)));
        assertEquals(3, result.size());
        // string with space
        tokens = Tokenizer.getTokens("string == 'this is a string'");
        cs = new ConditionSolver(table, tokens);
        result = cs.solve();
        assertTrue(result.contains(1));
        assertEquals(1, result.size());
    }

    @Test
    void testMismatchOperator() {
        // number
        tokens = Tokenizer.getTokens("name <= 'pe'");
        cs = new ConditionSolver(table, tokens);
        assertThrows(ConditionException.OperatorValueTypeNotMatchException.class,
                     cs::solve);
        // string
        tokens = Tokenizer.getTokens("name <= 'pe'");
        cs = new ConditionSolver(table, tokens);
        assertThrows(ConditionException.OperatorValueTypeNotMatchException.class,
                     cs::solve);
        // bool
        tokens = Tokenizer.getTokens("bool>false");
        cs = new ConditionSolver(table, tokens);
        assertThrows(ConditionException.OperatorValueTypeNotMatchException.class,
                     cs::solve);
    }

    @Test
    void testMultipleCondition() throws ConditionException, TableException, ValueException {
        String condition = "((id<3) AND(age==22))or((id>=2)AND(name like 'pe'))";
        List<String> tokens = Tokenizer.getTokens(condition);
        ConditionSolver cs = new ConditionSolver(table, tokens);
        Set<Integer> result = cs.solve();
        assertTrue(result.containsAll(List.of(1, 2, 5)));
        assertEquals(3, result.size());
    }
}

package edu.uob;

import edu.uob.exceptions.ValueException;
import edu.uob.tables.Type;
import edu.uob.tables.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueTests {
    @Test
    void testValueTypeMatching() throws ValueException {
        String valueString;

        valueString = "1000.123";
        assertEquals(Type.FLOAT, Value.matchValueType(valueString));

        valueString = "11123678";
        assertEquals(Type.INT, Value.matchValueType(valueString));

        valueString = "Null";
        assertEquals(Type.NULL, Value.matchValueType(valueString));

        valueString = "False";
        assertEquals(Type.BOOL, Value.matchValueType(valueString));

        valueString = "True";
        assertEquals(Type.BOOL, Value.matchValueType(valueString));

        valueString = "'This is a string.'";
        assertEquals(Type.STRING, Value.matchValueType(valueString));
    }

    @Test
    void testSameTypeCompare() {
        Value left;
        Value right;

        left = new Value("100", Type.FLOAT);
        right = new Value("100.00", Type.FLOAT);
        assertEquals(0, left.compareTo(right));

        left = new Value("100", Type.INT);
        right = new Value("120", Type.INT);
        assertTrue(left.compareTo(right) < 0);

        left = new Value("this is a string", Type.STRING);
        right = new Value("this is a string", Type.STRING);
        assertEquals(0, left.compareTo(right));

        left = new Value("True", Type.BOOL);
        right = new Value("False", Type.BOOL);
        assertTrue(left.compareTo(right) != 0);

        left = new Value("True", Type.BOOL);
        right = new Value("1000", Type.INT);
        assertEquals(-1, left.compareTo(right));
    }

    @Test
    void testDifferentTypeCompare() {
        Value left;
        Value right;

        left = new Value("100", Type.INT);
        right = new Value("100.00", Type.FLOAT); // base on
        assertEquals(0, left.compareTo(right));

        left = new Value("100", Type.INT);
        right = new Value("100.00", Type.STRING); // base on
        assertTrue(left.compareTo(right) != 0);

        left = new Value("100", Type.INT);
        right = new Value("100.00", Type.STRING); // base on
        assertTrue(left.compareTo(right) != 0);

        left = new Value("True", Type.BOOL);
        right = new Value("1000", Type.INT);// base on
        assertEquals(-1, left.compareTo(right));
    }
}

package edu.uob;

import edu.uob.syntax.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TokenizerTests {

    @Test
    void multipleWhitespaceTest() {
        String command = "Select * From table    where     age     >= 40 ;";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"Select", "*", "From", "table", "where", "age", ">=", "40", ";"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void withoutSomeWhitespaceTest() {
        String command = "Select * From table where age>=40;";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"Select", "*", "From", "table", "where", "age", ">=", "40", ";"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void multipleConditionTest() {
        String command = "WHERE (attri1 >= 10)AND((attri2 Like'na') or (attri3 != 30) )";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"WHERE", "(", "attri1", ">=", "10", ")", "AND", "(", "(", "attri2", //
                "Like", "'na'", ")", "or", "(", "attri3", "!=", "30", ")", ")"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void multipleAttributeTest() {
        String command = "Select attribute1, attribute2  ,attribute3,attribute4 From table;";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"Select", "attribute1", ",", "attribute2", ",", "attribute3", //
                ",", "attribute4", "From", "table", ";"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void multipleValueListTest() {
        String command = "INSERT INTO table VALUES('name','home address', -18,+32.99);";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"INSERT", "INTO", "table", "VALUES", "(", "'name'", ",", //
                "'home address'", ",", "-18", ",", "+32.99", ")", ";"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void updateValuesTest() {
        String command = "UPDATE table SET name='name1',age=+20 WHERE id==20;";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"UPDATE", "table", "SET", "name", "=", "'name1'", ",", //
                "age", "=", "+20", "WHERE", "id", "==", "20", ";"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void createTableWithAttributesTest() {
        String command = "CREATE TABLE tablename (attri1, attri2,attri3);";
        String[] actual = Tokenizer.getTokens(command).toArray(String[]::new);
        String[] expected = {"CREATE", "TABLE", "tablename", "(", "attri1", ",", "attri2", //
                ",", "attri3", ")", ";"};
        assertArrayEquals(expected, actual);

    }
}

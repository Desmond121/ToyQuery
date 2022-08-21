package edu.uob;

import edu.uob.exceptions.TableException;
import edu.uob.tables.Table;
import edu.uob.tables.TableIO;
import edu.uob.tables.Type;
import edu.uob.tables.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;


final class TableTests {
    Table table;
    File directory;

    @BeforeEach
    void init(@TempDir File tempDir) {
        directory = tempDir;
    }

    @Test
    void testAddDropAttribute() throws TableException {
        table = new Table();
        // add attribute.
        table.addAttribute("name");
        table.addAttribute("number");
        assertArrayEquals(List.of("id", "name", "number").toArray(),
                          table.getAttributeList().toArray());
        // drop attribute.
        table.dropAttribute("name");
        assertArrayEquals(List.of("id", "number").toArray(),
                          table.getAttributeList().toArray());
        // drop not exist
        assertThrows(TableException.class, () -> table.dropAttribute("phone"));
        // add duplicate
        assertThrows(TableException.class, () -> table.addAttribute("number"));
    }

    @Test
    void testInsertDeleteRecords() throws TableException {
        table = new Table();
        // add attribute.
        table.addAttribute("name");
        table.addAttribute("number");
        assertArrayEquals(List.of("id", "name", "number").toArray(),
                          table.getAttributeList().toArray());
        // insert record.
        table.insertRecord(List.of("tony", "123"));
        table.insertRecord(List.of("jack", "456"));
        // insert with id
        table.insertRecord(List.of("tom", "456"), 4);
        // insert duplicate id
        assertThrows(TableException.class,
                     () -> table.insertRecord(List.of("tom", "456"), 4));
        // delete records
        table.deleteRecord(4);
        // delete id not exist.
        assertThrows(TableException.class, () -> table.deleteRecord(20));
    }

    @Test
    void testReadTableFromFile() throws TableException, IOException {
        File tableFile = new File(directory + File.separator + "tempTable.tab");
        FileWriter fileWriter = new FileWriter(tableFile);
        String fileString = """
                id\tName\tAge\tEmail
                1\tBob\t21\tbob@bob.net
                2\tHarry\t32\tharry@harry.com
                3\tChris\t42\tchris@chris.ac.uk
                """;

        fileWriter.write(fileString);
        fileWriter.flush();
        fileWriter.close();
        table = TableIO.load(tableFile);
        assertEquals(fileString, table.toString());
    }

    @Test
    void testWriteTableToFile() throws TableException, IOException {
        File file = new File(directory + File.separator + "tempTable.tab");

        table = new Table(); // new table
        for (String attribute : Arrays.asList("Name", "Age", "Whatever")) {
            table.addAttribute(attribute);
        }
        table.insertRecord(Arrays.asList("Jack", "19", "bar"));
        table.insertRecord(Arrays.asList("Micky", "28", "foo"));

        TableIO.save(table, file);
        String actual = Files.readString(file.toPath());
        String expected = "id\tName\tAge\tWhatever\n1\tJack\t19\tbar\n2\tMicky\t28\tfoo\n";
        assertEquals(expected, actual);
    }

    @Test
    void testAlterTable() throws TableException, IOException {
        File file = new File(directory + File.separator + "tempTable.tab");
        Files.writeString(file.toPath(),
                          "id\tName\tAge\tWhatever\n1\tJack\t19\tbar\n2\tMicky\t28\tfoo\n");
        table = TableIO.load(file); // load table
        table.setValue("Age", "32", 2);
        TableIO.save(table, file);
        assertEquals("id\tName\tAge\tWhatever\n1\tJack\t19\tbar\n2\tMicky\t32\tfoo\n",
                     Files.readString(file.toPath()));
    }

    @Test
    void testConditionSearch() throws TableException {
        table = new Table("""
                                  id\tname\tage\tbool\tstring
                                  1\tdesmond\t22\ttrue\tthis is a string
                                  2\tpeter\t35\tfalse\tnull
                                  3\tparker\t5\ttrue\tnull
                                  4\ttony\t50\tfalse\tshortString
                                  5\tpeaky\t66\tfalse\tnull
                                  6\tblinder\tnull\tnull\tnull
                                  """);
        BiPredicate<Value, Value> biggerOrEqual =  //
                (value, referenceValue) -> value.compareTo(referenceValue) >= 0;
        Set<Integer> result = table.getIdsByMethod("age", new Value("25", Type.INT),
                                                   biggerOrEqual);
        assertArrayEquals(List.of(2, 4, 5).toArray(), result.toArray());
    }
}

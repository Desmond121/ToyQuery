package edu.uob;

import edu.uob.commands.*;
import edu.uob.exceptions.ParserException;
import edu.uob.syntax.Parser;
import edu.uob.syntax.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTests {
    String input;
    List<String> tokens;
    Parser parser;

    @Test
    void testValidInput() throws ParserException {
        input = "use database;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandUSE.class, parser.getCommand().getClass());

        input = "select*from table where((a==1)and(c like 'string')) or((a!=100)or(d<10.00));";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandSELECT.class, parser.getCommand().getClass());

        input = "create table name (attribute,attribute,attribute);";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandCREATE.class, parser.getCommand().getClass());

        input = "drop database databaseName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandDROP.class, parser.getCommand().getClass());

        input = "delete from tableName where (a==1)and (b like '123');";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandDELETE.class, parser.getCommand().getClass());
    }

    @Test
    void testSelect() throws ParserException {
        // valid
        input = "select*from table where((a==1)and(c like 'string')) or((a!=100)or(d<10.00));";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandSELECT.class, parser.getCommand().getClass());
        // invalid --------------------------------------↓
        input = "select*from table where (a==1)and(b==2)or(a!=10);";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
        // invalid -----------------------↓
        input = "select*from table where a=1;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
        // invalid --------------------------↓
        input = "select*from table where a==something;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testUse() throws ParserException {
        // valid
        input = "use database;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandUSE.class, parser.getCommand().getClass());

        // invalid -------------------↓
        input = "use database databaseName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testDrop() throws ParserException {
        // valid
        input = "drop table tableName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandDROP.class, parser.getCommand().getClass());
        // valid
        input = "drop database databaseName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandDROP.class, parser.getCommand().getClass());

        // invalid -------------------↓
        input = "drop databaseName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testCreate() throws ParserException {
        // valid
        input = "create table tableName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandCREATE.class, parser.getCommand().getClass());
        // valid
        input = "create database databaseName;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandCREATE.class, parser.getCommand().getClass());
        // valid
        input = "create table tableName (name,phone,address);";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandCREATE.class, parser.getCommand().getClass());

        // invalid ----------------------↓
        input = "create table tableName ();";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testAlter() throws ParserException {
        // valid
        input = "alter table tableName add attribute;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandALTER.class, parser.getCommand().getClass());

        // valid
        input = "alter table tableName drop attribute;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandALTER.class, parser.getCommand().getClass());

        // invalid ----------------------↓
        input = "alter table tableName delete attribute;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testInsert() throws ParserException {
        // valid
        input = "insert into table values('String with whitespace',100,null,false,true,100.00);";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandINSERT.class, parser.getCommand().getClass());

        // invalid -------------------------↓
        input = "insert into table values(string,100,null,false,true,100.00);";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());

        // invalid -----------------------↓
        input = "insert into table values();";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testUpdate() throws ParserException {
        // valid
        input = "update table set a=1,b='abc',c=false where a==1;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandUPDATE.class, parser.getCommand().getClass());

        // invalid -------------------------------------↓
        input = "update table set a=1,b='abc',c=something where a==1;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertThrows(ParserException.class, () -> parser.getCommand());
    }

    @Test
    void testDelete() throws ParserException {
        // valid
        input = "delete from tableName where (a==1)and (b like '123');";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandDELETE.class, parser.getCommand().getClass());
    }

    @Test
    void testJoin() throws ParserException {
        // valid
        input = "join a and b on a and b;";
        tokens = Tokenizer.getTokens(input);
        parser = new Parser(tokens);
        assertEquals(CommandJOIN.class, parser.getCommand().getClass());
    }
}

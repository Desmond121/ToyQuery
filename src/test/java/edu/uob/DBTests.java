package edu.uob;

import edu.uob.exceptions.QueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class DBTests {

    private DBServer server;

    // we make a new server for every @Test (i.e. this method runs before every @Test test case)
    @BeforeEach
    void setup(@TempDir File dbDir) {
        // Notice the @TempDir annotation, this instructs JUnit to create a new temp directory somewhere
        // and proceeds to *delete* that directory when the test finishes.
        // You can read the specifics of this at
        // https://junit.org/junit5/docs/5.4.2/api/org/junit/jupiter/api/io/TempDir.html

        // If you want to inspect the content of the directory during/after a test run for debugging,
        // simply replace `dbDir` here with your own File instance that points to somewhere you know.
        // IMPORTANT: If you do this, make sure you rerun the tests using `dbDir` again to make sure it
        // still works and keep it that way for the submission.
        server = new DBServer(dbDir);
    }

    @Test
    void testUse() throws QueryException {
        assertTrue(server.handleCommand("create database office;").startsWith("[OK]"));
        // not using
        assertEquals("[ERROR] Database not specified.",
                     server.handleCommand("select*from officer;"));
        // using
        assertTrue(server.handleCommand("use office;").startsWith("[OK]"));
        assertEquals("office", server.getDatabaseName());
        // not exist
        assertTrue(server.handleCommand("use city;").startsWith("[ERROR]"));
    }

    @Test
    void testCreate() {
        // create database
        assertTrue(server.handleCommand("create database office;").startsWith("[OK]"));
        // empty create
        assertTrue(server.handleCommand("use office;").startsWith("[OK]"));
        assertTrue(server.handleCommand("create table officer;").startsWith("[OK]"));
        assertEquals("""
                             [OK] 0 record(s) found.
                             id
                             """, server.handleCommand("select*from officer;"));
        // create with attribute
        assertTrue(server.handleCommand("use office;").startsWith("[OK]"));
        assertTrue(server.handleCommand("create table rules(title,text);")
                         .startsWith("[OK]"));
        assertEquals("""
                             [OK] 0 record(s) found.
                             id\ttitle\ttext
                             """, server.handleCommand("select*from rules;"));
        // duplicate create
        assertTrue(server.handleCommand("create table rules;").startsWith("[ERROR]"));
        assertTrue(server.handleCommand("create database office;").startsWith("[ERROR]"));
    }


    @Test
    void testDrop() {
        createEmptyTable();
        // drop table
        assertTrue(server.handleCommand("drop table student;").startsWith("[OK]"));
        assertTrue(server.handleCommand("select*from  student;").startsWith("[ERROR]"));
        // drop database(if table in use get dropped, it will be set to null);
        assertTrue(server.handleCommand("drop database school;").startsWith("[OK]"));
        assertThrows(QueryException.class,
                     () -> server.getDatabaseName());
        // not exist
        assertTrue(server.handleCommand("create database school;").startsWith("[OK]"));
        assertTrue(server.handleCommand("use  school;").startsWith("[OK]"));
        assertTrue(server.handleCommand("drop table student;").startsWith("[ERROR]"));
        assertTrue(server.handleCommand("drop database office;").startsWith("[ERROR]"));
    }


    @Test
    void testAlter() {
        createEmptyTable();
        // add
        assertTrue(server.handleCommand("alter table student add phoneNumber;")
                         .startsWith("[OK]"));
        // insert a record;
        assertTrue(server.handleCommand("insert into student values('610228');")
                         .startsWith("[OK]"));
        // add
        assertTrue(
                server.handleCommand("alter table student add name;").startsWith("[OK]"));
        // the new column will be null
        assertEquals("""
                             [OK] 1 record(s) found.
                             id\tphoneNumber\tname
                             1\t610228\tNULL
                             """, server.handleCommand("select*from student;"));
        // duplicate
        assertTrue(server.handleCommand("alter table student add name;")
                         .startsWith("[ERROR]"));
        // drop
        assertTrue(server.handleCommand("alter table student drop name;")
                         .startsWith("[OK]"));
        assertEquals("""
                             [OK] 1 record(s) found.
                             id\tphoneNumber
                             1\t610228
                             """, server.handleCommand("select*from student;"));
        // not exist
        assertTrue(server.handleCommand("alter table student drop name;")
                         .startsWith("[ERROR]"));
    }

    @Test
    void testInsert() {
        createEmptyTable();
        // no attribute cannot insert;
        assertTrue(server.handleCommand("insert into student values();")
                         .startsWith("[ERROR]"));
        assertTrue(server.handleCommand("insert into student values(1);")
                         .startsWith("[ERROR]"));
        // add attribute
        assertTrue(
                server.handleCommand("alter table student add name;").startsWith("[OK]"));
        assertTrue(server.handleCommand("alter table student add phoneNumber;")
                         .startsWith("[OK]"));
        // insert
        assertTrue(server.handleCommand("insert into student values('desmond','123123');")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into student values('jack','123124');")
                         .startsWith("[OK]"));
        assertEquals("""
                             [OK] 2 record(s) found.
                             id\tname\tphoneNumber
                             1\tdesmond\t123123
                             2\tjack\t123124
                             """, server.handleCommand("select*from student;"));
        // invalid
        assertTrue(server.handleCommand("insert into student values('jack');")
                         .startsWith("[ERROR]"));
        assertTrue(server.handleCommand("insert into student values('jack','123124',1);")
                         .startsWith("[ERROR]"));
        assertTrue(server.handleCommand("insert into school values('jack','123124');")
                         .startsWith("[ERROR]"));
    }

    @Test
    void testSelect() {
        createTablesWithContent();
        String result;

        result = server.handleCommand("select studentId,score from transcript;");
        assertEquals("""
                             [OK] 5 record(s) found.
                             studentId\tscore
                             1\t85
                             2\t60
                             1\t45
                             2\t61
                             4\t35
                             """, result);

        result = server.handleCommand(
                "select*from transcript where(test=='JAVA')and(score>80);");
        assertEquals("""
                             [OK] 1 record(s) found.
                             id\ttest\tscore\tstudentId\tpass
                             1\tJAVA\t85\t1\tTRUE
                             """, result);

        result = server.handleCommand(
                "select id,name from student where((name like 'a')and(grade<4))or((id>=2)and(grade==3));");
        assertEquals("""     
                             [OK] 3 record(s) found.
                             id\tname
                             2\tDesmond
                             3\tMarty
                             4\tDany
                             """, result);
        // no such attribute-------------------------------------------↓
        assertTrue(server.handleCommand(
                                 "select id,name from student where((people like 'a')and(grade<4))or((id>=2)and(grade==3));")
                         .startsWith("[ERROR]"));
        // not match operator and value
        assertTrue(server.handleCommand("select*from transcript where test >='JAVA';")
                         .startsWith("[ERROR]"));
    }

    @Test
    void testUpdate() {
        createTablesWithContent();
        String result;
        // reset pass threshold to 65
        assertTrue(
                server.handleCommand("update transcript set pass=False where score<65;")
                      .startsWith("[OK]"));
        result = server.handleCommand("select*from transcript;");
        assertEquals("""
                             [OK] 5 record(s) found.
                             id\ttest\tscore\tstudentId\tpass
                             1\tJAVA\t85\t1\tTRUE
                             2\tJAVA\t60\t2\tFALSE
                             3\tPOSE\t45\t1\tFALSE
                             4\tPOSE\t61\t2\tFALSE
                             5\tPOSE\t35\t4\tFALSE
                             """, result);
        // update test name
        assertTrue(server.handleCommand(
                                 "update transcript set test='OOP IN JAVA' where test like 'JAVA';")
                         .startsWith("[OK]"));
        result = server.handleCommand("select*from transcript;");
        assertEquals("""
                             [OK] 5 record(s) found.
                             id\ttest\tscore\tstudentId\tpass
                             1\tOOP IN JAVA\t85\t1\tTRUE
                             2\tOOP IN JAVA\t60\t2\tFALSE
                             3\tPOSE\t45\t1\tFALSE
                             4\tPOSE\t61\t2\tFALSE
                             5\tPOSE\t35\t4\tFALSE
                             """, result);
        // no such attribute
        assertTrue(server.handleCommand("update transcript set test='name' where a==1;")
                         .startsWith("[ERROR]"));
        assertTrue(server.handleCommand(
                                 "update transcript set t='name' where name like 'a';")
                         .startsWith("[ERROR]"));
    }

    @Test
    void testDelete() {
        createTablesWithContent();
        String result;
        // delete
        assertTrue(server.handleCommand("delete from transcript where (id==3)or(id==4);")
                         .startsWith("[OK]"));
        result = server.handleCommand("select*from transcript;");
        assertEquals("""
                             [OK] 3 record(s) found.
                             id\ttest\tscore\tstudentId\tpass
                             1\tJAVA\t85\t1\tTRUE
                             2\tJAVA\t60\t2\tTRUE
                             5\tPOSE\t35\t4\tFALSE
                             """, result);

    }

    @Test
    void testJoin() {
        createTablesWithContent();
        String result;
        result = server.handleCommand("join transcript and student on studentId and id;");
        assertEquals("""
                             [OK]
                             id\ttest\tscore\tpass\tname\tgrade
                             1\tJAVA\t85\tTRUE\tJack\t4
                             2\tJAVA\t60\tTRUE\tDesmond\t3
                             3\tPOSE\t45\tFALSE\tJack\t4
                             4\tPOSE\t61\tTRUE\tDesmond\t3
                             5\tPOSE\t35\tFALSE\tDany\t2
                             """, result);
        // check whether generate new id
        assertTrue(server.handleCommand("delete from transcript where id==3;")
                         .startsWith("[OK]"));
        result = server.handleCommand("join transcript and student on studentId and id;");
        assertEquals("""
                             [OK]
                             id\ttest\tscore\tpass\tname\tgrade
                             1\tJAVA\t85\tTRUE\tJack\t4
                             2\tJAVA\t60\tTRUE\tDesmond\t3
                             3\tPOSE\t61\tTRUE\tDesmond\t3
                             4\tPOSE\t35\tFALSE\tDany\t2
                             """, result);
        // try reverse
        result = server.handleCommand("join student and transcript on id and studentId;");
        assertEquals("""     
                             [OK]
                             id\tname\tgrade\ttest\tscore\tpass
                             1\tJack\t4\tJAVA\t85\tTRUE
                             2\tDesmond\t3\tJAVA\t60\tTRUE
                             3\tDesmond\t3\tPOSE\t61\tTRUE
                             4\tDany\t2\tPOSE\t35\tFALSE
                             """, result);
    }

    @Test
    void testInvalidCommandIsAnError() {
        assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
        List<String> invalidInputs = List.of("");
        for (String invalidInput : invalidInputs) {
            assertTrue(server.handleCommand(invalidInput).startsWith("[ERROR]"));
        }
    }

    void createEmptyTable() {
        // create database and table
        assertTrue(server.handleCommand("create database school;").startsWith("[OK]"));
        assertTrue(server.handleCommand("use  school;").startsWith("[OK]"));
        assertTrue(server.handleCommand("create table student;").startsWith("[OK]"));
        assertTrue(server.handleCommand("select*from  student;").startsWith("[OK]"));
    }

    void createTablesWithContent() {
        assertTrue(server.handleCommand("use school;").startsWith("[ERROR]"));
        // create database
        assertTrue(server.handleCommand("Create database school;").startsWith("[OK]"));
        // create table
        assertTrue(server.handleCommand("create table student(name,grade);")
                         .startsWith("[OK]"));
        // insert values
        assertTrue(server.handleCommand("insert into student values('Jack',4);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into student values('Desmond',3);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into student values('Marty',2);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into student values('Dany',2);")
                         .startsWith("[OK]"));
        // create table
        assertTrue(server.handleCommand("create table transcript(test,score,studentId);")
                         .startsWith("[OK]"));
        // insert values
        assertTrue(server.handleCommand("insert into transcript values('JAVA',85,1);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into transcript values('JAVA',60,2);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into transcript values('POSE',45,1);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into transcript values('POSE',61,2);")
                         .startsWith("[OK]"));
        assertTrue(server.handleCommand("insert into transcript values('POSE',35,4);")
                         .startsWith("[OK]"));
        // alter
        assertTrue(server.handleCommand("alter table transcript add pass;")
                         .startsWith("[OK]"));
        // update value
        assertTrue(
                server.handleCommand("update transcript set pass=True where score>=50;")
                      .startsWith("[OK]"));
        assertTrue(
                server.handleCommand("update transcript set pass=false where score<50;")
                      .startsWith("[OK]"));
        // check student
        String result = server.handleCommand("select*from student;");
        assertEquals("""     
                             [OK] 4 record(s) found.
                             id\tname\tgrade
                             1\tJack\t4
                             2\tDesmond\t3
                             3\tMarty\t2
                             4\tDany\t2
                             """, result);
        // check transcript
        result = server.handleCommand("select*from transcript;");
        assertEquals("""
                             [OK] 5 record(s) found.
                             id\ttest\tscore\tstudentId\tpass
                             1\tJAVA\t85\t1\tTRUE
                             2\tJAVA\t60\t2\tTRUE
                             3\tPOSE\t45\t1\tFALSE
                             4\tPOSE\t61\t2\tTRUE
                             5\tPOSE\t35\t4\tFALSE
                             """, result);
    }

}

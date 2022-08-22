# 🗃️ Toy Query
Toy query is a light-weight SQL-like database server implemented in pure Java.

## 😋 Features
- Regex-based Tokenizer.
- Recursive Descent parser supporting nested `WHERE` condition.
- AST Interpreter.
- Socket comunication between server and client.
- Probebly Structured code.
- Support basic data type include Int, Float, Boolean, String and Null.

## 😢 Some drawbacks 
- Data type information are not maintaining for attributes but only recognized by Regex.
- Not crash-proof.
- No buffer pool, write immidiately after query.
- No concurrent support.

## 🗪 Query Language
Toy Query support some basic SQL including:
- USE: changes the database against which the following queries will be run
- CREATE: constructs a new database or table (depending on the provided parameters)
- INSERT: adds a new record (row) to an existing table
- SELECT: searches for records that match the given condition
- UPDATE: changes the existing data contained within a table
- ALTER: changes the structure (columns) of an existing table
- DELETE: removes records that match the given condition from an existing table
- DROP: removes a specified table from a database, or removes the entire database
- JOIN: performs an **inner** join on two tables (returning all permutations of all matching records)

Full [BNF](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) grammar [here](https://github.com/Desmond121/ToyQuery/blob/master/BNF.txt).


## 🚀 How to Run
### Run with idea
- Open the project.
- Run `main` method of `DBServer` and `DBClient`.
### Run with Maven
- Server:
 ```
  ./mvnw exec:java -Dexec.mainClass="edu.uob.DBServer"  
 ```
- Client:
 ```
  ./mvnw exec:java -Dexec.mainClass="edu.uob.DBClient"  
 ```

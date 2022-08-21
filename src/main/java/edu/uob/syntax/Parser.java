package edu.uob.syntax;

import edu.uob.commands.*;
import edu.uob.exceptions.ParserException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Type;
import edu.uob.tables.Value;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final String RX_PLAIN_TEXT = "^([0-9a-zA-Z]+)$";
    private static final String RX_TABLE_OR_DATABASE = "(?i)(^(table|database)$)";
    private static final String RX_COMMA_OR_RIGHT_BRACKET = "^[,)]$";
    private static final String RX_ALTERATION_TYPE = "(?i)(^(drop|add)$)";
    private static final String RX_LOGIC_OPT = "(?i)(^(and|or)$)";
    private static final String RX_COMPARE_OPT = "(?i)(==|!=|>=|<=|>|<|LIKE)";

    List<String> tokens;
    int currentIndex;

    public Parser(List<String> tokens) throws ParserException {
        // last token should be ;
        if (!tokens.get(tokens.size() - 1).equals(";"))
            throw new ParserException.MissingTokenException(";");
        // remove ;
        tokens.remove(tokens.size() - 1);
        this.tokens = tokens;
        this.currentIndex = 0;
    }

    public Command getCommand() throws ParserException {
        String firstToken = tokens.get(currentIndex++).toUpperCase();
        return switch (firstToken) {
            case "USE" -> getCommandUSE();
            case "CREATE" -> getCommandCREATE();
            case "DROP" -> getCommandDROP();
            case "ALTER" -> getCommandALTER();
            case "INSERT" -> getCommandINSERT();
            case "SELECT" -> getCommandSELECT();
            case "UPDATE" -> getCommandUPDATE();
            case "DELETE" -> getCommandDELETE();
            case "JOIN" -> getCommandJOIN();
            default -> throw new ParserException.InvalidTokenException(firstToken);
        };
    }

    private Command getCommandUSE() throws ParserException {
        checkHasNextToken("<DatabaseName>");
        String databaseName = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<DatabaseName>");
        checkRemainTokens();
        return new CommandUSE(databaseName);
    }

    private Command getCommandCREATE() throws ParserException {
        // TABLE or DATABASE
        checkHasNextToken("\"TABLE\" or \"DATABASE\"");
        String createType = getNextTokenAfterMatching(RX_TABLE_OR_DATABASE,
                                                      "\"TABLE\" or \"DATABASE\"");
        // <TableName> or <DatabaseName>
        checkHasNextToken("<TableName> or <DatabaseName>");
        String name = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                "<TableName> or <DatabaseName>");
        // (optional)
        List<String> attributes = null;
        if (!isReachEnd() && createType.equalsIgnoreCase("table")) {
            // '('
            getNextStaticToken("(");
            // <AttributeList> ')'
            attributes = parseAttributeList(")");
        }
        checkRemainTokens();
        return new CommandCREATE(name, createType, attributes);
    }

    private Command getCommandDROP() throws ParserException {
        // TABLE or DATABASE
        checkHasNextToken("\"TABLE\" or \"DATABASE\"");
        String dropType = getNextTokenAfterMatching(RX_TABLE_OR_DATABASE,
                                                    "\"TABLE\" or \"DATABASE\"");
        // <TableName> or <DatabaseName>
        checkHasNextToken("<TableName> or <DatabaseName>");
        String name = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                "<TableName> or <DatabaseName>");
        checkRemainTokens();
        return new CommandDROP(name, dropType);
    }

    private Command getCommandALTER() throws ParserException {
        // TABLE
        checkHasNextToken("\"TABLE\"");
        getNextStaticToken("TABLE");
        // <TableName>
        checkHasNextToken("<TableName> or <DatabaseName>");
        String tableName = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                     "<TableName> or <DatabaseName>");
        // <AlterationType>
        checkHasNextToken("<AlterationType>");
        String alterType = getNextTokenAfterMatching(RX_ALTERATION_TYPE,
                                                     "\"ADD\" or \"DROP\"");
        // <AttributeName>
        checkHasNextToken("<AttributeName>");
        String AttributeName = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                         "<AttributeName>");
        // no remaining
        checkRemainTokens();
        return new CommandALTER(tableName, alterType, AttributeName);
    }

    private Command getCommandINSERT() throws ParserException {
        // "INTO"
        checkHasNextToken("\"INTO\"");
        getNextStaticToken("INTO");
        // <TableName>
        checkHasNextToken("<TableName>");
        String tableName = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // "VALUES"
        checkHasNextToken("\"VALUES\"");
        getNextStaticToken("VALUES");
        // '('
        checkHasNextToken("'('");
        getNextStaticToken("(");
        // <ValueList>
        List<String> valueList = parseValueList();

        checkRemainTokens();
        return new CommandINSERT(tableName, valueList);
    }

    private Command getCommandSELECT() throws ParserException {
        List<String> attributeList;
        List<String> conditionTokenList = null;
        // <wildAttributeList> "FROM"
        String wild = tokens.get(currentIndex);
        if (wild.equals("*")) {
            attributeList = new ArrayList<>();
            currentIndex++;
            // "FROM"
            checkHasNextToken("\"FROM\"");
            getNextStaticToken("FROM");
        }
        else {
            attributeList = parseAttributeList("From");
        }
        // <TableName>
        checkHasNextToken("<TableName>");
        String tableName = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // optional condition
        if (!isReachEnd()) {
            // "WHERE"
            checkHasNextToken("\"WHERE\"");
            getNextStaticToken("WHERE");
            // <Condition>
            int startIndex = currentIndex;
            parseConditions();
            // no trailing
            checkRemainTokens();
            conditionTokenList = new ArrayList<>(
                    tokens.subList(startIndex, tokens.size()));
        }
        return new CommandSELECT(tableName, attributeList, conditionTokenList);
    }

    private Command getCommandDELETE() throws ParserException {
        // "FROM"
        checkHasNextToken("\"FROM\"");
        getNextStaticToken("FROM");
        // <TableName>
        checkHasNextToken("<TableName>");
        String tableName = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // "WHERE"
        checkHasNextToken("\"WHERE\"");
        getNextStaticToken("WHERE");
        // <Condition>
        int startIndex = currentIndex;
        parseConditions();
        // no trailing
        checkRemainTokens();
        ArrayList<String> conditionTokenList = new ArrayList<>(
                tokens.subList(startIndex, tokens.size()));
        return new CommandDELETE(tableName, conditionTokenList);
    }

    private Command getCommandUPDATE() throws ParserException {
        // <TableName>
        checkHasNextToken("<TableName>");
        String tableName = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // "SET"
        checkHasNextToken("\"SET\"");
        getNextStaticToken("SET");
        // <NameValueList> "WHERE"
        ArrayList<String> attributeList = new ArrayList<>();
        ArrayList<String> valueList = new ArrayList<>();
        parseNameValueList(attributeList, valueList);
        // <Condition>
        int startIndex = currentIndex;
        parseConditions();
        // no trailing
        checkRemainTokens();
        ArrayList<String> conditionTokenList = new ArrayList<>(
                tokens.subList(startIndex, tokens.size()));

        return new CommandUPDATE(tableName, attributeList, valueList, conditionTokenList);
    }


    // todo
    private Command getCommandJOIN() throws ParserException {
        // <TableName>
        checkHasNextToken("<TableName>");
        String leftTable = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // "AND"
        checkHasNextToken("\"AND\"");
        getNextStaticToken("AND");
        // <TableName>
        checkHasNextToken("<TableName>");
        String rightTable = getNextTokenAfterMatching(RX_PLAIN_TEXT, "<TableName>");
        // "ON"
        checkHasNextToken("\"ON\"");
        getNextStaticToken("ON");
        // "<AttributeName>"
        checkHasNextToken("<AttributeName>");
        String leftAttribute = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                         "<AttributeName>");
        // "AND"
        checkHasNextToken("\"AND\"");
        getNextStaticToken("AND");
        // "<AttributeName>"
        checkHasNextToken("<AttributeName>");
        String rightAttribute = getNextTokenAfterMatching(RX_PLAIN_TEXT,
                                                          "<AttributeName>");

        return new CommandJOIN(leftTable, rightTable, leftAttribute, rightAttribute);
    }

    // Current token should be the first token of <attributeList> when calling this method.
    // endSignal is the token after attributeList. In CREATE command, it's "\\)", in Select it's
    // "FROM"
    private List<String> parseAttributeList(String endToken) throws ParserException {
        ArrayList<String> result = new ArrayList<>();
        while (true) { // keep processing 2 tokens each time.
            // <AttributeName>
            checkHasNextToken("<AttributeName>");
            result.add(getNextTokenAfterMatching(RX_PLAIN_TEXT, "<AttributeName>"));
            // ',' means continue, ')' means end.
            checkHasNextToken("\",\"");
            String next = tokens.get(currentIndex++);
            if (next.equalsIgnoreCase(endToken)) {
                return result; // reach end.
            }
            else if (!next.equals(",")) { // Invalid.
                throw new ParserException.UnexpectedTokenException(next,
                                                                   "\",\" or \")\"");
            }
        }
    }

    // Current token should be the first token of <ValueList> when calling this method.
    private List<String> parseValueList() throws ParserException {
        ArrayList<String> result = new ArrayList<>();
        while (true) { // keep processing 2 tokens each time.
            // <Value>
            checkHasNextToken("<Value>");
            result.add(getNextTokenAsValue());
            // ',' means continue, ')' means end.
            checkHasNextToken("\",\"");
            String next = getNextTokenAfterMatching(RX_COMMA_OR_RIGHT_BRACKET, "\",\"");
            if (next.equals(")")) {
                return result;
            }
        }
    }

    // Current token should be the first token of <ValueList> when calling this method.
    // end token should be "WHERE"
    private void parseNameValueList(List<String> nameList, List<String> valueList) throws ParserException {
        while (true) {
            // <AttributeName>
            checkHasNextToken("<AttributeName>");
            nameList.add(getNextTokenAfterMatching(RX_PLAIN_TEXT, "<AttributeName>"));
            // "="
            checkHasNextToken("\"=\"");
            getNextStaticToken("=");
            // <Value>
            checkHasNextToken("<Value>");
            valueList.add(getNextTokenAsValue());
            // ',' means continue, ')' means end.
            checkHasNextToken("\",\"");
            String next = tokens.get(currentIndex++);
            if (next.equalsIgnoreCase("where")) {
                return; // reach end.
            }
            else if (!next.equals(",")) {// Invalid.
                throw new ParserException.UnexpectedTokenException(next,
                                                                   "\",\" or \")\"");
            }
        }
    }

    private boolean isReachEnd() {
        return currentIndex >= tokens.size();
    }

    // check is there any token can be parsed next.
    private void checkHasNextToken(String expectTokenType) throws ParserException {
        if (isReachEnd()) {
            throw new ParserException.MissingTokenException(expectTokenType);
        }
    }

    // if token match regex return it, otherwise throw exception
    private String getNextTokenAfterMatching(String regex, String expectedToken) throws ParserException {
        String actualToken = tokens.get(currentIndex++);
        if (!actualToken.matches(regex))
            throw new ParserException.UnexpectedTokenException(actualToken,
                                                               expectedToken);
        return actualToken;
    }

    private void getNextStaticToken(String expectedToken) throws ParserException {
        String actualToken = tokens.get(currentIndex++);
        if (!actualToken.equalsIgnoreCase(expectedToken))
            throw new ParserException.UnexpectedTokenException(actualToken,
                                                               '\"' + expectedToken +
                                                               '\"');
    }


    // check value type and get value string.
    private String getNextTokenAsValue() throws ParserException {
        String value = tokens.get(currentIndex++);
        try {
            Type type = Value.matchValueType(value);
            return switch (type) {
                case STRING -> value.substring(1, value.length() - 1); // remove quote;
                case BOOL, NULL -> value.toUpperCase();
                case INT, FLOAT -> value;
            };
        } catch (ValueException e) {
            throw new ParserException(e.getMessage());
        }
    }

    // check whether there is any remaining token after parse.
    private void checkRemainTokens() throws ParserException {
        if (!isReachEnd())
            throw new ParserException.RemainTokensException(tokens.get(currentIndex));
    }

    // check condition syntax.(sublist between WHERE and ;)
    private void parseConditions() throws ParserException {
        // "(" or <AttributeName>
        checkHasNextToken("\"(\" or <AttributeName>");
        if (tokens.get(currentIndex).equals("(")) {
            currentIndex++;
            // <Condition>
            parseConditions();
            // ")"
            checkHasNextToken("\")\"");
            getNextStaticToken(")");
            // "AND" or "OR"
            checkHasNextToken("\"AND\" or \"OR\"");
            getNextTokenAfterMatching(RX_LOGIC_OPT, "\"AND\" or \"OR\"");
            // "("
            checkHasNextToken("\"(\"");
            getNextStaticToken("(");
            // <Condition>
            parseConditions();
            // ")"
            checkHasNextToken("\")\"");
            getNextStaticToken(")");
        }
        else {
            // <AttributeName>
            checkHasNextToken("<AttributeName>");
            getNextTokenAfterMatching(RX_PLAIN_TEXT, "<AttributeName>");
            // <Operator>
            checkHasNextToken("<Operator>");
            getNextTokenAfterMatching(RX_COMPARE_OPT, "<Operator>");
            // <Value>
            checkHasNextToken("<Value>");
            getNextTokenAsValue(); // this method will check value type.
        }
    }
}

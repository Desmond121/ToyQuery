package edu.uob.syntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    private static final String REGEX_OPERATORS = "(?i)(==|!=|>=|<=|>|<)";
    private static final String REGEX_BRACKETS = "[()]";
    private static final String REGEX_LITERAL_STRING = "('[^'\"]*')";
    private static final String REGEX_SYMBOLS = "[;,*]";
    private static final String REGEX_ASSIGNMENT = "(?<=[^=<>!])=(?=[^=])";
    private static final String LITERAL_PLACEHOLDER = "'LITERAL'";

    // This method split a command string into many tokens. Splitting rule:
    // 1. replace token surround by \' with the placeholder, so there will not be any string
    // with whitespace split into tokens.
    // 2. add whitespace to left and right of all operators, symbols(\' not included), brackets.
    // 3. split string into tokens by whitespace.
    // 4. replace all placeholder with original literal string.
    public static List<String> getTokens(String command) {
        // match all literal string and store them.
        Pattern p = Pattern.compile(REGEX_LITERAL_STRING);
        Matcher m = p.matcher(command);
        ArrayDeque<String> literalStrings = new ArrayDeque<>();
        while (m.find()) {
            literalStrings.add(m.group(0));
        }
        // generalize the command string (replace literal string and add whitespace).
        command = command.replaceAll(REGEX_LITERAL_STRING,
                                     " " + LITERAL_PLACEHOLDER + " ")
                         .replaceAll(REGEX_OPERATORS, " $0 ")
                         .replaceAll(REGEX_BRACKETS, " $0 ")
                         .replaceAll(REGEX_SYMBOLS, " $0 ")
                         .replaceAll(REGEX_ASSIGNMENT, " $0 ");
        command = command.trim();
        // get tokens.
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(command.split("\\s+")));
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals(LITERAL_PLACEHOLDER)) {
                tokens.set(i, literalStrings.removeFirst());
            }
        }
        return tokens;
    }
}

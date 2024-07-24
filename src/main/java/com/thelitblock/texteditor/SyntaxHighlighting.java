package com.thelitblock.texteditor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighting {

    private static final String[] KEYWORDS = new String[] {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String FUNCTION_PATTERN = "\\b[a-zA-Z_][a-zA-Z_0-9]*\\s*(?=\\()";
    private static final String NUMBER_PATTERN = "\\b\\d+\\b";
    private static final String ANNOTATION_PATTERN = "@[a-zA-Z_][a-zA-Z_0-9]*";
    private static final String VARIABLE_PATTERN = "\\b[a-zA-Z_][a-zA-Z_0-9]*\\b";
    private static final String PERIOD_PATTERN = "\\.";
    private static final String OPERATOR_PATTERN = "[\\+\\-\\*/%<>=!&|^~]";

    private static final Pattern PATTERN = Pattern.compile(
        "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
        + "|(?<PAREN>" + PAREN_PATTERN + ")"
        + "|(?<BRACE>" + BRACE_PATTERN + ")"
        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
        + "|(?<STRING>" + STRING_PATTERN + ")"
        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        + "|(?<FUNCTION>" + FUNCTION_PATTERN + ")"
        + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
        + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
        + "|(?<VARIABLE>" + VARIABLE_PATTERN + ")"
        + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
        + "|(?<PERIOD>" + PERIOD_PATTERN + ")"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = getContextSensitiveStyleClass(matcher, text);
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private static String getContextSensitiveStyleClass(Matcher matcher, String text) {
        String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("PAREN") != null ? "paren" :
                matcher.group("BRACE") != null ? "brace" :
                matcher.group("BRACKET") != null ? "bracket" :
                matcher.group("SEMICOLON") != null ? "semicolon" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("FUNCTION") != null ? "function" :
                matcher.group("NUMBER") != null ? "number" :
                matcher.group("ANNOTATION") != null ? "annotation" :
                matcher.group("VARIABLE") != null ? "variable" :
                matcher.group("OPERATOR") != null ? "operator" :
                matcher.group("PERIOD") != null ? determinePeriodStyleClass(matcher.start(), text) :
                null; /* never happens */
        assert styleClass != null;
        return styleClass;
    }

    private static String determinePeriodStyleClass(int periodIndex, String text) {
        int startIndex = Math.max(0, periodIndex - 40);
        int endIndex = Math.min(text.length(), periodIndex + 40);
        String snippet = text.substring(startIndex, endIndex);

        if (snippet.matches("(?s).*\\bimport\\s+[^;]*\\b.*")) {
            return "import-period";
        }
        else if (snippet.matches("(?s).*\\b[a-zA-Z_][a-zA-Z_0-9]*\\s*\\(.*")) {
            return "method-call-period";
        }
        else {
            return "period";
        }
    }
}

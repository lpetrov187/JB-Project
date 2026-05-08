package jbl;

import java.util.ArrayList;
import java.util.List;

import static jbl.TokenType.*;

public class Lexer {

    private final String source;
    private int pos = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    // Current character without advancing
    private char current() {
        return pos < source.length() ? source.charAt(pos) : '\0';
    }

    // Consume and return current character
    private char advance() {
        return source.charAt(pos++);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < source.length()) {
            char c = current();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                tokens.add(new Token(NEWLINE, "\n", 0, line));
                line++;
                advance();
            } else if (c == '/' && pos + 1 < source.length() && source.charAt(pos + 1) == '/') {
                while (pos < source.length() && source.charAt(pos) != '\n') pos++;
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdent());
            } else {
                tokens.add(readSymbol());
            }
        }
        tokens.add(new Token(EOF, "", 0, line));
        return tokens;
    }

    private Token readNumber() {
        int start = pos;
        int val = 0;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            val = val * 10 + (source.charAt(pos) - '0');
            pos++;
        }
        return new Token(NUMBER, source.substring(start, pos), val, line);
    }

    private Token readIdent() {
        int start = pos;
        while (pos < source.length() && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String word = source.substring(start, pos);
        TokenType type = switch (word) {
            case "fun"    -> FUN;
            case "if"     -> IF;
            case "then"   -> THEN;
            case "else"   -> ELSE;
            case "while"  -> WHILE;
            case "do"     -> DO;
            case "return" -> RETURN;
            case "true"   -> TRUE;
            default       -> IDENT;
        };
        return new Token(type, word, 0, line);
    }

    // Two-character lookahead for ==, !=, <=, >=
    private Token readSymbol() {
        char c = advance();
        return switch (c) {
            case '+' -> new Token(PLUS,   "+", 0, line);
            case '-' -> new Token(MINUS,  "-", 0, line);
            case '*' -> new Token(STAR,   "*", 0, line);
            case '/' -> new Token(SLASH,  "/", 0, line);
            case '(' -> new Token(LPAREN, "(", 0, line);
            case ')' -> new Token(RPAREN, ")", 0, line);
            case '{' -> new Token(LBRACE, "{", 0, line);
            case '}' -> new Token(RBRACE, "}", 0, line);
            case ',' -> new Token(COMMA,  ",", 0, line);
            case '=' -> {
                if (current() == '=') { advance(); yield new Token(EQEQ, "==", 0, line); }
                yield new Token(EQ, "=", 0, line);
            }
            case '!' -> {
                if (current() == '=') { advance(); yield new Token(NEQ, "!=", 0, line); }
                throw new RuntimeException("Unexpected character '!' at line " + line);
            }
            case '<' -> {
                if (current() == '=') { advance(); yield new Token(LTE, "<=", 0, line); }
                yield new Token(LT, "<", 0, line);
            }
            case '>' -> {
                if (current() == '=') { advance(); yield new Token(GTE, ">=", 0, line); }
                yield new Token(GT, ">", 0, line);
            }
            default -> throw new RuntimeException("Unexpected character '" + c + "' at line " + line);
        };
    }
}

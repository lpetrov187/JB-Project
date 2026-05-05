package jbl;

enum TokenType {
    NUMBER, IDENT, TRUE,
    FUN, IF, THEN, ELSE, WHILE, DO, RETURN,
    PLUS, MINUS, STAR, SLASH,
    EQ,     // =  (assignment)
    EQEQ,   // ==
    NEQ,    // !=
    LT, LTE, GT, GTE,
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA,
    NEWLINE, EOF
}

public record Token(TokenType type, String lexeme, int value, int line) {}

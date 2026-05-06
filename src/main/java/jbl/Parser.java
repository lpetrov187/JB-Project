package jbl;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static jbl.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Token peek() {
        return tokens.get(pos);
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token expect(TokenType type) {
        Token t = peek();
        if (t.type() != type) {
            throw new RuntimeException(
                "Expected " + type + " but got " + t.type() +
                " (\"" + t.lexeme() + "\") at line " + t.line());
        }
        return advance();
    }

    private void skipNewlines() {
        while (peek().type() == NEWLINE) advance();
    }

    // -------------------------------------------------------------------------
    // Program
    // -------------------------------------------------------------------------

    public Program parseProgram() {
        List<Stmt> stmts = new ArrayList<>();
        skipNewlines();
        while (peek().type() != EOF) {
            stmts.addAll(parseStmts(Set.of(NEWLINE, EOF)));
            while (peek().type() == NEWLINE) advance();
        }
        return new Program(stmts);
    }

    // -------------------------------------------------------------------------
    // Statements
    // -------------------------------------------------------------------------

    /**
     * Parses a comma-separated list of statements, stopping when it sees a
     * token in {@code stopAt} or a token that cannot start a statement.
     *
     * The {@code stopAt} set is propagated into while bodies so they inherit
     * the parent context — this is what makes the while body "greedy".
     */
    private List<Stmt> parseStmts(Set<TokenType> stopAt) {
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(parseStmt(stopAt));
        while (peek().type() == COMMA) {
            advance();       // consume ','
            skipNewlines();
            stmts.add(parseStmt(stopAt));
        }
        return stmts;
    }

    private Stmt parseStmt(Set<TokenType> stopAt) {
        switch (peek().type()) {
            case FUN:    return parseFunDef();
            case IF:     return parseIfStmt(stopAt);
            case WHILE:  return parseWhileStmt(stopAt);
            case RETURN: return parseReturnStmt();
            case IDENT:  return parseIdentStmt();
            default: throw new RuntimeException(
                "Unexpected token " + peek().type() +
                " (\"" + peek().lexeme() + "\") at line " + peek().line());
        }
    }

    /** fun IDENT '(' params ')' '{' stmts '}' */
    private FunDef parseFunDef() {
        expect(FUN);
        String name = expect(IDENT).lexeme();
        expect(LPAREN);
        List<String> params = new ArrayList<>();
        if (peek().type() != RPAREN) {
            params.add(expect(IDENT).lexeme());
            while (peek().type() == COMMA) {
                advance();
                params.add(expect(IDENT).lexeme());
            }
        }
        expect(RPAREN);
        expect(LBRACE);
        skipNewlines();
        List<Stmt> body = parseStmts(Set.of(RBRACE));
        skipNewlines();
        expect(RBRACE);
        return new FunDef(name, params, body);
    }

    /**
     * if expr then stmt (else stmt)?
     *
     * Each branch is a SINGLE stmt (not stmts). The stopAt is forwarded so
     * that nested whiles still know their own stop boundary.
     */
    private IfStmt parseIfStmt(Set<TokenType> stopAt) {
        expect(IF);
        Expr condition = parseExpr();
        expect(THEN);
        Stmt thenBranch = parseStmt(stopAt);
        Stmt elseBranch = null;
        if (peek().type() == ELSE) {
            advance();
            elseBranch = parseStmt(stopAt);
        }
        return new IfStmt(condition, thenBranch, elseBranch);
    }

    /**
     * while expr do stmts
     *
     * The body is parsed with the SAME stopAt as the surrounding context —
     * this is the key rule that makes commas after a while body belong to
     * the while, not to an outer statement list.
     */
    private WhileStmt parseWhileStmt(Set<TokenType> stopAt) {
        expect(WHILE);
        Expr condition = parseExpr();
        expect(DO);
        List<Stmt> body = parseStmts(stopAt);   // inherit parent stopAt
        return new WhileStmt(condition, body);
    }

    /** return expr */
    private ReturnStmt parseReturnStmt() {
        expect(RETURN);
        return new ReturnStmt(parseExpr());
    }

    /**
     * IDENT '=' expr       -> AssignStmt
     * IDENT '(' args ')'   -> ExprStmt(CallExpr)
     */
    private Stmt parseIdentStmt() {
        String name = expect(IDENT).lexeme();
        if (peek().type() == EQ) {
            advance();
            return new AssignStmt(name, parseExpr());
        }
        if (peek().type() == LPAREN) {
            advance();
            List<Expr> args = parseArgs();
            expect(RPAREN);
            return new ExprStmt(new CallExpr(name, args));
        }
        throw new RuntimeException(
            "Expected '=' or '(' after identifier '" + name + "' at line " + peek().line());
    }

    // -------------------------------------------------------------------------
    // Expressions
    // -------------------------------------------------------------------------

    private Expr parseExpr() {
        return parseComparison();
    }

    /** arith (op arith)?  -- at most one comparison per expression */
    private Expr parseComparison() {
        Expr left = parseArith();
        TokenType t = peek().type();
        if (t == EQEQ || t == NEQ || t == LT || t == LTE || t == GT || t == GTE) {
            String op = advance().lexeme();
            return new BinaryExpr(left, op, parseArith());
        }
        return left;
    }

    /** term (('+' | '-') term)* */
    private Expr parseArith() {
        Expr left = parseTerm();
        while (peek().type() == PLUS || peek().type() == MINUS) {
            String op = advance().lexeme();
            left = new BinaryExpr(left, op, parseTerm());
        }
        return left;
    }

    /** unary (('*' | '/') unary)* */
    private Expr parseTerm() {
        Expr left = parseUnary();
        while (peek().type() == STAR || peek().type() == SLASH) {
            String op = advance().lexeme();
            left = new BinaryExpr(left, op, parseUnary());
        }
        return left;
    }

    /** '-' unary | primary */
    private Expr parseUnary() {
        if (peek().type() == MINUS) {
            advance();
            return new UnaryExpr("-", parseUnary());
        }
        return parsePrimary();
    }

    /** NUMBER | 'true' | '(' expr ')' | IDENT ('(' args ')' | epsilon) */
    private Expr parsePrimary() {
        Token t = peek();
        switch (t.type()) {
            case NUMBER -> { advance(); return new NumberLit(t.value()); }
            case TRUE   -> { advance(); return new BoolLit(true); }
            case LPAREN -> {
                advance();
                Expr e = parseExpr();
                expect(RPAREN);
                return e;
            }
            case IDENT -> {
                advance();
                if (peek().type() == LPAREN) {
                    advance();
                    List<Expr> args = parseArgs();
                    expect(RPAREN);
                    return new CallExpr(t.lexeme(), args);
                }
                return new VarExpr(t.lexeme());
            }
            default -> throw new RuntimeException(
                "Unexpected token in expression: " + t.type() +
                " (\"" + t.lexeme() + "\") at line " + t.line());
        }
    }

    /** expr (',' expr)*  -- only inside argument lists, between '(' and ')' */
    private List<Expr> parseArgs() {
        List<Expr> args = new ArrayList<>();
        if (peek().type() != RPAREN) {
            args.add(parseExpr());
            while (peek().type() == COMMA) {
                advance();
                args.add(parseExpr());
            }
        }
        return args;
    }
}

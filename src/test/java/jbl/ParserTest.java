package jbl;

import jbl.ast.nodes.Program;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private final AstPrinter printer = new AstPrinter();

    private String parse(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        Program program = new Parser(tokens).parseProgram();
        return printer.print(program);
    }

    // -------------------------------------------------------------------------
    // Basic statements
    // -------------------------------------------------------------------------

    @Test
    void simpleAssignment() {
        assertEquals("(assign x (num 2))", parse("x = 2"));
    }

    @Test
    void multipleLines() {
        assertEquals("(assign x (num 1))\n(assign y (num 2))", parse("x = 1\ny = 2"));
    }

    @Test
    void commaSeperatedStatements() {
        assertEquals("(assign x (num 0))\n(assign y (num 1))", parse("x = 0, y = 1"));
    }

    // -------------------------------------------------------------------------
    // Expressions & precedence
    // -------------------------------------------------------------------------

    @Test
    void arithmeticPrecedence() {
        // * must bind tighter than +
        assertEquals("(assign x (+ (num 1) (* (num 2) (num 3))))", parse("x = 1 + 2 * 3"));
    }

    @Test
    void parenthesesOverridePrecedence() {
        assertEquals("(assign x (* (+ (num 1) (num 2)) (num 3)))", parse("x = (1 + 2) * 3"));
    }

    @Test
    void unaryMinus() {
        assertEquals("(assign x (neg (num 5)))", parse("x = -5"));
    }

    @Test
    void comparison() {
        assertEquals("(assign flag (== (var x) (num 0)))", parse("flag = x == 0"));
    }

    @Test
    void functionCallExpression() {
        assertEquals("(assign x (call double (num 5)))", parse("x = double(5)"));
    }

    @Test
    void functionCallMultipleArgs() {
        assertEquals("(assign x (call add (num 1) (num 2)))", parse("x = add(1, 2)"));
    }

    // -------------------------------------------------------------------------
    // If / else
    // -------------------------------------------------------------------------

    @Test
    void ifWithElse() {
        assertEquals(
            "(if (> (var x) (num 0)) (assign y (num 1)) (assign y (num 0)))",
            parse("if x > 0 then y = 1 else y = 0")
        );
    }

    @Test
    void ifWithoutElse() {
        assertEquals(
            "(if (< (var x) (num 10)) (assign x (+ (var x) (num 1))))",
            parse("if x < 10 then x = x + 1")
        );
    }

    // -------------------------------------------------------------------------
    // While — greedy body (the critical stopAt cases)
    // -------------------------------------------------------------------------

    @Test
    void whileGreedyBody() {
        // CRITICAL: both stmts must be INSIDE the while body, not after it
        assertEquals(
            "(while (< (var x) (num 3)) (assign y (num 0)) (assign x (+ (var x) (num 1))))",
            parse("while x < 3 do y = 0, x = x + 1")
        );
    }

    @Test
    void whileIfElseGreedyBody() {
        // The comma after the if/else belongs to the while body (from the plan)
        String src = "while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1";
        assertEquals(
            "(while (< (var x) (num 3)) (if (== (var x) (num 1)) (assign y (num 10)) (assign y (+ (var y) (num 1)))) (assign x (+ (var x) (num 1))))",
            parse(src)
        );
    }

    // -------------------------------------------------------------------------
    // Functions
    // -------------------------------------------------------------------------

    @Test
    void functionDefinition() {
        assertEquals(
            "(fun square (n) (return (* (var n) (var n))))",
            parse("fun square(n) { return n * n }")
        );
    }

    @Test
    void recursiveFunction() {
        String src = "fun fact(n) { if n == 0 then return 1 else return fact(n - 1) * n }";
        assertEquals(
            "(fun fact (n) (if (== (var n) (num 0)) (return (num 1)) (return (* (call fact (- (var n) (num 1))) (var n)))))",
            parse(src)
        );
    }

    @Test
    void iterativeFact_returnPropagatesThroughWhile() {
        // n = n - 1 must be INSIDE the while body (stopAt = RBRACE propagates through while)
        String src = "fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }";
        assertEquals(
            "(fun fact_iter (n) (assign r (num 1)) (while true (if (== (var n) (num 0)) (return (var r)) (assign r (* (var r) (var n)))) (assign n (- (var n) (num 1)))))",
            parse(src)
        );
    }
}

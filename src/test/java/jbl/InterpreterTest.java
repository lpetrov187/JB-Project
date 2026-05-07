package jbl;

import jbl.ast.nodes.Program;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest {

    private Interpreter interp;

    private void run(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        Program program = new Parser(tokens).parseProgram();
        interp = new Interpreter();
        interp.execute(program);
    }

    private Value get(String name) {
        return interp.getValue(name);
    }

    // -------------------------------------------------------------------------
    // Arithmetic
    // -------------------------------------------------------------------------

    @Test
    void intLiteral() {
        run("x = 42");
        assertEquals(new IntVal(42), get("x"));
    }

    @Test
    void addition() {
        run("x = 2 + 3");
        assertEquals(new IntVal(5), get("x"));
    }

    @Test
    void precedence() {
        run("x = 1 + 2 * 3");
        assertEquals(new IntVal(7), get("x"));
    }

    @Test
    void unaryMinus() {
        run("x = -5");
        assertEquals(new IntVal(-5), get("x"));
    }

    @Test
    void multipleAssignments() {
        run("x = 3, y = x * x");
        assertEquals(new IntVal(9), get("y"));
    }

    // -------------------------------------------------------------------------
    // Comparisons
    // -------------------------------------------------------------------------

    @Test
    void comparisonTrue() {
        run("b = 3 > 1");
        assertEquals(new BoolVal(true), get("b"));
    }

    @Test
    void comparisonFalse() {
        run("b = 1 == 2");
        assertEquals(new BoolVal(false), get("b"));
    }

    @Test
    void equalityOnBooleans() {
        run("b = true == true");
        assertEquals(new BoolVal(true), get("b"));
    }

    // -------------------------------------------------------------------------
    // If / else
    // -------------------------------------------------------------------------

    @Test
    void ifTaken() {
        run("x = 5, if x > 0 then y = 1 else y = 0");
        assertEquals(new IntVal(1), get("y"));
    }

    @Test
    void elseTaken() {
        run("x = -1, if x > 0 then y = 1 else y = 0");
        assertEquals(new IntVal(0), get("y"));
    }

    @Test
    void ifWithoutElseSkipped() {
        run("y = 99, x = 0, if x > 0 then y = 1");
        assertEquals(new IntVal(99), get("y"));
    }

    // -------------------------------------------------------------------------
    // While
    // -------------------------------------------------------------------------

    @Test
    void whileLoop() {
        run("i = 0, s = 0, while i < 5 do s = s + i, i = i + 1");
        assertEquals(new IntVal(10), get("s")); // 0+1+2+3+4
    }

    @Test
    void whileSkippedWhenFalse() {
        run("x = 0, while x > 0 do x = x - 1");
        assertEquals(new IntVal(0), get("x"));
    }

    // -------------------------------------------------------------------------
    // Functions
    // -------------------------------------------------------------------------

    @Test
    void simpleFunction() {
        run("fun double(n) { return n * 2 }, r = double(7)");
        assertEquals(new IntVal(14), get("r"));
    }

    @Test
    void multiArgFunction() {
        run("fun add(a, b) { return a + b }, r = add(3, 4)");
        assertEquals(new IntVal(7), get("r"));
    }

    @Test
    void recursiveFactorial() {
        run("fun fact(n) { if n == 0 then return 1 else return fact(n - 1) * n }, r = fact(5)");
        assertEquals(new IntVal(120), get("r"));
    }

    @Test
    void iterativeFactorial() {
        run("fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }, res = fact_iter(5)");
        assertEquals(new IntVal(120), get("res"));
    }

    @Test
    void recursiveFibonacci() {
        run("fun fib(n) { if n < 2 then return n else return fib(n - 1) + fib(n - 2) }, r = fib(10)");
        assertEquals(new IntVal(55), get("r"));
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    void undefinedVariableThrows() {
        assertThrows(RuntimeException.class, () -> run("x = y"));
    }

    @Test
    void callNonFunctionThrows() {
        assertThrows(RuntimeException.class, () -> run("x = 5, r = x(1)"));
    }

    @Test
    void argCountMismatchThrows() {
        assertThrows(RuntimeException.class, () -> run("fun f(a, b) { return a + b }, r = f(1)"));
    }
}

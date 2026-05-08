package jbl;

import jbl.ast.nodes.CallExpr;
import jbl.ast.nodes.ReturnStmt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class DebugInterpreter extends Interpreter {

    private final String tracedFunc;
    private final Deque<String> callStack = new ArrayDeque<>();
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public DebugInterpreter(String tracedFunc) {
        this.tracedFunc = tracedFunc;
    }

    @Override
    public Value visitCallExpr(CallExpr node) {
        callStack.push(node.name());
        try {
            return super.visitCallExpr(node);
        } finally {
            callStack.pop();
        }
    }

    @Override
    public Value visitReturnStmt(ReturnStmt node) {
        if (!tracedFunc.equals(currentFunction)) {
            throw new ReturnSignal(node.value().accept(this));
        }

        System.out.println();
        System.out.println("[Stage 1]  emulated breakpoint -- '" + tracedFunc + "' return statement reached  (depth " + callStack.size() + ")");
        System.out.println("           IntelliJ IDEA: value is on the operand stack; There is no retrieved info about it; JDWP doesn't provide stack data.");
        System.out.println("           JBL:           expression not yet evaluated -- return value unavailable either way.");
        printLocals();
        pause();

        Value result = node.value().accept(this);

        System.out.println();
        System.out.println("[Stage 2]  JBL: '" + tracedFunc + "' return value captured  (depth " + callStack.size() + ")");
        System.out.println("           return value -> " + valueRepr(result));
        pause();

        throw new ReturnSignal(result);
    }

    private void pause() {
        try {
            while (true) {
                System.out.print("(debug) > ");
                System.out.flush();
                String line = input.readLine();
                if (line == null) return;
                switch (line.trim()) {
                    case "c", "continue" -> { return; }
                    case "locals" -> printLocals();
                    case "stack"  -> printStack();
                    default -> System.out.println("  Commands: c / continue, locals, stack");
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Debug input error", e);
        }
    }

    private void printLocals() {
        currentEnv.bindings().entrySet().stream()
            .filter(e -> !(e.getValue() instanceof FunVal))
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println("  " + e.getKey() + " = " + valueRepr(e.getValue())));
    }

    private void printStack() {
        int i = 0;
        for (String frame : callStack) {
            System.out.println("  #" + i + "  " + frame);
            i++;
        }
    }

    private static String valueRepr(Value v) {
        if (v instanceof IntVal  iv) return String.valueOf(iv.n());
        if (v instanceof BoolVal bv) return String.valueOf(bv.b());
        return "<fun>";
    }
}

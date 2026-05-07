package jbl;

import jbl.ast.VisitorAdaptor;
import jbl.ast.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interpreter extends VisitorAdaptor<Value> {

    private final Environment globalEnv = new Environment(null);
    private Environment currentEnv = globalEnv;

    public void execute(Program program) {
        program.accept(this);
    }

    public Value getValue(String name) {
        return globalEnv.get(name);
    }

    public void printGlobals() {
        globalEnv.bindings().entrySet().stream()
            .filter(e -> !(e.getValue() instanceof FunVal))
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                Value v = e.getValue();
                String repr = v instanceof IntVal i  ? String.valueOf(i.n())
                            : v instanceof BoolVal b ? String.valueOf(b.b())
                            : v.toString();
                System.out.println(e.getKey() + ": " + repr);
            });
    }

    // -------------------------------------------------------------------------
    // Statements — return null, act via side effects
    // -------------------------------------------------------------------------

    @Override
    public Value visitProgram(Program node) {
        for (var stmt : node.stmts()) stmt.accept(this);
        return null;
    }

    @Override
    public Value visitFunDef(FunDef node) {
        currentEnv.set(node.name(), new FunVal(node.params(), node.body()));
        return null;
    }

    @Override
    public Value visitAssignStmt(AssignStmt node) {
        currentEnv.set(node.name(), node.value().accept(this));
        return null;
    }

    @Override
    public Value visitIfStmt(IfStmt node) {
        if (isTruthy(node.condition().accept(this))) {
            node.thenBranch().accept(this);
        } else if (node.elseBranch() != null) {
            node.elseBranch().accept(this);
        }
        return null;
    }

    @Override
    public Value visitWhileStmt(WhileStmt node) {
        while (isTruthy(node.condition().accept(this))) {
            for (var stmt : node.body()) stmt.accept(this);
        }
        return null;
    }

    @Override
    public Value visitReturnStmt(ReturnStmt node) {
        throw new ReturnSignal(node.value().accept(this));
    }

    @Override
    public Value visitExprStmt(ExprStmt node) {
        node.expr().accept(this);
        return null;
    }

    // -------------------------------------------------------------------------
    // Expressions — evaluate and return a Value
    // -------------------------------------------------------------------------

    @Override
    public Value visitBinaryExpr(BinaryExpr node) {
        Value left  = node.left().accept(this);
        Value right = node.right().accept(this);
        return switch (node.op()) {
            case "+"  -> new IntVal(asInt(left) + asInt(right));
            case "-"  -> new IntVal(asInt(left) - asInt(right));
            case "*"  -> new IntVal(asInt(left) * asInt(right));
            case "/"  -> new IntVal(asInt(left) / asInt(right));
            case "==" -> new BoolVal(left.equals(right));
            case "!=" -> new BoolVal(!left.equals(right));
            case "<"  -> new BoolVal(asInt(left) <  asInt(right));
            case "<=" -> new BoolVal(asInt(left) <= asInt(right));
            case ">"  -> new BoolVal(asInt(left) >  asInt(right));
            case ">=" -> new BoolVal(asInt(left) >= asInt(right));
            default   -> throw new RuntimeException("Unknown operator: " + node.op());
        };
    }

    @Override
    public Value visitUnaryExpr(UnaryExpr node) {
        return new IntVal(-asInt(node.operand().accept(this)));
    }

    @Override
    public Value visitNumberLit(NumberLit node) { return new IntVal(node.value()); }

    @Override
    public Value visitBoolLit(BoolLit node)     { return new BoolVal(node.value()); }

    @Override
    public Value visitVarExpr(VarExpr node)     { return currentEnv.get(node.name()); }

    @Override
    public Value visitCallExpr(CallExpr node) {
        Value callee = currentEnv.get(node.name());
        if (!(callee instanceof FunVal fun))
            throw new RuntimeException(node.name() + " is not a function");

        List<Value> args = new ArrayList<>();
        for (var arg : node.args()) args.add(arg.accept(this));

        if (fun.params().size() != args.size())
            throw new RuntimeException("Argument count mismatch for " + node.name());

        Environment callEnv = new Environment(globalEnv);
        for (int i = 0; i < fun.params().size(); i++)
            callEnv.set(fun.params().get(i), args.get(i));

        Environment saved = currentEnv;
        currentEnv = callEnv;
        try {
            for (var stmt : fun.body()) stmt.accept(this);
            return new IntVal(0); // implicit return value if no return stmt
        } catch (ReturnSignal r) {
            return r.value;
        } finally {
            currentEnv = saved;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isTruthy(Value v) {
        if (v instanceof BoolVal b) return b.b();
        if (v instanceof IntVal i)  return i.n() != 0;
        throw new RuntimeException("Not a condition: " + v);
    }

    private int asInt(Value v) {
        if (v instanceof IntVal i) return i.n();
        throw new RuntimeException("Expected number, got: " + v);
    }
}

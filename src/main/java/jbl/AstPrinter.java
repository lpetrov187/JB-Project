package jbl;

import jbl.ast.Node;
import jbl.ast.VisitorAdaptor;
import jbl.ast.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Produces a compact S-expression string from any AST node.
 * Used by ParserTest for assertions and by Main for interactive inspection.
 *
 * Examples:
 *   x = 2             -> (assign x (num 2))
 *   x = 1 + 2 * 3     -> (assign x (+ (num 1) (* (num 2) (num 3))))
 *   while x < 3 do ... -> (while (< (var x) (num 3)) ...)
 */
public class AstPrinter extends VisitorAdaptor<String> {

    public String print(Node node) {
        return node.accept(this);
    }

    @Override
    public String visitProgram(Program node) {
        return node.stmts().stream()
                .map(s -> s.accept(this))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String visitFunDef(FunDef node) {
        String params = String.join(" ", node.params());
        String body   = node.body().stream().map(s -> s.accept(this)).collect(Collectors.joining(" "));
        return "(fun " + node.name() + " (" + params + ") " + body + ")";
    }

    @Override
    public String visitIfStmt(IfStmt node) {
        String cond = node.condition().accept(this);
        String then = node.thenBranch().accept(this);
        if (node.elseBranch() != null) {
            return "(if " + cond + " " + then + " " + node.elseBranch().accept(this) + ")";
        }
        return "(if " + cond + " " + then + ")";
    }

    @Override
    public String visitWhileStmt(WhileStmt node) {
        String cond = node.condition().accept(this);
        String body = node.body().stream().map(s -> s.accept(this)).collect(Collectors.joining(" "));
        return "(while " + cond + " " + body + ")";
    }

    @Override
    public String visitReturnStmt(ReturnStmt node) {
        return "(return " + node.value().accept(this) + ")";
    }

    @Override
    public String visitAssignStmt(AssignStmt node) {
        return "(assign " + node.name() + " " + node.value().accept(this) + ")";
    }

    @Override
    public String visitExprStmt(ExprStmt node) {
        return "(expr " + node.expr().accept(this) + ")";
    }

    @Override
    public String visitBinaryExpr(BinaryExpr node) {
        return "(" + node.op() + " " + node.left().accept(this) + " " + node.right().accept(this) + ")";
    }

    @Override
    public String visitUnaryExpr(UnaryExpr node) {
        return "(neg " + node.operand().accept(this) + ")";
    }

    @Override
    public String visitNumberLit(NumberLit node) {
        return "(num " + node.value() + ")";
    }

    @Override
    public String visitBoolLit(BoolLit node) {
        return node.value() ? "true" : "false";
    }

    @Override
    public String visitVarExpr(VarExpr node) {
        return "(var " + node.name() + ")";
    }

    @Override
    public String visitCallExpr(CallExpr node) {
        if (node.args().isEmpty()) {
            return "(call " + node.name() + ")";
        }
        String args = node.args().stream().map(a -> a.accept(this)).collect(Collectors.joining(" "));
        return "(call " + node.name() + " " + args + ")";
    }

    // -------------------------------------------------------------------------
    // Visual tree printer
    // -------------------------------------------------------------------------

    private record Tree(String label, List<Node> children) {}

    public String printTree(Node node) {
        StringBuilder sb = new StringBuilder();
        renderTree(toTree(node), sb, "", "");
        return sb.toString().stripTrailing();
    }

    private void renderTree(Tree tree, StringBuilder sb, String prefix, String childPrefix) {
        sb.append(prefix).append(tree.label()).append("\n");
        List<Node> children = tree.children();
        for (int i = 0; i < children.size(); i++) {
            boolean last = i == children.size() - 1;
            renderTree(
                toTree(children.get(i)),
                sb,
                childPrefix + (last ? "└── " : "├── "),
                childPrefix + (last ? "    " : "│   ")
            );
        }
    }

    private Tree toTree(Node node) {
        if (node instanceof Program n)
            return new Tree("Program", List.copyOf(n.stmts()));
        if (node instanceof FunDef n) {
            String sig = "fun " + n.name() + "(" + String.join(", ", n.params()) + ")";
            return new Tree(sig, List.copyOf(n.body()));
        }
        if (node instanceof IfStmt n) {
            List<Node> children = new ArrayList<>();
            children.add(n.condition());
            children.add(n.thenBranch());
            if (n.elseBranch() != null) children.add(n.elseBranch());
            return new Tree("if", children);
        }
        if (node instanceof WhileStmt n) {
            List<Node> children = new ArrayList<>();
            children.add(n.condition());
            children.addAll(n.body());
            return new Tree("while", children);
        }
        if (node instanceof ReturnStmt n)
            return new Tree("return", List.of(n.value()));
        if (node instanceof AssignStmt n)
            return new Tree("assign " + n.name(), List.of(n.value()));
        if (node instanceof ExprStmt n)
            return new Tree("expr", List.of(n.expr()));
        if (node instanceof BinaryExpr n)
            return new Tree(n.op(), List.of(n.left(), n.right()));
        if (node instanceof UnaryExpr n)
            return new Tree("neg", List.of(n.operand()));
        if (node instanceof NumberLit n)
            return new Tree("num " + n.value(), List.of());
        if (node instanceof BoolLit n)
            return new Tree(String.valueOf(n.value()), List.of());
        if (node instanceof VarExpr n)
            return new Tree("var " + n.name(), List.of());
        if (node instanceof CallExpr n)
            return new Tree("call " + n.name(), List.copyOf(n.args()));
        return new Tree(node.toString(), List.of());
    }
}

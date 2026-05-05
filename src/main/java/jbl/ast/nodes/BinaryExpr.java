package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

public class BinaryExpr extends Expr {
    private final Expr left;
    private final String op;
    private final Expr right;

    public BinaryExpr(Expr left, String op, Expr right) {
        this.left  = left;
        this.op    = op;
        this.right = right;
    }

    public Expr left()   { return left; }
    public String op()   { return op; }
    public Expr right()  { return right; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitBinaryExpr(this); }
}

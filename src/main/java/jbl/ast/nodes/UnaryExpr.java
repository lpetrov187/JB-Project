package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

public class UnaryExpr extends Expr {
    private final String op;
    private final Expr operand;

    public UnaryExpr(String op, Expr operand) {
        this.op      = op;
        this.operand = operand;
    }

    public String op()    { return op; }
    public Expr operand() { return operand; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitUnaryExpr(this); }
}

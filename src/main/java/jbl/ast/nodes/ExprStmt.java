package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

public class ExprStmt extends Stmt {
    private final Expr expr;

    public ExprStmt(Expr expr) { this.expr = expr; }

    public Expr expr() { return expr; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitExprStmt(this); }
}

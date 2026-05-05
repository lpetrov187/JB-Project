package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

public class ReturnStmt extends Stmt {
    private final Expr value;

    public ReturnStmt(Expr value) { this.value = value; }

    public Expr value() { return value; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitReturnStmt(this); }
}

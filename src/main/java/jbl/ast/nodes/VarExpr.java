package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

public class VarExpr extends Expr {
    private final String name;

    public VarExpr(String name) { this.name = name; }

    public String name() { return name; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitVarExpr(this); }
}

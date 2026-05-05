package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

public class BoolLit extends Expr {
    private final boolean value;

    public BoolLit(boolean value) { this.value = value; }

    public boolean value() { return value; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitBoolLit(this); }
}

package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

public class NumberLit extends Expr {
    private final int value;

    public NumberLit(int value) { this.value = value; }

    public int value() { return value; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitNumberLit(this); }
}

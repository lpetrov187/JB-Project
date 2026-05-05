package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Visitor;

import java.util.List;

public class CallExpr extends Expr {
    private final String name;
    private final List<Expr> args;

    public CallExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    public String name()      { return name; }
    public List<Expr> args()  { return args; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitCallExpr(this); }
}

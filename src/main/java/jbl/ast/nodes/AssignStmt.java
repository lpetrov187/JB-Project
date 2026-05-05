package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

public class AssignStmt extends Stmt {
    private final String name;
    private final Expr value;

    public AssignStmt(String name, Expr value) {
        this.name  = name;
        this.value = value;
    }

    public String name() { return name; }
    public Expr value()  { return value; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitAssignStmt(this); }
}

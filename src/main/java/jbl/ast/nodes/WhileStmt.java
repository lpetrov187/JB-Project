package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

import java.util.List;

public class WhileStmt extends Stmt {
    private final Expr condition;
    private final List<Stmt> body;

    public WhileStmt(Expr condition, List<Stmt> body) {
        this.condition = condition;
        this.body      = body;
    }

    public Expr condition()  { return condition; }
    public List<Stmt> body() { return body; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitWhileStmt(this); }
}

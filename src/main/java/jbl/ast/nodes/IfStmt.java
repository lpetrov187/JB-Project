package jbl.ast.nodes;

import jbl.ast.Expr;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

public class IfStmt extends Stmt {
    private final Expr condition;
    private final Stmt thenBranch;
    private final Stmt elseBranch; // nullable

    public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        this.condition  = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Expr condition()  { return condition; }
    public Stmt thenBranch() { return thenBranch; }
    public Stmt elseBranch() { return elseBranch; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitIfStmt(this); }
}

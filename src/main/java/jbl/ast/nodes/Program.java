package jbl.ast.nodes;

import jbl.ast.Node;
import jbl.ast.Stmt;
import jbl.ast.Visitor;

import java.util.List;

public class Program extends Node {
    private final List<Stmt> stmts;

    public Program(List<Stmt> stmts) { this.stmts = stmts; }

    public List<Stmt> stmts() { return stmts; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitProgram(this); }
}

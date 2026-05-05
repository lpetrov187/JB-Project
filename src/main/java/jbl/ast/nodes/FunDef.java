package jbl.ast.nodes;

import jbl.ast.Stmt;
import jbl.ast.Visitor;

import java.util.List;

public class FunDef extends Stmt {
    private final String name;
    private final List<String> params;
    private final List<Stmt> body;

    public FunDef(String name, List<String> params, List<Stmt> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public String name()          { return name; }
    public List<String> params()  { return params; }
    public List<Stmt> body()      { return body; }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visitFunDef(this); }
}

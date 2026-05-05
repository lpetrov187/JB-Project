package jbl.ast;

import jbl.ast.nodes.*;

/**
 * Default no-op implementation of Visitor.
 * Subclasses override only the visit methods they care about.
 * Mirrors the VisitorAdaptor pattern from MicroJava.
 */
public abstract class VisitorAdaptor<T> implements Visitor<T> {

    protected T defaultResult() { return null; }

    @Override public T visitProgram(Program node)         { return defaultResult(); }
    @Override public T visitFunDef(FunDef node)           { return defaultResult(); }
    @Override public T visitIfStmt(IfStmt node)           { return defaultResult(); }
    @Override public T visitWhileStmt(WhileStmt node)     { return defaultResult(); }
    @Override public T visitReturnStmt(ReturnStmt node)   { return defaultResult(); }
    @Override public T visitAssignStmt(AssignStmt node)   { return defaultResult(); }
    @Override public T visitExprStmt(ExprStmt node)       { return defaultResult(); }
    @Override public T visitBinaryExpr(BinaryExpr node)   { return defaultResult(); }
    @Override public T visitUnaryExpr(UnaryExpr node)     { return defaultResult(); }
    @Override public T visitNumberLit(NumberLit node)     { return defaultResult(); }
    @Override public T visitBoolLit(BoolLit node)         { return defaultResult(); }
    @Override public T visitVarExpr(VarExpr node)         { return defaultResult(); }
    @Override public T visitCallExpr(CallExpr node)       { return defaultResult(); }
}

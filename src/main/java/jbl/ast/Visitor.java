package jbl.ast;

import jbl.ast.nodes.*;

public interface Visitor<T> {
    T visitProgram(Program node);
    T visitFunDef(FunDef node);
    T visitIfStmt(IfStmt node);
    T visitWhileStmt(WhileStmt node);
    T visitReturnStmt(ReturnStmt node);
    T visitAssignStmt(AssignStmt node);
    T visitExprStmt(ExprStmt node);
    T visitBinaryExpr(BinaryExpr node);
    T visitUnaryExpr(UnaryExpr node);
    T visitNumberLit(NumberLit node);
    T visitBoolLit(BoolLit node);
    T visitVarExpr(VarExpr node);
    T visitCallExpr(CallExpr node);
}

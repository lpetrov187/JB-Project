package jbl;

import jbl.ast.Stmt;
import java.util.List;

public sealed interface Value permits IntVal, BoolVal, FunVal {}
record IntVal(int n)                                     implements Value {}
record BoolVal(boolean b)                                implements Value {}
record FunVal(List<String> params, List<Stmt> body)      implements Value {}

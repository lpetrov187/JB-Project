package jbl;

public class Main {
    public static void main(String[] args) throws java.io.IOException {
        String source = new String(System.in.readAllBytes());
        java.util.List<Token> tokens = new Lexer(source).tokenize();
        jbl.ast.nodes.Program ast   = new Parser(tokens).parseProgram();
        Interpreter interp = new Interpreter();
        interp.execute(ast);
        interp.printGlobals();
    }
}

package jbl;

public class Main {
    public static void main(String[] args) throws java.io.IOException {
        if (args.length == 3 && args[0].equals("--debug")) {
            String funcName = args[1];
            String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[2])));
            java.util.List<Token> tokens = new Lexer(source).tokenize();
            jbl.ast.nodes.Program ast = new Parser(tokens).parseProgram();
            DebugInterpreter interp = new DebugInterpreter(funcName);
            interp.execute(ast);
            interp.printGlobals();
        } else {
            String source = new String(System.in.readAllBytes());
            java.util.List<Token> tokens = new Lexer(source).tokenize();
            jbl.ast.nodes.Program ast   = new Parser(tokens).parseProgram();
            Interpreter interp = new Interpreter();
            interp.execute(ast);
            interp.printGlobals();
        }
    }
}

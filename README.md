# JBL Interpreter

A tree-walking interpreter for **JBL** — a small, hand-crafted language built in Java with Gradle.
The architecture includes a multi-stage compiler pipeline (Visitor pattern, scope management, recursive-descent parser) and targets an interpreter.

---

## Quickstart guide

### Option A — Docker (no JDK needed)
```bash
docker build -t jbl .
docker run --rm -i jbl < input_data/your_input.jbl
```

### Option B — Gradle wrapper (requires JDK 17+)
```bash
./gradlew run < input_data/your_input.jbl        # Unix/Mac
```
```bash
gradlew.bat run < input_data/your_input.jbl      # Windows
```

### Option C — Self-contained JAR (requires JDK 17+)
```bash
./gradlew jar
java -jar build/libs/jbl-1.0.0.jar < input_data/your_input.jbl
```

### Option D — Debug mode (interactive, function-exit breakpoints)
```bash
./gradlew run --args="--debug factorial input_data/factorial_demo.jbl"   # Unix/Mac
```
```bash
gradlew.bat run --args="--debug factorial input_data/factorial_demo.jbl" # Windows
```
```bash
docker run --rm -it jbl --debug factorial input_data/factorial_demo.jbl  # Docker
```

### Run tests
```bash
./gradlew test
```

---

## Language

JBL is a small imperative language. Programs are read from stdin; global variable values are printed to stdout on exit.

### Types
JBL has two runtime value types:
- **Integer** — numeric literals and arithmetic results
- **Boolean** — `true` literal and comparison results (`==`, `<`, `>`, etc.)

Integers are truthy when non-zero; booleans are truthy when `true`. Both can be used as `if`/`while` conditions.

### Syntax overview

```
# Assignment
x = 42
y = x + 1

# Arithmetic  (+  -  *  /)
z = (x + y) * 2 - 1

# Comparisons  (==  !=  <  <=  >  >=)
flag = x >= 10

# If / else  (single statement per branch)
if x > 0 then y = 1 else y = -1

# While loop  (greedy body — see below)
while x > 0 do x = x - 1

# Functions
fun square(n) { return n * n }
result = square(7)

# Recursion
fun fact(n) { if n == 0 then return 1 else return fact(n - 1) * n }
x = fact(10)
```

### Statement separator
Comma (`,`) separates statements on the same line:
```
x = 0, y = 1, z = x + y
```

### Greedy while body
The body of a `while` loop is a **greedy** comma-separated statement list — it inherits the surrounding stop context. This means:
```
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```
Both `if ...` **and** `x = x + 1` are **inside** the while body, not after it.

### Scoping & output
- Variables assigned at the top level are **global** and printed on exit (first-assignment order).
- Variables defined inside a function are **local** — they never appear in the output.
- Functions are stored separately and do not appear in the output.

### Sample programs

**Simple arithmetic**
```
x = 1 + 2 * 3
```
Output: `x: 7`

**While + if/else**
```
x = 0, y = 0
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```
Output:
```
x: 3
y: 11
```

**Recursive factorial**
```
fun fact(n) { if n == 0 then return 1 else return fact(n - 1) * n }
x = fact(5)
```
Output: `x: 120`

**Iterative factorial**
```
fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
x = fact_iter(5)
```
Output: `x: 120`

---

## Architecture

The interpreter is a classic multi-stage pipeline:

```
Source text
    |
    v
 Lexer          -> List<Token>
    |
    v
 Parser         -> AST (Program node)
    |
    v
 Interpreter    -> executes tree, collects globals
    |
    v
 stdout         name: value
```

### Components

| File | Role |
|------|------|
| `Token.java` | `TokenType` enum + `Token` record |
| `Lexer.java` | Hand-written tokenizer |
| `ast/Node.java` | Abstract AST base |
| `ast/Stmt.java` | Abstract statement base |
| `ast/Expr.java` | Abstract expression base |
| `ast/Visitor.java` | Visitor interface (one method per node type) |
| `ast/VisitorAdaptor.java` | Default no-op visitor |
| `ast/nodes/` | Concrete AST node classes |
| `Parser.java` | Recursive-descent parser with `stopAt` propagation |
| `Value.java` | Sealed runtime value type — `IntVal`, `BoolVal`, `FunVal` |
| `Environment.java` | Scope chain — current scope + parent reference |
| `ReturnSignal.java` | Control-flow exception used to unwind the call stack on `return` |
| `Interpreter.java` | Tree-walking evaluator, implements `Visitor<Value>` |
| `AstPrinter.java` | Debug visitor — S-expression and visual tree output |
| `Main.java` | Entry point — wires all stages together |

### Lexer (`Token.java` + `Lexer.java`)

`TokenType` is a plain enum. `Token` is an immutable record:
```java
record Token(TokenType type, String lexeme, int value, int line) {}
```
| Parameter | Type | Description |
|-----------|------|-------------|
| `type` | `TokenType` | The token's grammatical category (e.g. `NUMBER`, `PLUS`, `IDENT`, `IF`) |
| `lexeme` | `String` | The exact source text that was matched (e.g. `"42"`, `"+"`, `"myVar"`) |
| `value` | `int` | Pre-parsed integer for `NUMBER` tokens; `0` for all others |
| `line` | `int` | 1-based source line number, used in error messages |

The lexer is hand-written (no JFlex). Key rules:
- Spaces, tabs, and `\r` are silently skipped.
- `\n` emits a `NEWLINE` token (used as a statement terminator at the top level).
- `==`, `!=`, `<=`, `>=` are recognized with one-character lookahead inside `readSymbol()`.
- Keywords (`fun if then else while do return true`) are identified in `readIdent()`.

---

### AST (`ast/`)

The AST uses a classic object-oriented hierarchy with double-dispatch via the Visitor pattern.

**Base classes** (in `jbl.ast`):

| Class | Extends | Purpose |
|-------|---------|---------|
| `Node` | — | Root of the hierarchy; declares `accept(Visitor<T>)` |
| `Stmt` | `Node` | Marker base for all statement nodes |
| `Expr` | `Node` | Marker base for all expression nodes |

**Visitor interface** (`Visitor<T>`): one `visitXxx(Xxx node)` method per concrete node type. The type parameter `T` is the return type — `Value` for the interpreter, `String` for `AstPrinter`.

**`VisitorAdaptor<T>`**: abstract class that implements `Visitor<T>` with all methods returning `defaultResult()` (defaults to `null`). Subclasses override only the nodes they care about.

**Concrete nodes** (in `jbl.ast.nodes`):

| Node | Kind | Key fields |
|------|------|------------|
| `Program` | — | `List<Stmt> stmts` |
| `FunDef` | `Stmt` | `String name`, `List<String> params`, `List<Stmt> body` |
| `IfStmt` | `Stmt` | `Expr condition`, `Stmt thenBranch`, `Stmt elseBranch` (nullable) |
| `WhileStmt` | `Stmt` | `Expr condition`, `List<Stmt> body` |
| `ReturnStmt` | `Stmt` | `Expr value` |
| `AssignStmt` | `Stmt` | `String name`, `Expr value` |
| `ExprStmt` | `Stmt` | `Expr expr` (a function call used as a statement) |
| `BinaryExpr` | `Expr` | `Expr left`, `String op`, `Expr right` |
| `UnaryExpr` | `Expr` | `String op`, `Expr operand` |
| `NumberLit` | `Expr` | `int value` |
| `BoolLit` | `Expr` | `boolean value` (`true` → 1 at runtime) |
| `VarExpr` | `Expr` | `String name` |
| `CallExpr` | `Expr` | `String name`, `List<Expr> args` |

`IfStmt` holds **single** statements for each branch (not lists). `WhileStmt` holds a **list** — the greedy body collected by `parseStmts`.

---

### Parser (`Parser.java`)

A hand-written recursive-descent parser. All statement-parsing methods take a `Set<TokenType> stopAt` parameter that controls when a comma-separated list stops consuming statements.

**Entry point:** `parseProgram()` returns a `Program` node containing a flat list of all top-level statements.

**Grammar (simplified):**
```
program     -> line*
line        -> stmts NEWLINE
stmts       -> stmt (',' stmt)*
stmt        -> fun_def | if_stmt | while_stmt | return_stmt | assign | expr_stmt
fun_def     -> 'fun' IDENT '(' params ')' '{' stmts '}'
if_stmt     -> 'if' expr 'then' stmt ('else' stmt)?
while_stmt  -> 'while' expr 'do' stmts
return_stmt -> 'return' expr
assign      -> IDENT '=' expr
expr_stmt   -> IDENT '(' args ')'
expr        -> comparison -> arith -> term -> unary -> primary
```

**Key design decisions:**

- `parseWhileStmt` passes the **same** `stopAt` down to `parseStmts` for the body. This is what makes the while body greedy — commas after a while body are consumed by the while, not by the surrounding list.
- `parseIfStmt` calls `parseStmt` (not `parseStmts`) for each branch — only a single statement per branch.
- `parseFunDef` uses `stopAt = {RBRACE}` so the body stops at `}`.
- `parseArgs` uses commas as argument separators inside `(` ... `)` — no ambiguity with statement-level commas since those are always outside parens.
- IDENT disambiguation: one token lookahead — `=` means `AssignStmt`, `(` means `ExprStmt(CallExpr)`.

---

### Interpreter (`Interpreter.java`)

A tree-walking interpreter that extends `VisitorAdaptor<Value>`. It holds two environment references:
- `globalEnv` — created once, stores top-level variables and function definitions
- `currentEnv` — pointer to the active scope; swapped on each function call

**Statement visitors** return `null` and act through side effects (writing to the environment, looping, throwing).

**Expression visitors** return a `Value` that bubbles up to the parent node.

**Return handling:** `visitReturnStmt` throws a `ReturnSignal` (a lightweight `RuntimeException`). It propagates through all nested visitors untouched until `visitCallExpr` catches it. This avoids manual stack tracking.

**Function calls (`visitCallExpr`):**
1. Look up the name → must resolve to a `FunVal`
2. Evaluate all argument expressions
3. Create a fresh child scope with `globalEnv` as parent
4. Bind parameters to argument values in the child scope
5. Swap `currentEnv`, execute the body in a try/catch for `ReturnSignal`, restore `currentEnv` in `finally`

---

### Environment (`Environment.java`)

A simple chained scope: a `HashMap<String, Value>` plus a nullable `parent` reference.

- `get(name)` — looks in current scope first, then walks up the parent chain
- `set(name, value)` — always writes to the current scope

Two scopes exist at runtime: the global scope (`parent = null`) and one call scope per active function invocation (`parent = globalEnv`). Functions are not closures — they can only see globals and their own locals.

---

## Debug mode — illustrating IDEA-385867

JBL ships with an interactive debugger that demonstrates **why IntelliJ's emulated method-exit breakpoints cannot show return values**, and what the multi-stage fix looks like in practice.

### The problem

IntelliJ keeps emulated method-exit breakpoints cheap: instead of enabling `MethodExitWithReturnValue` monitoring across the JVM, it scans compiled bytecode for `xreturn` instructions and plants an ordinary line-level breakpoint there. When that breakpoint fires, the return value is sitting on the **operand stack** — but JDWP's `StackFrame.GetValues` only exposes named local variable slots. The operand stack is hidden from the API entirely. There is also no JDWP command to request the return value after the fact: the only source of that information is the `MethodExitWithReturnValue` event payload, and that event is only generated if exit monitoring was armed *before* the method started returning — which is exactly what emulated breakpoints were designed to avoid.

### The illustration — two stages

The illustration works by inserting two observable pause points around the return-value evaluation inside the interpreter.

- **Stage 1** — before the return expression is evaluated; the value does not exist yet. Corresponds to the emulated breakpoint firing.
- **Stage 2** — after the return expression is evaluated; the value is captured and printed. Corresponds to the point where the concrete fix would deliver it.

Each recursive call gets its own Stage 1 / Stage 2 pair. The depth counter makes the recursion structure visible: Stage 1s deepen the call stack, Stage 2s unwind it while accumulating the result.

This illustration is a parallel between JBL and the actual IntelliJ IDEA fix that is yet to be implemented. The solution to the real problem lies in retrieving the return value by placing strategically timed breakpoints around the function's exit — the exact mechanism is still open.

### Usage

```
./gradlew run --args="--debug <funcName> <file>"
docker run --rm -it jbl --debug <funcName> <file>
```

**Commands at the `(debug) >` prompt:**

| Command | Effect |
|---------|--------|
| `c` or `continue` | Resume execution |
| `locals` | Print the current function's local variables |
| `stack` | Print the call stack (innermost frame = #0) |

Locals are also printed automatically at each Stage 1 and Stage 2 pause.
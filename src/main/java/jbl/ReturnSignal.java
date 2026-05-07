package jbl;

class ReturnSignal extends RuntimeException {
    final Value value;

    ReturnSignal(Value value) {
        super(null, null, true, false); // suppress JVM stack trace overhead
        this.value = value;
    }
}

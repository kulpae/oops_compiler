/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der eine Referenz
 * dereferenziert, d.h. aus einer Variablen, deren Adresse gegeben ist, den
 * Wert ausliest.
 * Dieser Ausdruck wird immer nachträglich während der Kontextanalyse in
 * den Syntaxbaum eingefügt.
 */
class DeRefExpression extends Expression {
    /** Der Ausdruck, der die Adresse berechnet. */
    Expression operand;
    
    /**
     * Konstruktor.
     * @param operand Der Ausdruck, der die Adresse berechnet.
     */
    DeRefExpression(Expression operand) {
        super(operand.position);
        this.operand = operand;
        type = operand.type;
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    Expression optimizeTree() throws CompileException {
      operand.optimizeTree();
      return this;
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("DEREF" + (type == null ? "" : " : " + type.identifier.name));
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.println("; DEREF Argument code");
        operand.generateCode(code);
        code.println("; DEREF");
        code.println("MRM R5, (R2)");
        code.println("MRM R5, (R5)");
        code.println("MMR (R2), R5");
    }

    /** BEGIN Aufgabe (i): Vererbung */
    boolean bindsDynamically(){
      return operand.bindsDynamically();
    }
    /** END Aufgabe (i)*/
}

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der ein Objekt in
 * einen Wert eines Basisdatentyps auspackt ("unboxing").
 * Dieser Ausdruck wird immer nachträglich während der Kontextanalyse in
 * den Syntaxbaum eingefügt.
 */
class UnBoxExpression extends Expression {
    /** Der Ausdruck, der das auszupackende Objekt berechnet. */
    Expression operand;
    
    /**
     * Konstruktor.
     * Der Konstruktor stellt fest, von welcher Klasse der auszupackende
     * Ausdruck ist bestimmt den entsprechenden Basisdatentyp.
     * @param operand Der Ausdruck, der das auszupackende Objekt berechnet.
     */
    UnBoxExpression(Expression operand) {
        super(operand.position);
        this.operand = operand;
        if (operand.type.isA(ClassDeclaration.intClass)) {
            type = ClassDeclaration.intType;
        /** BEGIN Aufgabe (d): Boolean */
        } else if (operand.type.isA(ClassDeclaration.boolClass)) {
            type = ClassDeclaration.boolType;
        /** END Aufgabe(d) */
        } else {
            assert false;
        }
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    Expression optimizeTree() throws CompileException {
      operand = operand.optimizeTree();
      return this;
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("UNBOX" + (type == null ? "" : " : " + type.identifier.name));
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
        operand.generateCode(code);
        code.println("; UNBOX");
        code.println("MRM R5, (R2) ; Objektreferenz vom Stapel lesen");
	/** BEGIN Bonus Aufgabe (4): Try&Catch-Erweiterung 
	String catchDiv0Label = code.nextLabel();
	code.println("JPC R5, " + catchDiv0Label + " ; Überspringe Fehlerbehandlung UNBOX NULL");
	new ThrowStatement(new LiteralExpression(1, new ClassDeclaration(new Identifier("_Integer", null)) , position)).generateCode(code);
	code.println(catchDiv0Label + ":");
	/** END Bonus Aufgabe (4)*/
        // code.println("MRI R6, " + ClassDeclaration.HEADERSIZE);
        /** BEGIN Aufgabe (j): Garbage Collector*/
        code.println("MRI R6, " + (operand.type.objectSize - 1));
        /** END Aufgabe (j)*/
        code.println("ADD R5, R6 ; Adresse des Werts bestimmen");
        code.println("MRM R5, (R5) ; Wert auslesen");
        code.println("MMR (R2), R5 ; und auf den Stapel schreiben");
    }
}

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der ein neues Objekt erzeugt.
 */
class NewExpression extends Expression {
    /** Der Typ des neuen Objekts. */
    ResolvableIdentifier newType;
    
    /**
     * Konstruktor.
     * @param newType Der Typ des neuen Objekts.
     * @param position Die Position, an der dieser Ausdruck im Quelltext beginnt.
     */
    NewExpression(ResolvableIdentifier newType, Position position) {
        super(position);
        this.newType = newType;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression contextAnalysis(Declarations declarations) throws CompileException {
        declarations.resolveType(newType);
        type = (ClassDeclaration) newType.declaration;
        return this;
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("NEW " + newType.name + (type == null ? "" : " : " + type.identifier.name));
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.println("; NEW " + newType.name);
        code.println("ADD R2, R1");
        /** BEGIN Aufgabe (j): Garbage Collector*/
        // code.println("MMR (R2), R4 ; Referenz auf neues Objekt auf den Stapel legen");
        code.println("MRI R6, _free ; Adresse von _free holen");
        code.println("MRM R6, (R6) ; Referenz auf neues Objekt aus _free holen");
        code.println("MMR (R2), R6 ; Referenz auf neues Objekt auf den Stapel legen");
        /** BEGIN Aufgabe (i): Vererbung */
        code.println("MRI R5, " + ((ClassDeclaration)newType.declaration).identifier.name);
        // code.println("MMR (R4), R5 ; Referenz auf die VMT des Objects");
        code.println("MMR (R6), R5 ; Referenz auf die VMT des Objects");
        /** END Aufgabe (i) */
        // code.println("MRI R5, " + ((ClassDeclaration) newType.declaration).objectSize);
        // code.println("ADD R4, R5 ; Heap weiter zählen");
        // code.println("ADD R6, R5 ; Heap weiter zählen");
        /** BEGIN Aufgabe (j): Garbage Collector*/
        code.println("ADD R6, R1 ; Heap weiter zählen");
        code.println("MRI R5, 0 ; NULL Pointer ");
        for(int i=0; i<((ClassDeclaration) newType.declaration).objectSize; i++){
          code.println("MMR (R6), R5 ; Attribut "+i+" nullen");
          code.println("ADD R6, R1 ; Heap weiter zaehlen");
        }
        /** END Aufgabe (j)*/
        code.println("MRI R5, _free ; Adresse von _free holen");
        code.println("MMR (R5), R6 ; neuen Heap Pointer in _free ablegen");
        /** END Aufgabe (j) */
    }
}

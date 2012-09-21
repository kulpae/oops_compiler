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
        code.println("MMR (R2), R4 ; Referenz auf neues Objekt auf den Stapel legen");
        code.println("MRI R5, " + ((ClassDeclaration) newType.declaration).objectSize);
        code.println("ADD R4, R5 ; Heap weiter zählen");
    }
}

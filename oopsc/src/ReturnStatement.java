/**
 * Aufgabe (g): Return
 * Die Klasse realisiert die Anweisung 'RETURN' zum Verlassen einer Methoden.
 * (mit oder ohne einem Ergebnis)
 */
class ReturnStatement extends Statement {
    /** Der Ausdruck, dessen Wert zurueckgeliefert werden soll. */
    Expression value;

    /** Position des Return-Statements */
    Position position;
    
    /**
     * Konstruktor.
     * @param position Die Position des Return-Statements
     */
    ReturnStatement(Position position) {
        this.position = position;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        // Annahme, dass der returnType bereits aufgeloest wurde
        ClassDeclaration returnType = (ClassDeclaration) declarations.currentMethod.returnType.declaration;
        if(value == null){
            ClassDeclaration.voidType.check(returnType, position);
        } else {
            value = value.contextAnalysis(declarations);
            value = value.box(declarations);
            value.type.check(returnType, value.position);
        }

    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    void optimizeTree(){
      value.optimizeTree();
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("RETURN");
        if(value != null){
            tree.indent();
            value.print(tree);
            tree.unindent();
        }
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.println("; RETURN");
        if(value != null){
            value.generateCode(code);
        }
        //Springe ans Ende der Methode
        code.println("MRI R0, "+code.returnLabel());
    }

    /** Liefert true, wenn dieses Statement immer ein RETURN erreicht
     * @return true, wenn RETURN immer erreicht wird
     */
    boolean returnAccessible(){
        return true;
    }
    
}

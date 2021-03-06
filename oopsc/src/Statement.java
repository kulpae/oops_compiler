import java.util.LinkedList;

/**
 * Die abstrakte Basisklasse für alle Anweisungen im Syntaxbaum.
 */
abstract class Statement {
    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    abstract void contextAnalysis(Declarations declarations) throws CompileException;

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    abstract void print(TreeStream tree);

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    Statement optimizeStatement() throws CompileException {
	return this;
    }

    LinkedList<Statement> optimizeStatements() throws CompileException {
	LinkedList<Statement> list = new LinkedList<Statement>();
	list.add(optimizeStatement());
	return list;
    }
    /** END Bonus Aufgabe 2*/


    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    abstract void generateCode(CodeStream code);

    /** BEGIN Aufgabe (g): Return */
    /**
     * Liefert true, wenn dieses Statement immer ein RETURN erreicht
     * @return true, wenn RETURN immer erreicht wird
     */
    boolean returnAccessible(){
      return false;
    }
    /** End Aufgabe (g)*/
}

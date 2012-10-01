/** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
import java.util.LinkedList;

/**
 * Die Klasse repräsentiert die Anweisung Forever des optimierten Codes
 */
class ForeverStatement extends Statement {

    /** Die Anweisungen im Schleifenrumpf. */
    LinkedList<Statement> statements = new LinkedList<Statement>();
    
    /**
     * Konstruktor.
     * @param statements Die Statements des Forever-Statements
     */
    ForeverStatement(LinkedList<Statement> statements) {
	this.statements = statements;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        for (Statement s : statements) {
            s.contextAnalysis(declarations);
        }
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("FOREVER");
        tree.indent();
        if (!statements.isEmpty()) {
            tree.indent();
            for (Statement s : statements) {
                s.print(tree);
            }
            tree.unindent();
        }
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        String foreverLabel = code.nextLabel();
        String endLabel = code.nextLabel();
        code.println("; FOREVER");
        code.println(foreverLabel + ":");
        for (Statement s : statements) {
            s.generateCode(code);
        }
        code.println("; END FOREVER");
        code.println("MRI R0, " + foreverLabel);
        code.println(endLabel + ":");
    }
}
/** END Bonus Aufgabe 2*/

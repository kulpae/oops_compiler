/** BEGIN Aufgabe (h): Ausnahmebehandlung */
import java.util.LinkedList;

/**
 * Die Klasse repräsentiert die Anweisung TRY-CATCH im Syntaxbaum.
 */
class TryStatement extends Statement {
    /** Die Bedingung der CATCH-Anweisung. */
    Expression catchCode;
    
    /** Die Anweisungen im TRY-Teil. */
    LinkedList<Statement> tryStatements = new LinkedList<Statement>();

    /** Die Anweisungen im CATCH-Teil. */
    LinkedList<Statement> catchStatements = new LinkedList<Statement>();

    /**
     * Konstruktor.
     */
    TryStatement() {
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        catchCode = catchCode.contextAnalysis(declarations);
        catchCode = catchCode.unBox();
        catchCode.type.check(ClassDeclaration.intType, catchCode.position);
        for (Statement s : tryStatements) {
            s.contextAnalysis(declarations);
        }
        for (Statement s : catchStatements) {
            s.contextAnalysis(declarations);
        }
    }
    
    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("TRY");
        tree.indent();
        for (Statement s : tryStatements) {
          s.print(tree);
        }
        tree.unindent();
        tree.print("CATCH ");
        catchCode.print(tree);
        tree.indent();
        for (Statement s : catchStatements) {
          s.print(tree);
        }
        tree.unindent();
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        String catchLabel = code.nextLabel();
        String catchStatementLabel = code.nextLabel();
        String endLabel = code.nextLabel();

        code.println("; TRY Ausnahmerahmen");
        // Zustand sichern
        code.println("MRR R6, R2 ; R2 zwischenspeichern ");
        code.println("MRI R5, _exception ; _exception Adresse holen");
        code.println("MRM R5, (R5); _exception holen");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R5; _exception ist naechster Ausnahmerahmen");
        code.println("MRR R7, R2; Adresse vom Ausnahmerahmen holen");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R6 ; R2 sichern aus dem Zwischenspeicher");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R3 ; R3 sichern");
        code.println("MRI R5, "+catchLabel+" ; catch Zweig ist die Ausnahmebehandlung ");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R5 ");
        code.println("; _exception aktualisieren ");
        code.println("MRI R5, _exception");
        code.println("MMR (R5), R7 ");

        code.println("; TRY");
        for (Statement s : tryStatements) {
            s.generateCode(code);
        }

        code.println("MRI R0, " + endLabel + " ; Sprung zu END TRY");
        code.println("; CATCH");
        code.println(catchLabel + ":");

        code.println("MRM R7, (R2) ; Fehlercode holen");
        // nicht notwendig, da Stack wiederhergestellt wird
        // code.println("SUB R2, R1 ; pop");

        code.correctExceptionFrame();
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R7 ; Fehlercode auf dem Stack legen");

        //catchCode auf dem Stack ablegen
        catchCode.generateCode(code);

        //Throw-Wert,  Catch-Wert und Adresse der naechsten Ausnahmerbehandlung holen
        code.println("MRI R7, _exception ; _exception Adresse holen");
        code.println("MRM R7, (R7) ; _exception holen");
        code.println("MRI R5, 3 ; offset fuer Ausnahmebehandlung");
        code.println("ADD R7, R5 ; zeigt auf Stelle mit der naechste Ausnahmebehandlung");
        code.println("MRM R7, (R7) ; naechste Ausnahmebehandlung holen");
        code.println("MRM R5, (R2) ; pop catch value");
        code.println("SUB R2, R1");
        code.println("MRM R6, (R2) ; pop throw value");

        //Throw-Wert und Catch-Wert auf Gleichheit pruefen
        code.println("SUB R5, R6 ; 0 wenn gleich");
        code.println("ISZ R5, R5 ; wenn 0, ");
        code.println("JPC R5, " + catchStatementLabel + " ; springe zu CATCH statements ");
        code.println("MRR R0, R7 ; springe zur naechsten Ausnahmebehandlung");

        code.println("; CATCH STATEMENTS");
        code.println(catchStatementLabel + ":");
        code.println("SUB R2, R1 ; entferne Throw-Wert vom Stack ");
        for (Statement s : catchStatements) {
            s.generateCode(code);
        }

        code.println("; END TRY");
        code.println(endLabel + ":");
    }

    /** TryStatement erreicht RETURN, wenn TRY und CATCH-Zweige RETURN erreichen.
     * Falls THROW nicht aufgefangen wird, so ist die RETURN-Erreichbarkeit irrelevant */
    boolean returnAccessible(){
        boolean tryReturn = false;
        boolean catchReturn = false;

        for(Statement s: tryStatements){
            tryReturn = tryReturn || s.returnAccessible();
        }
        for(Statement s: catchStatements){
            catchReturn = catchReturn || s.returnAccessible();
        }
        return tryReturn && catchReturn;
    }

}
/** END Aufgabe (h) */

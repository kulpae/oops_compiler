import java.util.LinkedList;

/**
 * Die Klasse repräsentiert die Anweisung IF-THEN im Syntaxbaum.
 */
class IfStatement extends Statement {
    /** Die Bedingung der IF-Anweisung. */
    Expression condition;
    
    /** Die Anweisungen im THEN-Teil. */
    LinkedList<Statement> thenStatements = new LinkedList<Statement>();

    /** BEGIN Aufgabe (b): ELSEIF und ELSE */
    /** Die Anweisungen im ELSE-Teil. */
    LinkedList<Statement> elseStatements = new LinkedList<Statement>();
    /** END Aufgabe (b) */

    /**
     * Konstruktor.
     * @param condition Die Bedingung der IF-Anweisung.
     */
    IfStatement(Expression condition) {
        this.condition = condition;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        condition = condition.contextAnalysis(declarations);
        condition = condition.unBox();
        condition.type.check(ClassDeclaration.boolType, condition.position);
        for (Statement s : thenStatements) {
            s.contextAnalysis(declarations);
        }
        /** BEGIN Aufgabe (b): ELSEIF und ELSE */
        for (Statement s : elseStatements) {
            s.contextAnalysis(declarations);
        }
        /** END Aufgabe (b) */
    }
    
    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    LinkedList<Statement> optimizeStatements() throws CompileException {
	condition = condition.optimizeTree();
	LinkedList<Statement> list = new LinkedList<Statement>();
	for (Statement s : thenStatements) {
		list.addAll(s.optimizeStatements());
	}
	thenStatements = list;
	list = new LinkedList<Statement>();
	for (Statement s : elseStatements) {
		list.addAll(s.optimizeStatements());
	}
	elseStatements = list;
	if(condition instanceof LiteralExpression){
		LiteralExpression op = (LiteralExpression)condition;
			if(op.value == 0){
				return elseStatements;
			}else{
				return thenStatements;
			}
	}
	
	list = new LinkedList<Statement>();
	list.add(this);
	return list;
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("IF");
        tree.indent();
        condition.print(tree);
        if (!thenStatements.isEmpty()) {
            tree.println("THEN");
            tree.indent();
            for (Statement s : thenStatements) {
                s.print(tree);
            }
            tree.unindent();
        }
        /** BEGIN Aufgabe (b): ELSEIF und ELSE */
        if (!elseStatements.isEmpty()) {
            tree.println("ELSE");
            tree.indent();
            for (Statement s : elseStatements) {
                s.print(tree);
            }
            tree.unindent();
        }
        /** END Aufgabe (b) */
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        String elseLabel = code.nextLabel();//Aufgabe(b) Label für ELSE Statements
        String endLabel = code.nextLabel();
        code.println("; IF");
        condition.generateCode(code);
        code.println("MRM R5, (R2) ; Bedingung vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("ISZ R5, R5 ; Wenn 0, dann");
        code.println("JPC R5, " + elseLabel + " ; Sprung zu ELSE"); //Aufgabe(b) Sprung zu ELSE statt zum ende
        code.println("; THEN");
        for (Statement s : thenStatements) {
            s.generateCode(code);
        }
        /** BEGIN Aufgabe (b): ELSEIF und ELSE */
        code.println("MRI R0, " + endLabel + " ; Sprung zu END IF");
        code.println("; ELSE");
        code.println(elseLabel + ":");
        for (Statement s : elseStatements) {
            s.generateCode(code);
        }
        /** END Aufgabe (b) */
        code.println("; END IF");
        code.println(endLabel + ":");
    }

    /** BEGIN Aufgabe (g): Return */
    /** IfStatement erreicht RETURN, wenn THEN und ELSE-Zweige RETURN erreichen */
    boolean returnAccessible(){
        boolean thenReturn = false;
        boolean elseReturn = false;

        for(Statement s: thenStatements){
            thenReturn = thenReturn || s.returnAccessible();
        }
        for(Statement s: elseStatements){
            elseReturn = elseReturn || s.returnAccessible();
        }
        return thenReturn && elseReturn;
    }
    /** END Aufgabe (g) */
}

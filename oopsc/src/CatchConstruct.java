import java.util.LinkedList;

/** BEGIN Bonus Aufgabe 4: Try&Catch-Erweiterung*/
class CatchConstruct{
	Expression catchCode;
	LinkedList<Statement> catchStatements = new LinkedList<Statement>();

	/**
	 * Konstruktor.
	 */
	CatchConstruct(){
	}
	/**
	 * Die Methode führt die Kontextanalyse für diese Anweisung durch.
	 * @param declarations Die an dieser Stelle gültigen Deklarationen.
	 * @throws CompileException Während der Kontextanylyse wurde ein Fehler
	 *         gefunden.
	 */
	void contextAnalysis(Declarations declarations) throws CompileException {
		catchCode = catchCode.unBox();
		catchCode.type.check(ClassDeclaration.intType, catchCode.position);
	        
	        for (Statement s : catchStatements) {
	            s.contextAnalysis(declarations);
	        }
	}

    	CatchConstruct optimizeConstruct() throws CompileException {
		catchCode.optimizeTree();
		LinkedList<Statement> list = new LinkedList<Statement>();
		for (Statement s : catchStatements) {
			list.addAll(s.optimizeStatements());
		}
		catchStatements = list;
		return this;
	}

	/**
	 * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
	 * @param tree Der Strom, in den die Ausgabe erfolgt.
	 */
	void print(TreeStream tree) {
        	tree.print("CATCH ");
        	catchCode.print(tree);
        	tree.indent();
        	for (Statement s : catchStatements) {
        	  s.print(tree);
        	}
	}
	/** TryStatement erreicht RETURN, wenn TRY und CATCH-Zweige RETURN erreichen.
	 * Falls THROW nicht aufgefangen wird, so ist die RETURN-Erreichbarkeit irrelevant */
	boolean returnAccessible(){
	        boolean catchReturn = false;	
		for(Statement s: catchStatements){
	            catchReturn = catchReturn || s.returnAccessible();
        	}
        	return catchReturn;
	}

	Integer getCatchValue(){
		if(catchCode instanceof LiteralExpression){
			LiteralExpression e = (LiteralExpression)catchCode;
			return e.value;
		}else if(catchCode instanceof UnaryExpression){
			UnaryExpression e = (UnaryExpression)catchCode;
			if(catchCode instanceof LiteralExpression){
				LiteralExpression f = (LiteralExpression)e.operand;
				return -f.value;
			}
		}
		return null;
	}
}

/** END Bonus Aufgabe 4*/

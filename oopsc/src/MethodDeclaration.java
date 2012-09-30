import java.util.LinkedList;

/**
 * Die Klasse repräsentiert eine Methode im Syntaxbaum.
 */
class MethodDeclaration extends Declaration {
    /** Die lokale Variable SELF. */
    VarDeclaration self = new VarDeclaration(new Identifier("_self", null), false);
    /** BEGIN Aufgabe (i): Vererbung */
    /** Die lokale Variable BASE. */
    VarDeclaration base = new VarDeclaration(new Identifier("_base", null), false);
    /** END Aufgabe (i)*/

    /** Die lokalen Variablen der Methode. */
    LinkedList<VarDeclaration> vars = new LinkedList<VarDeclaration>();

    /** Die Anweisungen der Methode, d.h. der Methodenrumpf. */
    LinkedList<Statement> statements = new LinkedList<Statement>();

    /** BEGIN Aufgabe (f): Mathoden Parameter */
    /** Die Parameter der Methode */
    LinkedList<VarDeclaration> params = new LinkedList<VarDeclaration>();
    /** END Aufgabe (f) */

    /** BEGIN Aufgabe (g): Return */
    ResolvableIdentifier returnType = new ResolvableIdentifier("_Void", null);
    /** End Aufgabe (g)*/

    /** BEGIN Aufgabe (i) */
    /** Index der Methode in der VMT */
    int index;
    /** END Aufgabe (i) */

    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Methode.
     */
    MethodDeclaration(Identifier name) {
        super(name);
        /** BEGIN Aufgabe (g): Return */
        //falls kein Rueckgabewert angegeben, ist der Standard-Rueckgabewert bereits aufgeloest
        returnType.declaration = ClassDeclaration.voidType;
        /** END Aufgabe (g) */
    }

    /**
     * Führt die Kontextanalyse für diese Methoden-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        // SELF ist Variable vom Typ dieser Klasse
        self.type = new ResolvableIdentifier(declarations.currentClass.identifier.name, null);
        self.type.declaration = declarations.currentClass;
        /** BEGIN Aufgabe (i): Vererbung */
        // BASE aufloesen: ist eine Variable vom Typ der Basisklasse dieser Klasse
        if(declarations.currentClass.baseType != null){
            base.type = new ResolvableIdentifier(declarations.currentClass.baseType.name, null);
            base.type.declaration = declarations.currentClass.baseType.declaration;
        }
        /** END Aufgabe (i)*/

        /** BEGIN Aufgabe (f): Methoden Parameter */
        //Main.main darf keine Parameter haben!
        if(declarations.currentClass.identifier.name.equals("Main") && identifier.name.equals("main")){
          if(!params.isEmpty()){
            throw new CompileException("Methode Main.main darf keine Parameter haben", null);
          }
          /** BEGIN Aufgabe (g): Return*/
          if(hasReturnType()){
            throw new CompileException("Methode Main.main darf keinen Rueckgabewert haben", null);
          }
          /** END Aufgabe (g) */
        }
        /** END Aufgabe (f) */

        // Löse Typen aller Variablen auf
        for (VarDeclaration v : vars) {
            v.contextAnalysis(declarations);
        }

        // Neuen Deklarationsraum schaffen
        declarations.enter();

        /** BEGIN Aufgabe (g): Return */
        declarations.currentMethod = this;
        /** END Aufgabe (g)*/

        // SELF eintragen
        declarations.add(self);

        /** BEGIN Aufgabe (i): Vererbung */
        // Mache BASE sichtbar, wenn die Klasse eine Basisklasse hat
        if(declarations.currentClass.baseType != null){
          declarations.add(base);
        }
        /** END Aufgabe (i) */

        /** BEGIN Aufgabe (f): Methoden Parameter */
        int offset = -2 - params.size();
        // SELF liegt vor der Rücksprungadresse auf dem Stapel
        // self.offset = -2;
        self.offset = offset++;

        /** BEGIN Aufgabe (i): Vererbung */
        // BASE und SELF zeigen auf die selbe Stelle
        base.offset = self.offset;
        /** END Aufgabe (i) */

        // Parameter eintragen
        for (VarDeclaration v : params) {
            declarations.add(v);
            v.offset = offset++;
        }

        // Rücksprungadresse und alten Rahmenzeiger überspringen
        // int offset = 1;
        offset = 1; // Aufgabe (f)

        /** END Aufgabe (f)*/

        // Lokale Variablen eintragen
        for (VarDeclaration v : vars) {
            declarations.add(v);
            v.offset = offset++;
        }

        // Kontextanalyse aller Anweisungen durchführen
        // for (Statement s : statements) {
        //     s.contextAnalysis(declarations);
        // }
        /** BEGIN Aufgabe (g): Return */
        boolean returnAccessible = false;
        for(Statement s: statements){
            s.contextAnalysis(declarations);
            returnAccessible = returnAccessible || s.returnAccessible();
        }
        if(hasReturnType() && !returnAccessible){
          throw new CompileException("Auf jedem Ausfuehrungspfad wird ein Rueckgabewert erwartet", null);
        }
        /** END Aufgabe (g) */

        // Alten Deklarationsraum wiederherstellen
        declarations.leave();
    }

    /** BEGIN Aufgabe (f): Methoden Parameter */
    /**
     * Führt die Kontextanalyse für die Signatur dieser Methoden-Deklaration durch.
     * Hier werden die Bezeichner aufgeloest, die ausserhalb der Methode relevant sind
     * (Parameter und Rueckgabewert).
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysisForSignature(Declarations declarations) throws CompileException {
      // Löse Typen aller Parameter auf
      for (VarDeclaration v : params) {
        v.contextAnalysis(declarations);
      }
      /** BEGIN Aufgabe (g): Return */
      declarations.resolveType(returnType);
      /** END Aufgabe (g)*/
    }
    /** END Aufgabe (f)*/

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    void optimizeTree(){
      LinkedList<Statement> list = new LinkedList<Statement>();
	for (Statement s : statements) {
		list.addAll(s.optimizeStatements());
	}
	statements = list;
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        // tree.println("METHOD " + identifier.name);
        /** BEGIN Aufgabe (g): Return */
        String returnTypeExt = "";
        if(hasReturnType()){
          returnTypeExt = " : " + returnType.name;
        }
        tree.println("METHOD " + identifier.name + returnTypeExt);
        /** END Aufgabe (g) */

        tree.indent();
        /** BEGIN Aufgabe (f): Methoden Parameter */
        if (!params.isEmpty()) {
            tree.println("PARAMETERS");
            tree.indent();
            for (VarDeclaration v : params) {
                v.print(tree);
            }
            tree.unindent();
        }
        /** END Aufgabe (f) */
        if (!vars.isEmpty()) {
            tree.println("VARIABLES");
            tree.indent();
            for (VarDeclaration v : vars) {
                v.print(tree);
            }
            tree.unindent();
        }
        if (!statements.isEmpty()) {
            tree.println("BEGIN");
            tree.indent();
            for (Statement s : statements) {
                s.print(tree);
            }
            tree.unindent();
        }
        tree.unindent();
    }

    /**
     * Generiert den Assembler-Code für diese Methode. Dabei wird davon ausgegangen,
     * dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.setNamespace(self.type.name + "_" + identifier.name);
        code.println("; METHOD " + identifier.name);
        code.println(self.type.name + "_" + identifier.name + ":");
        code.println("ADD R2, R1");
        code.println("MMR (R2), R3 ; Alten Stapelrahmen sichern");
        code.println("MRR R3, R2 ; Aktuelle Stapelposition ist neuer Rahmen");
        if (!vars.isEmpty()) {
            code.println("MRI R5, " + vars.size());
            code.println("ADD R2, R5 ; Platz für lokale Variablen schaffen");
        }
        for (Statement s : statements) {
            s.generateCode(code);
            /** BEGIN Aufgabe (g): Return */
            // Weitere Anweisungen ignorieren, wenn diese Anweisung RETURN erreicht
            if(s.returnAccessible()){
              break;
            }
            /** END Aufgabe (g) */

        }

        /** BEGIN Aufgabe (g): Return */
        code.println(code.returnLabel()+":");
        if(hasReturnType()){
            code.println("MRM R6, (R2); Rueckgabewert sichern");
            code.println("SUB R2, R1");
        }
        /** END Aufgabe (g)*/

        code.println("; END METHOD " + identifier.name);
        // code.println("MRI R5, " + (vars.size() + 3));
        /** BEGIN Aufgabe (f): Methoden Parameter */
        code.println("MRI R5, " + (vars.size() + 3 + params.size()));
        /** END Aufgabe (f) */
        code.println("SUB R2, R5 ; Stack korrigieren");
        /** BEGIN Aufgabe (g): Return */
        if(hasReturnType()){
            code.println("ADD R2, R1");
            code.println("MMR (R2), R6; Rueckgabewert auf dem Stack ablegen");
        }
        /** END Aufgabe (g)*/
        code.println("SUB R3, R1");
        code.println("MRM R5, (R3) ; Rücksprungadresse holen");
        code.println("ADD R3, R1");
        code.println("MRM R3, (R3) ; Alten Stapelrahmen holen");
        code.println("MRR R0, R5 ; Rücksprung");
    }

    /** BEGIN Aufgabe (g): Return */
    /**
     * @return true, wenn diese Methode einen Rueckgabewert besitzt
     */
    boolean hasReturnType(){
      return returnType != null && returnType.declaration != ClassDeclaration.voidType;
    }
    /** END Aufgabe (g)*/

    /** BEGIN Aufgabe (i) */
    /**
     * Liefert true, wenn beide Methoden den selben Bezeicher haben
     * @param o Zu vergleichende Methode
     * @return true, wenn o eine Methode ist, die den gleichen Bezeichner hat,
     * wie diese Methode.
     */
    public boolean equals(Object o){
        return (o instanceof MethodDeclaration) && ((MethodDeclaration)o).identifier.equals(identifier);
    }

    /**
     * Liefert true, wenn beide Methoden die selbe Signatur haben.
     * Zur Signatur gehoeren: Anzahl und Type der Parameter, Identifier und der
     * Type des Rueckgabewertes
     * @param o zu vergleichende Methodendeklaration
     * @return true, wenn Signaturen gleich sind
     */
    boolean sameSignature(MethodDeclaration o){
        return this.equals(o) && this.sameParameters(o) && this.sameReturnType(o);
    }

    /**
     * Liefert true, wenn beide Methoden die selben Parameter haben.
     * @param o zu vergleichende Methodendeklaration
     * @return true, wenn Parameter gleich sind
     */
    boolean sameParameters(MethodDeclaration o){
        if(o.params.size() != params.size()){
            return false;
        }
        for(int i=0; i<params.size(); i++){
            if(!o.params.get(i).type.declaration.identifier.equals(params.get(i).type.declaration.identifier)){
                return false;
            }
        }
        return true;
    }
    /**
     * Liefert true, wenn beide Methoden den selben Rueckgabewert haben.
     * @param o zu vergleichende Methodendeklaration
     * @return true, wenn Rueckgabewerte gleich sind
     */
    boolean sameReturnType(MethodDeclaration o){
        return o.returnType.declaration == returnType.declaration;
    }

    /** END Aufgabe (i) */
}

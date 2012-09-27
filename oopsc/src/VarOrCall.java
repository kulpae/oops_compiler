import java.util.LinkedList;
/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der dem Zugriff auf eine 
 * Variable oder ein Attribut bzw. einem Methodenaufruf entspricht.
 */
class VarOrCall extends Expression {
    /** Der Name des Attributs, der Variablen oder der Methode. */
    ResolvableIdentifier identifier;

    /** BEGIN Aufgabe (f): Methoden Parameter */
    /** aktuelle Parameter des Methodenaufrufs */
    LinkedList<Expression> params = new LinkedList<Expression>();
    /** END Aufgabe (f)*/
    
    /**
     * Konstruktor.
     * @param identifier Der Name des Attributs, der Variablen oder der Methode.
     */
    VarOrCall(ResolvableIdentifier identifier) {
        super(identifier.position);
        this.identifier = identifier;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * Dabei wird ein Zugriff über SELF in den Syntaxbaum eingefügt,
     * wenn dieser Ausdruck ein Attribut oder eine Methode bezeichnet.
     * Diese Methode wird niemals für Ausdrücke aufgerufen, die rechts
     * vom Objekt-Zugriffsoperator stehen.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck oder ein neuer Ausdruck, falls ein Boxing 
     *         oder der Zugriff über SELF eingefügt wurde.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression contextAnalysis(Declarations declarations) throws CompileException {
        contextAnalysisForMember(declarations);
        if (identifier.declaration instanceof MethodDeclaration || 
                identifier.declaration instanceof VarDeclaration && ((VarDeclaration) identifier.declaration).isAttribute) {
            AccessExpression a = new AccessExpression(new VarOrCall(new ResolvableIdentifier("_self", position)), this);
            a.leftOperand = a.leftOperand.contextAnalysis(declarations);
            a.leftOperand = a.leftOperand.box(declarations);
            a.type = type;
            a.lValue = lValue;
            /** BEGIN Ausgabe (f): Methoden Parameter */
            contextAnalysisForParameters(declarations);
            /** END Aufgabe (f) */
            return a;
        } else {
            return this;
        }
    }
    
    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * Diese Methode wird auch für Ausdrücke aufgerufen, die rechts
     * vom Objekt-Zugriffsoperator stehen.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysisForMember(Declarations declarations) throws CompileException {
        declarations.resolveVarOrMethod(identifier);
        if (identifier.declaration instanceof VarDeclaration) {
            type = (ClassDeclaration) ((VarDeclaration) identifier.declaration).type.declaration;
            lValue = true;
        } else if (identifier.declaration instanceof MethodDeclaration) {
            type = ClassDeclaration.voidType;
        } else {
            assert false;
        }
    }

    /** BEGIN Aufgabe (f): Methoden Parameter */
    /**
     * Die Methode führt die Kontextanalyse für die aktuellen Parameter durch.
     * (falls notwendig)
     * Hier wird geprueft, ob die aktuelle Parameter zu den formalen Parametern passen.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysisForParameters(Declarations declarations) throws CompileException{
        if (identifier.declaration instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) identifier.declaration;
            LinkedList<VarDeclaration> mdecl = method.params;
            //zu viele Parameter angegeben?
            if(params.size() > mdecl.size()){
                throw new CompileException("Zu viele Parameter", position);
            }
            //zu wenige Parameter angegeben?
            if(params.size() < mdecl.size()){
                throw new CompileException("Zu wenige Parameter", position);
            }
            // stimmt der Typ der Parameter?
            for(int p=0; p< mdecl.size(); p++){
                Expression v = params.get(p);
                v.contextAnalysis(declarations);
                //boxen/dereferenzieren
                v = v.box(declarations);
                params.set(p, v);
                ClassDeclaration mtype = (ClassDeclaration) mdecl.get(p).type.declaration;
                v.type.check(mtype, v.position);
            }
        }
    }
    /** END Aufgabe (f) */
    
    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println(identifier.name + (type == null ? "" : " : " + 
                (lValue ? "REF " : "") + type.identifier.name));
        /** BEGIN Aufgabe (f): Methoden Parameter */
        if(!params.isEmpty()){
          tree.indent();
          tree.println("PARAMETERS");
          tree.indent();
          for(Expression p: params){
            p.print(tree);
          }
          tree.unindent();
          tree.unindent();
        }
        /** END Aufgabe (f) */
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        if (identifier.declaration instanceof VarDeclaration) {
            VarDeclaration v = (VarDeclaration) identifier.declaration;
            if (v.isAttribute) {
                code.println("; Referenz auf Attribut " + identifier.name);
                code.println("MRM R5, (R2)");
                code.println("MRI R6, " + v.offset);
                code.println("ADD R5, R6");
                code.println("MMR (R2), R5");
            } else {
                code.println("; Referenz auf Variable " + identifier.name);
                code.println("MRI R5, " + v.offset);
                code.println("ADD R5, R3");
                code.println("ADD R2, R1");
                code.println("MMR (R2), R5");
            }
        } else if (identifier.declaration instanceof MethodDeclaration) {
            MethodDeclaration m = (MethodDeclaration) identifier.declaration;
            /** BEGIN Aufgabe (f): Methoden Parameter */
            if(!params.isEmpty()){
              code.println("; CALL "+m.self.type.name + "."+m.identifier.name);
              for(int i = 0; i< params.size(); i++){
                Expression p = params.get(i);
                code.println("; Parameter "+i+":");
                p.generateCode(code);
              }
            }
            /** END Aufgabe (f) */
            String returnLabel = code.nextLabel();
            code.println("MRI R5, " + returnLabel);
            code.println("ADD R2, R1");
            code.println("MMR (R2), R5 ; Rücksprungadresse auf den Stapel");
            code.println("; Statischer Aufruf von " + identifier.name);
            code.println("MRI R0, " + m.self.type.name + "_" + m.identifier.name);
            code.println(returnLabel + ":");
        } else {
            assert false;
        }
    }
}

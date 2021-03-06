/**
 * Die abstrakte Basisklasse für alle Ausdrücke im Syntaxbaum.
 * Zusätzlich zur Standardschnittstelle für Ausdrücke definiert sie auch
 * Methoden zur Erzeugung neuer Ausdrücke für das Boxing und Unboxing von
 * Ausdrücken sowie das Dereferenzieren.
 */
abstract class Expression {
    /** Der Typ dieses Ausdrucks. Solange er nicht bekannt ist, ist dieser Eintrag null. */
    // ClassDeclaration type;
    /** BEGIN Bonus Aufgabe 3: Mehrere Fehlermeldungen */
    // ClassDeclaration type;
    ClassDeclaration type = ClassDeclaration.univType;
    /** END Bonus Aufgabe 3*/

    /**
     * Ist dieser Ausdruck ein L-Wert, d.h. eine Referenz auf eine Variable?
     * Die meisten Ausdrücke sind keine L-Werte.
     */
    boolean lValue = false;

    /** Die Quelltextposition, an der dieser Ausdruck beginnt. */
    Position position;

    /**
     * Konstruktor.
     * @param position Die Quelltextposition, an der dieser Ausdruck beginnt.
     */
    Expression(Position position) {
        this.position = position;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * Sie ist nicht abstrakt, da es einige abgeleitete Klassen gibt,
     * die sie nicht implementieren, weil sie dort nicht benötigt wird.
     * Da im Rahmen der Kontextanalyse auch neue Ausdrücke erzeugt werden
     * können, sollte diese Methode immer in der Form "a = a.contextAnalysis(...)"
     * aufgerufen werden, damit ein neuer Ausdruck auch im Baum gespeichert wird.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck oder ein neuer Ausdruck, falls ein Boxing,
     *         Unboxing oder eine Dereferenzierung in den Baum eingefügt
     *         wurden.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression contextAnalysis(Declarations declarations) throws CompileException {
        return this;
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    Expression optimizeTree() throws CompileException {
	return this;
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    abstract void print(TreeStream tree);

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    abstract void generateCode(CodeStream code);

    /**
     * Die Methode prüft, ob dieser Ausdruck "geboxt" oder dereferenziert werden muss.
     * Ist dies der Fall, wird ein entsprechender Ausdruck erzeugt, von dem dieser
     * dann der Operand ist. Dieser neue Ausdruck wird zurückgegeben. Daher sollte diese
     * Methode immer in der Form "a = a.box(...)" aufgerufen werden.
     * "Boxing" ist das Verpacken eines Basisdatentyps in ein Objekt. Dereferenzieren ist
     * das Auslesen eines Werts, dessen Adresse angegeben wurde.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck oder ein neuer Ausdruck, falls ein Boxing oder eine
     *         Dereferenzierung eingefügt wurde.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression box(Declarations declarations) throws CompileException {
        /** BEGIN Aufgabe (j): Garbage Collector*/
        VarOrCall var = null;
        if(this instanceof AccessExpression){
          var = ((AccessExpression)this).rightOperand;
        } else if(this instanceof VarOrCall){
          var = (VarOrCall)this;
        }
        if(var != null && var.identifier.name.equals("_value")){
          return new DeRefExpression(this);
        }
        /** END Aufgabe (j)*/

        // if (type.isA(ClassDeclaration.intType)) {
        /** BEGIN Aufgabe (d): Boolean */
        if (type.isA(ClassDeclaration.intType) || type.isA(ClassDeclaration.boolType)) {
        /** END Aufgabe (d) */
            return new BoxExpression(this, declarations);
        } else if (lValue) {
            return new DeRefExpression(this);
        /** BEGIN Aufgabe (i): Vererbung*/
        } else if (this instanceof VarOrCall &&
            (((VarOrCall)this).identifier.name.equals("_self") ||
             ((VarOrCall)this).identifier.name.equals("_base")) ) {
            return new DeRefExpression(this);
        /** END Aufgabe (i)*/
        } else {
            return this;
        }
    }

    /**
     * Die Methode prüft, ob dieser Ausdruck dereferenziert, "entboxt" oder beides
     * werden muss.
     * Ist dies der Fall, wird ein entsprechender Ausdruck erzeugt, von dem dieser
     * dann der Operand ist. Dieser neue Ausdruck wird zurückgegeben. Daher sollte diese
     * Methode immer in der Form "a = a.unBox(...)" aufgerufen werden.
     * "Unboxing" ist das Auspacken eines Objekts zu einem Basisdatentyp. Dereferenzieren ist
     * das Auslesen eines Werts, dessen Adresse angegeben wurde.
     * @return Dieser Ausdruck oder ein neuer Ausdruck, falls ein Unboxing und/oder eine
     *         Dereferenzierung eingefügt wurde(n).
     */
    Expression unBox() {
        if (lValue) {
            return new DeRefExpression(this).unBox();
        // } else if (type != ClassDeclaration.nullType && type.isA(ClassDeclaration.intClass)) {
        /** BEGIN Aufgabe (d): Boolean */
        } else if (type != ClassDeclaration.nullType && (type.isA(ClassDeclaration.intClass) || type.isA(ClassDeclaration.boolClass))) {
        /** END Aufgabe (d) */
            return new UnBoxExpression(this);
        } else {
            return this;
        }
    }


    /** BEGIN Aufgabe (i): Vererbung */
    boolean bindsDynamically(){
      return true;
    }
    /** END Aufgabe (i)*/
}

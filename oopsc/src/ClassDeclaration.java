import java.util.LinkedList;

/**
 * Die Klasse repräsentiert eine Klassendeklaration im Syntaxbaum.
 * Zudem stellt sie Methoden zum Typvergleich zur Verfügung.
 */
class ClassDeclaration extends Declaration {
    /**
     * Konstante für die Größe der Verwaltungsinformation am Anfang eines jeden Objekts.
     * Bisher ist die Größe 0.
     */
    // static final int HEADERSIZE = 0;
    /** BEGIN Aufgabe (i): Vererbung */
    static final int HEADERSIZE = 1;
    /** END Aufgabe (i) */

    /** Ein interner Typ für das Ergebnis von Methoden. */
    static final ClassDeclaration voidType = new ClassDeclaration(new Identifier("_Void", null));

    /** Ein interner Typ für null. Dieser Typ ist kompatibel zu allen Klassen. */
    static final ClassDeclaration nullType = new ClassDeclaration(new Identifier("_Null", null));

    /** Der interne Basisdatentyp für Zahlen. */
    static final ClassDeclaration intType = new ClassDeclaration(new Identifier("_Integer", null));

    /** Der interne Basisdatentyp für Wahrheitswerte. */
    static final ClassDeclaration boolType = new ClassDeclaration(new Identifier("_Boolean", null));

    /** Die Klasse Integer. */
    static final ClassDeclaration intClass = new ClassDeclaration(new Identifier("Integer", null));

    /** BEGIN Aufgabe (d): Boolean */
    /** Die Klasse Boolean. */
    static final ClassDeclaration boolClass = new ClassDeclaration(new Identifier("Boolean", null));
    /** END Aufgabe (d) */

    /** BEGIN Aufgabe (i): Vererbung */
    /** Die Klasse Object*/
    static final ClassDeclaration objectClass = new ClassDeclaration(new Identifier("Object", null));

    /** Die Basisklasse. Standardmaessig ist sie nicht gesetzt*/
    ResolvableIdentifier baseType = null;
    /** END Aufgabe (i) */

    /** Die Attribute dieser Klasse. */
    LinkedList<VarDeclaration> attributes = new LinkedList<VarDeclaration>();

    /** Die Methoden dieser Klasse. */
    LinkedList<MethodDeclaration> methods = new LinkedList<MethodDeclaration>();

    /** BEGIN Aufgabe (i): Vererbung */
    MethodDeclaration[] vmt;
    /** END Aufgabe (i) */

    /** Die innerhalb dieser Klasse sichtbaren Deklarationen. */
    Declarations declarations;

    /**
     * Die Größe eines Objekts dieser Klasse. Die Größe wird innerhalb von
     * {@link #contextAnalysis(Declarations) contextAnalysis} bestimmt.
     */
    int objectSize;

    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Klasse.
     */
    ClassDeclaration(Identifier name) {
        super(name);
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Klassen-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        // Standardgröße für Objekte festlegen
        objectSize = HEADERSIZE;

        /** BEGIN Aufgabe (i): Vererbung */
        //Methodenzaehler
        int mIdx = 0;
        //VMT anlegen
        vmt = new MethodDeclaration[methods.size()];

        if(baseType != null){
          declarations.resolveType(baseType);
          ClassDeclaration clazz = this;
          while(clazz.baseType != null && (clazz = ((ClassDeclaration)clazz.baseType.declaration)) != null){
            if(clazz == this){
              throw new CompileException("Zyklische Vererbung in Klasse "+identifier.name+" entdeckt", baseType.position);
            }
          }

          // Basisklasse vor aktueller Klasse
          baseType.declaration.contextAnalysis(declarations);
          //aktuelle VMT um die Groesse der VMT der Basisklasse vergroessern
          vmt = new MethodDeclaration[((ClassDeclaration)baseType.declaration).vmt.length + vmt.length];
          //Virtuelle Methoden der Basisklasse in die aktuelle VMT aufnehmen
          for(MethodDeclaration m: ((ClassDeclaration)baseType.declaration).vmt){
            if(m != null){
              vmt[mIdx++] = m;
            }
          }

          // Aktuelle Klasse erbt den Sichtbarkeitsbereich der Basisklasse
          declarations = (Declarations) ((ClassDeclaration)baseType.declaration).declarations.clone();
          //objectSize von der Basisklasse wird hier weiter benutzt
          objectSize = ((ClassDeclaration) baseType.declaration).objectSize;

        }
        /** END Aufgabe (i)*/

        // Attributtypen auflösen und Indizes innerhalb des Objekts vergeben
        for (VarDeclaration a : attributes) {
            a.contextAnalysis(declarations);
            a.offset = objectSize++;
        }

        // Neuen Deklarationsraum schaffen
        declarations.enter();
        declarations.currentClass = this;

        // Attribute eintragen
        for (VarDeclaration a : attributes) {
            declarations.add(a);
        }

        // Methoden eintragen
        for (MethodDeclaration m : methods) {
            declarations.add(m);
        }

        // Wird auf ein Objekt dieser Klasse zugegriffen, werden die Deklarationen
        // in diesem Zustand benötigt. Deshalb werden sie in der Klasse gespeichert.
        this.declarations = (Declarations) declarations.clone();

        /** BEGIN Aufgabe (f): Methoden Parameter */
        for (MethodDeclaration m : methods) {
            m.contextAnalysisForSignature(declarations);
            /** BEGIN Aufgabe (i): Vererbung */
            // Wenn die Methode bekannt ist, dann ueberschreiben
            // Ansonsten anhaengen
            boolean found = false;
            for(int i=0; i<vmt.length; i++){
              if(vmt[i] != null && vmt[i].equals(m)){
                m.index = i;
                found = true;
                if(vmt[i].sameSignature(m)){
                  vmt[i] = m; // Ueberschreiben
                } else {
                  throw new CompileException("Ueberladung nicht erlaubt!", m.identifier.position);
                }
              }
            }

            // Wenn neu, dann anhaengen
            if(!found){
              m.index = mIdx++;
              vmt[m.index] = m;
            }

            /** END Aufgabe (i) */
        }
        /** END Aufgabe (f) */

        /** BEGIN Aufgabe (e): Mehrere Klassen */
        // // Kontextanalyse für Methoden durchführen
        // for (MethodDeclaration m : methods) {
        //     m.contextAnalysis(declarations);
        // }
        /** END Aufgabe (e) */

        // Deklarationsraum verlassen
        declarations.leave();
    }

    /** BEGIN Aufgabe (e): Mehrere Klassen */
    /**
     * Die Methode führt die Kontextanalyse für den Rumpf dieser Klassen-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysisForBody(Declarations declarations) throws CompileException {
        // Kontextanalyse für Methoden durchführen
        for (MethodDeclaration m : methods) {
            m.contextAnalysis(declarations);
        }
    }
    /** END Aufgabe (e) */


    /**
     * Die Methode prüft, ob dieser Typ kompatibel mit einem anderen Typ ist.
     * @param expected Der Typ, mit dem verglichen wird.
     * @return Sind die beiden Typen sind kompatibel?
     */
    boolean isA(ClassDeclaration expected) {
        // Spezialbehandlung für null, das mit allen Klassen kompatibel ist,
        // aber nicht mit den Basisdatentypen _Integer und _Boolean sowie auch nicht
        // an Stellen erlaubt ist, wo gar kein Wert erwartet wird.
        /** BEGIN Aufgabe (i): Vererbung */
        // if (this == nullType && expected != intType && expected != boolType && expected != voidType) {
        //     return true;
        // } else {
        //     return this == expected;
        // }
        return (this == expected) ||
          ((this == nullType) && (expected != null) && expected.isA(objectClass)) ||
          ((this != objectClass) && (this.baseType != null) && ((ClassDeclaration)this.baseType.declaration).isA(expected));
        /** END Aufgabe (i)*/
    }

    /**
     * Die Methode erzeugt eine Ausnahme für einen Typfehler. Sie wandelt dabei intern verwendete
     * Typnamen in die auch außen sichtbaren Namen um.
     * @param expected Der Typ, der nicht kompatibel ist.
     * @param position Die Stelle im Quelltext, an der der Typfehler gefunden wurde.
     * @throws CompileException Die Meldung über den Typfehler.
     */
    static void typeError(ClassDeclaration expected, Position position) throws CompileException {
        if (expected == intType) {
            throw new CompileException("Ausdruck vom Typ Integer erwartet", position);
        } else if (expected == boolType) {
            throw new CompileException("Ausdruck vom Typ Boolean erwartet", position);
        } else if (expected == ClassDeclaration.voidType) {
            throw new CompileException("Hier darf keinen Wert zurückgeliefert werden", position);
        } else {
            throw new CompileException("Ausdruck vom Typ " + expected.identifier.name + " erwartet", position);
        }
    }

    /**
     * Die Methode prüft, ob dieser Typ kompatibel mit einem anderen Typ ist.
     * Sollte das nicht der Fall sein, wird eine Ausnahme mit einer Fehlermeldung generiert.
     * @param expected Der Typ, mit dem verglichen wird.
     * @param position Die Position im Quelltext, an der diese Überprüfung
     *         relevant ist. Die Position wird in der Fehlermeldung verwendet.
     * @throws CompileException Die Typen sind nicht kompatibel.
     */
    void check(ClassDeclaration expected, Position position) throws CompileException {
        if (!isA(expected)) {
            typeError(expected, position);
        }
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    void optimizeTree(){
      for (MethodDeclaration m : methods) {
        m.optimizeTree();
      }
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        /** BEGIN Aufgabe (i): Vererbung */
        if(baseType != null){
          tree.format("CLASS %s EXTENDS %s\n", identifier.name, baseType.name);
        } else {
          tree.println("CLASS " + identifier.name);
        }
        /** END Aufgabe (i)*/
        tree.indent();
        if (!attributes.isEmpty()) {
            tree.println("ATTRIBUTES");
            tree.indent();
            for (VarDeclaration a : attributes) {
                a.print(tree);
            }
            tree.unindent();
        }
        if (!methods.isEmpty()) {
            tree.println("METHODS");
            tree.indent();
            for (MethodDeclaration m : methods) {
                m.print(tree);
            }
            tree.unindent();
        }
        /** BEGIN Aufgabe (i): Vererbung */
        tree.println("VMT");
        tree.indent();
        for(int i=0; i<vmt.length; i++){
          if(vmt[i] != null){
            tree.format("%2d: %s.%s\n", i, vmt[i].self.type.declaration.identifier.name, vmt[i].identifier.name);
          }
        }
        tree.unindent();
        /** END Aufgabe (i)*/
        tree.unindent();
    }

    /**
     * Generiert den Assembler-Code für diese Klasse. Dabei wird davon ausgegangen,
     * dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.println("; CLASS " + identifier.name);

        // Synthese für alle Methoden
        for (MethodDeclaration m : methods) {
            m.generateCode(code);
        }
        code.println("; END CLASS " + identifier.name);
    }

    /** BEGIN Aufgabe (i): Vererbung*/
    /**
     * Generiert die Virtuelle Methodentabelle dieser Klasse.
     * Dabei wird davon ausgegangen, dass die Kontextanalyse vorher
     * erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateVMTCode(CodeStream code){
      code.println("; VMT "+identifier.name);
      code.println(identifier.name+":");
      for(MethodDeclaration m: vmt){
        if(m!= null){
          code.format("DAT 1, %s_%s\n", identifier.name, m.identifier.name);
        }
      }
    }
    /** END Aufgabe (i)*/
}

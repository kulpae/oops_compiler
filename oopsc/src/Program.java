import java.util.LinkedList;
/**
 * Die Klasse repräsentiert den Syntaxbaum des gesamten Programms.
 * Sie ist der Einstiegspunkt für die Kontextanalyse und die
 * Synthese.
 */
class Program {
    /** Die benutzerdefinierte Klasse. */
    // ClassDeclaration theClass;
    /** BEGIN Anfang (e): mehrere Klassen */
    LinkedList<ClassDeclaration> classes;
    /** END Anfang (e) */

    /**
     * Eine Ausdruck, der ein Objekt der Klasse Main erzeugt und dann darin die
     * Methode main aufruft. Entspricht NEW Main.main.
     */
    private Expression main = new AccessExpression(
            new NewExpression(new ResolvableIdentifier("Main", null), null),
            new VarOrCall(new ResolvableIdentifier("main", null)));

    /** BEGIN Aufgabe (e): mehrere Klassen */
    /**
     * Konstruktor.
     * @param theClass Die benutzerdefinierte Klasse.
     */
    Program(ClassDeclaration theClass) {
        // this.theClass = theClass;
        this.classes = new LinkedList<ClassDeclaration>();
        this.classes.add(theClass);
    }

    /**
     * Konstruktor.
     * @param classes Eine Liste der benutzerdefinierten Klassen.
     */
    Program(LinkedList<ClassDeclaration> classes) {
        this.classes = classes;
    }

    /** END Aufgabe (e) */

    /**
     * Die Methode führt die Kontextanalyse für das Programm durch.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis() throws CompileException {
        Declarations declarations = new Declarations();

        // Integer enthält ein Element
        // ClassDeclaration.intClass.objectSize = ClassDeclaration.HEADERSIZE + 1;

        /** BEGIN Aufgabe (d): Boolean */
        // Boolean enthält ein Element
        // ClassDeclaration.boolClass.objectSize = ClassDeclaration.HEADERSIZE + 1;
        /** END Aufgabe (d) */

        // Neuen Deklarationsraum schaffen
        declarations.enter();

        // Vordefinierte Klasse hinzufügen
        // declarations.add(ClassDeclaration.intClass);
        /** BEGIN Aufgabe (d): Boolean */
        // declarations.add(ClassDeclaration.boolClass);
        /** END Aufgabe (d) */
        /** BEGIN Aufgabe (e): mehrere Klassen */
        classes.add(ClassDeclaration.objectClass); //Aufgabe (i): Vererbung
        classes.add(ClassDeclaration.intClass);
        classes.add(ClassDeclaration.boolClass);

        //Vorgegebene Klassen initiieren
        /** BEGIN Aufgabe (i): Vererbung */
        // Die Klassen Integer und Boolean erben von Object
        ClassDeclaration.intClass.baseType = new ResolvableIdentifier("Object", null);
        ClassDeclaration.boolClass.baseType = new ResolvableIdentifier("Object", null);
        /** END Aufgabe (i) */

        /** BEGIN Aufgabe (j): Garbage Collector*/
        VarDeclaration nullAttr = new VarDeclaration(new Identifier("_Null", null), true);
        nullAttr.type = new ResolvableIdentifier("_Void", null);
        nullAttr.type.declaration = ClassDeclaration.voidType;
        ClassDeclaration.objectClass.attributes.add(nullAttr);
        /** END Aufgabe (j)*/

        VarDeclaration intValue = new VarDeclaration(new Identifier("_value", null), true);
        intValue.type = new ResolvableIdentifier("_Integer", null);
        intValue.type.declaration = ClassDeclaration.intType;
        ClassDeclaration.intClass.attributes.add(intValue);

        VarDeclaration boolValue = new VarDeclaration(new Identifier("_value", null), true);
        boolValue.type = new ResolvableIdentifier("_Boolean", null);
        boolValue.type.declaration = ClassDeclaration.boolType;
        ClassDeclaration.boolClass.attributes.add(boolValue);
        /** END Aufgabe (e) */

        // Benutzerdefinierte Klasse hinzufügen
        /** BEGIN Aufgabe (e): mehrere Klassen*/
        // declarations.add(theClass);
        for(ClassDeclaration c: classes){
            declarations.add(c);
        }

        // Kontextanalyse für die Methoden der Klasse durchführen
        // theClass.contextAnalysis(declarations);
        for(ClassDeclaration c: classes){
            c.contextAnalysis(declarations);
        }

        //Kontextanalyse fuer die Ruempfe der Klassen durchfuehren
        for(ClassDeclaration classdecl: classes){
            classdecl.contextAnalysisForBody((Declarations) classdecl.declarations.clone());
        }
        /** END Aufgabe (e) */

        // Abhängigkeiten für Startup-Code auflösen
        main = main.contextAnalysis(declarations);

        // Deklarationsraum verlassen
        declarations.leave();
    }

    /**
     * Die Methode gibt den Syntaxbaum des Programms aus.
     */
    void printTree() {
        TreeStream tree = new TreeStream(System.out, 4);
        // theClass.print(tree);
        /** BEGIN Aufgabe (e): mehrere Klassen */
        for(ClassDeclaration c: classes){
            c.print(tree);
        }
        /** END Aufgabe (e)*/
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    void optimizeTree() throws CompileException {
        for(ClassDeclaration c: classes){
            c.optimizeTree();
        }
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode generiert den Assembler-Code für das Programm. Sie geht
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code, int stackSize, int heapSize) {
        // Start-Code: Register initialisieren
        code.setNamespace("_init");
        code.println("; Erzeugt durch OOPS-0 compiler, Version 2012-03-15.");
        code.println("MRI R1, 1 ; R1 ist immer 1");
        // code.println("MRI R2, _stack ; R2 zeigt auf Stapel");
        /** BEGIN Aufgabe (j): Garbage Collector*/
        code.println("MRI R2, _stackR2 ; R2 zeigt auf R2-Stapel");
        code.println("MRI R4, _stackR4 ; R4 zeigt auf Objekt-Stapel");
        // code.println("MRI R4, _heap ; R4 zeigt auf die nächste freie Stelle auf dem Heap");
        /** END Aufgabe (j)*/

        /** BEGIN Aufgabe (h): Ausnahmebehandlung */
        // Zustand sichern
        code.println("MRR R5, R2 ; R2 zwischenspeichern ");
        code.println("ADD R2, R1 ; push");
        code.println("MRR R6, R2 ; Ausnahmerahmen Adresse zwischenspeichern ");
        code.println("MMR (R2), R6; Initialler Ausnahmerahmen zeigt auf sich selbst");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R5 ; R2 sichern aus dem Zwischenspeicher");
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R3 ; R3 sichern");
        /** BEGIN Aufgabe (j): Garbage Collector*/
        code.println("ADD R2, R1 ; push");
        code.println("MMR (R2), R4 ; R4 sichern");
        /** END Aufgabe (j)*/
        code.println("ADD R2, R1 ; push");
        code.println("MRI R5, _final_exception_handler ; finalle Ausnahmenbehandlung ");
        code.println("MMR (R2), R5 ");
        code.println("; _exception aktualisieren ");
        code.println("MRI R5, _exception");
        code.println("MMR (R5), R6 ");

        /** END Aufgabe (h) */
        // Ein Objekt der Klasse Main konstruieren und die Methode main aufrufen.
        main.generateCode(code);
        code.println("MRI R0, _end ; Programm beenden");

        // Generiere Code für benutzerdefinierte Klasse
        // theClass.generateCode(code);
        /** BEGIN Aufgabe (e): mehrere Klassen */
        for(ClassDeclaration c: classes){
            c.generateCode(code);
        }
        /** END Aufgabe (e)*/

        /** BEGIN Aufgabe (h): Ausnahmebehandlung */
        //TODO: benutze den allgemeinen Wiederherstellungscode
        code.println("_final_exception_handler: ; Finale Ausnahmebehandlung");
        code.println("MRM R7, (R2) ; Fehlercode holen");
        // nicht notwendig, da Stack wiederhergestellt wird
        // code.println("SUB R2, R1 ; pop");

        code.correctExceptionFrame();
/** BEGIN Bonus Aufgabe (4): Try&Catch-Erweiterung*/
	printException(code, "Div durch 0" , 0);
	printException(code, "NULL Zeigerzugriff" , 1);
/** END Bonus Aufgabe (4)*/
        code.println("MRI R5, 65 ; A");
        code.println("SYS 1, 5");
        code.println("MRI R5, 66 ; B");
        code.println("SYS 1, 5");
        code.println("MRI R5, 79 ; O");
        code.println("SYS 1, 5");
        code.println("MRI R5, 82 ; R");
        code.println("SYS 1, 5");
        code.println("MRI R5, 84 ; T");
        code.println("SYS 1, 5");
        code.println("MRI R5, 32 ; Leerzeichen ");
        code.println("SYS 1, 5");
        code.println("SYS 1, 7 ; Fehlercode ausgeben ");
        code.println("MRI R0, _end ; Programm beenden ");

        /** BEGIN Aufgabe (j): Garbage Collector*/
        // _lookup holt die Parameter direkt von den Registern (Optimierung)
        // @param Ruecksprungadresse
        // @param Objektgroesse
        // @return freie Adresse
        // TODO: ueberpruefe platz und raeume auf
        code.println("_lookup: ; legt die naechste freie Stelle vom Heap auf R6 ab");
        code.println("MRI R7, _free; hole die Adresse von _free");
        code.println("MRM R7, (R7); hole den Wert von _free");
        code.println("MRM R5, (R2); hole die Objektgroesse");
        code.println("SUB R2, R1; pop");
        //kein erneutes gc, wenn gc gerade aktiv ist
        code.println("MRI R6, _gc_active");
        code.println("MRM R6, (R6)");
        code.println("JPC R6, _lookup_ret ; ueberspringe den GC");
        //setze _gc_active auf 1
        code.println("MRI R6, _gc_active");
        code.println("MMR (R6), R1");
        //heapgrenzen vergleichen
        code.println("ADD R5, R7; naechste freie Stelle");
        code.println("MRI R6, "+heapSize+"; heapgroesse");
        code.println("SUB R5, R6");
        code.println("MRI R6, _ch");
        code.println("MRM R6, (R6)");
        code.println("SUB R5, R6");
        code.println("ISN R5, R5; wenn heap platz hat,");
        code.println("JPC R5, _lookup_ret; ueberspringe den GC");

        //_free auf den Anfang des naechsten Heaps setzen und heappointer
        //tauschen
        code.println("MRI R6, _nh");
        code.println("MRM R6, (R6)");
        code.println("MRI R5, _free");
        code.println("MMR (R5), R6");
        code.println("MRI R5, _ch");
        code.println("MRM R7, (R5)");
        code.println("MMR (R5), R6");
        code.println("MRI R5, _nh");
        code.println("MMR (R5), R7");
        
        //Objekte klonen
        //for (e: range(_stackR4, R4)){
        // if(e != NULL){
        //  e := call(e.vmt[0]);
        // }
        //}
        

        // gc laeuft nicht mehr
        // setze _gc_active auf 0
        code.println("MRI R6, _gc_active");
        code.println("MRI R5, 0");
        code.println("MMR (R6), R5");


        //TODO: erneut die Grenzen prueffen
        code.println("_lookup_ret:");
        code.println("MRM R5, (R2); hole Ruecksprungadresse");
        code.println("SUB R2, R1; pop");
        code.println("MMR (R2), R7; lege die freie Adresse ab");
        code.println("MRR R0, R5; springe zurueck");
        /** END Aufgabe (j)*/

        code.println("_exception: ; Verweis auf den aktuellen Ausnahmerahmen");
        code.println("DAT 1, 0");
        /** END Aufgabe (h) */

        /** BEGIN Aufgabe (i): Vererbung */
        // Generiere die Virtuellen Methodentabellen aller Klassen
        for(ClassDeclaration c: classes){
            c.generateVMTCode(code);
        }
        /** END Aufgabe (i)*/

        /** BEGIN Aufgabe (j): Garbage Collector*/
        code.println("_gc_active: ; flag, ob GC gerade arbeitet");
        code.println("DAT 1, 0");
        code.println("_free: ; naechste freie Stelle im Heap");
        // code.println("DAT 1, _heap");
        code.println("DAT 1, _heap1");
        code.println("_stackR2: ; Hier fängt der R2-Stapel an");
        code.println("DAT " + stackSize + ", 0");
        code.println("_stackR4: ; Hier fängt der R4-Stapel an");
        code.println("DAT " + stackSize + ", 0");
        code.println("_ch: ; Aktueller Heap");
        code.println("DAT 1, _heap1");
        code.println("_nh: ; Naechster Heap");
        code.println("DAT 1, _heap2");
        code.println("_heap1: ; Hier fängt der erste Heap an");
        code.println("DAT " + heapSize + ", 0");
        code.println("_heap2: ; Hier fängt der zweite Heap an");
        code.println("DAT " + heapSize + ", 0");
        /** END Aufgabe (j)*/
        // Speicher für Stapel und Heap reservieren
        // code.println("_stack: ; Hier fängt der Stapel an");
        // code.println("DAT " + stackSize + ", 0");
        // code.println("_heap: ; Hier fängt der Heap an");
        // code.println("DAT " + heapSize + ", 0");
        code.println("_end: ; Programmende");
    }

/** BEGIN Bonus Aufgabe (4): Try&Catch-Erweiterung*/
	void printException(CodeStream code, String exceptionText , int exceptionValue){
		String exceptionLabel = code.nextLabel();
		String text = exceptionText+"\n";
        	code.println("MRI R5, "+exceptionValue+" ; Wert der Fehlerauswertung");
        	code.println("SUB R5, R7 ; 0 wenn gleich");
        	code.println("JPC R5, " + exceptionLabel + " ; Ueberspringe Fehlerauswertung");
		for(int i=0;i<text.length(); i++){
			Character c = text.charAt(i);
        		code.println("MRI R5, "+(int)c+" ; "+((c!='\n')?c:"\\n"));
        		code.println("SYS 1, 5");
		}
	        code.println("MRI R0, _end ; Programm beenden ");
        	code.println(exceptionLabel + ":");
	}
/** END Bonus Aufgabe (4)*/
}

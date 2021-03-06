class VarDeclaration extends Declaration {
    /** Der Typ der Variablen bzw. des Attributs. */
    ResolvableIdentifier type;
    
    /** Wird hier ein Attribut deklariert (statt einer lokalen Variablen)? */
    boolean isAttribute;
    
    /**
     * Die Position der Variablen im Stapelrahmen bzw. des Attributs im Objekt.
     * Dies wird während der Kontextanalyse eingetragen.
     */ 
    int offset;
    
    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Variablen bzw. des Attributs.
     * @param isAttribute Wird hier ein Attribut deklariert (statt einer lokalen
     *         Variablen)?
     */
    VarDeclaration(Identifier name, boolean isAttribute) {
        super(name);
        this.isAttribute = isAttribute;
    }

    /**
     * Führt die Kontextanalyse für diese Variablen-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        declarations.resolveType(type);
    }

    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        /** BEGIN Bonus Aufgabe 5: Zugriffsschutz*/
        tree.format("%s ", identifier.accessType);
        /** END Bonus Aufgabe 5*/
        tree.println(identifier.name + 
                (type.declaration == null ? "" : " (" + offset + ")") +
                " : " + type.name);
    }
}

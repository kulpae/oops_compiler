/**
 * Die Klasse repräsentiert ein Symbol, das von der lexikalischen
 * Analyses erkannt wurde.
 */
class Symbol extends Position {
    /** Alle definierten Symbole. */
    enum Id {
        IDENT, NUMBER,
        BEGIN, END,
        CLASS, IS, METHOD,
        READ, WRITE,
        IF, THEN,
        WHILE, DO,
        COLON, SEMICOLON, COMMA, PERIOD,
        LPAREN, RPAREN,
        EQ, NEQ, GT, GTEQ, LT, LTEQ,
        PLUS, MINUS, TIMES, DIV, MOD,
        BECOMES, NEW,
        SELF,
        NULL,
        TRUE, FALSE, /** Aufgabe (a): TRUE und FALSE */
        ELSE, ELSEIF, /** Aufgabe (b): ELSEIF und ELSE */
        AND, OR, NOT, /** Aufgabe (c): AND, OR, NOT */
        ANDTHEN, ORELSE, /** Bonus Aufgabe 1: AND THEN, OR ELSE */
        RETURN, /** Aufgabe (g): Return */
        TRY, CATCH, THROW, /** Aufgabe (h): Ausnahmebehandlung */
        EXTENDS, BASE, /** Aufgabe (i): Vererbung */
        UNKNOWN, /** Bonus Aufgabe 3: Mehrere Fehlermeldungen */
        PRIVATE, PROTECTED, PUBLIC, /** Bonus Aufgabe 5: Zugriffsschutz*/
        EOF
    };
    
    /** Das Symbol. */
    Id id;
    
    /** Wenn das Symbol NUMBER ist, steht die gelesene Zahl in diesem Attribut. */
    int number;

    /** Wenn das Symbol IDENT ist, steht der gelesene Bezeichner in diesem Attribut. */
    String ident;
    
    /**
     * Konstruktor.
     * @param id Das erkannte Symbol.
     * @param position Die Quelltextstelle, an der das Symbol erkannt wurde.
     */
    Symbol(Id id, Position position) {
        super(position.line, position.column);
        this.id = id;
    }
    
    /**
     * Die Methode erzeugt aus diesem Objekt eine darstellbare Zeichenkette.
     * @return Die Zeichenkette.
     */
    public String toString() {
        switch (id) {
        case IDENT:
            return "IDENT: " + ident;
        case NUMBER:
            return "NUMBER: " + number;
        default:
            return id.toString();
        }
    }
}

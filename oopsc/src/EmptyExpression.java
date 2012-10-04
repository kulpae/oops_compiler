/** BEGIN Bonus Aufgabe 3: Mehrere Fehlermeldungen */
/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der in einem
 * Fehlerfall anstelle des tatsaechlichen Ausdrucks steht.
 */
class EmptyExpression extends Expression {
    
    /**
     * Konstruktor.
     * @param operand Der Ausdruck, der die Adresse berechnet.
     */
    EmptyExpression(Position position) {
        super(position);
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("? : " + type.identifier.name);
    }

    /**
     * Die Methode generiert keinen Assembler-Code für diesen Ausdruck. 
     * Denn im Fehlerfall wird kein Code erzeugt.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
    }

}
/** END Bonus Aufgabe 3*/

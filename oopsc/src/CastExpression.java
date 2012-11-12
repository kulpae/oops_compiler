/** BEGIN Aufgabe (j): Garbage Collector */
/**
 * Die Klasse repräsentiert einen Ausdruck, mit dem der Typ eines anderen
 * Ausdrucks umgewandelt werden kann.
 */
class CastExpression extends Expression{
    /** Der Typ dieses Ausdrucks nach der Umwandlung. */
    ResolvableIdentifier castType;

    /** Der umzuwandelnde Ausdruck. */
    Expression expression;

    /**
     * Konstruktor.
     * @param position Die Quelltextposition, an der dieser Ausdruck beginnt.
     */
    CastExpression(Expression expr, Position position) {
        super(position);
        this.expression = expr;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * Hier wird der Typ des Ausdrucks umgewandelt.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck oder ein neuer Ausdruck, falls ein Boxing,
     *         Unboxing oder eine Dereferenzierung in den Baum eingefügt
     *         wurden.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression contextAnalysis(Declarations declarations) throws CompileException {
        expression.contextAnalysis(declarations);
        lValue = expression.lValue;
        // lvalue kann nicht gecastet werden
        if(!lValue){
          declarations.resolveType(castType);
          type = (ClassDeclaration) castType.declaration;
        } else {
          type = expression.type;
        }
        return this;
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree){
      tree.print("CAST ("+castType.name+")");
      tree.println((type == null ? "" : " : " + (lValue ? "REF " : "") + type.identifier.name));
      tree.indent();
      expression.print(tree);
      tree.unindent();
    }

    /** Generiert den Code des Ausdrucks */
    void generateCode(CodeStream code){
      expression.generateCode(code);
    }
}
/** END Aufgabe (j) */

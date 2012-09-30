/**
 * Die Klasse repräsentiert einen Ausdruck mit einem binären Operator im Syntaxbaum.
 */
class BinaryExpression extends Expression {
    /** Der linke Operand. */
    Expression leftOperand;

    /** Der Operator. */
    Symbol.Id operator;

    /** Der rechte Operand. */
    Expression rightOperand;

    /**
     * Konstruktor.
     * @param operator Der Operator.
     * @param leftOperand Der linke Operand.
     * @param rightOperand Der rechte Operand.
     */
    BinaryExpression(Expression leftOperand, Symbol.Id operator, Expression rightOperand) {
        super(leftOperand.position);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    Expression contextAnalysis(Declarations declarations) throws CompileException {
        leftOperand = leftOperand.contextAnalysis(declarations);
        rightOperand = rightOperand.contextAnalysis(declarations);
        switch (operator) {
        case PLUS:
        case MINUS:
        case TIMES:
        case DIV:
        case MOD:
            leftOperand = leftOperand.unBox();
            rightOperand = rightOperand.unBox();
            leftOperand.type.check(ClassDeclaration.intType, leftOperand.position);
            rightOperand.type.check(ClassDeclaration.intType, rightOperand.position);
            type = ClassDeclaration.intType;
            break;
        /** BEGIN Aufgabe (c): AND, OR, NOT */
        case AND:
        case OR:
        case ANDTHEN:
        case ORELSE:
            leftOperand = leftOperand.unBox();
            rightOperand = rightOperand.unBox();
            leftOperand.type.check(ClassDeclaration.boolType, leftOperand.position);
            rightOperand.type.check(ClassDeclaration.boolType, rightOperand.position);
            type = ClassDeclaration.boolType;
            break;
        /** END Aufgabe (c) */
        case GT:
        case GTEQ:
        case LT:
        case LTEQ:
            leftOperand = leftOperand.unBox();
            rightOperand = rightOperand.unBox();
            leftOperand.type.check(ClassDeclaration.intType, leftOperand.position);
            rightOperand.type.check(ClassDeclaration.intType, rightOperand.position);
            type = ClassDeclaration.boolType;
            break;
        case EQ:
        case NEQ:
            // Wenn einer der beiden Operanden NULL ist, muss der andere
            // ein Objekt sein (oder auch NULL)
            if (leftOperand.type == ClassDeclaration.nullType) {
                rightOperand = rightOperand.box(declarations);
            } else if (rightOperand.type == ClassDeclaration.nullType) {
                leftOperand = leftOperand.box(declarations);
            } else {
                // ansonsten wird versucht, die beiden Operanden in
                // Basisdatentypen zu wandeln
                leftOperand = leftOperand.unBox();
                rightOperand = rightOperand.unBox();
            }

            // Nun muss der Typ mindestens eines Operanden gleich oder eine
            // Ableitung des Typs des anderen Operanden sein.
            if (!leftOperand.type.isA(rightOperand.type) &&
                    !rightOperand.type.isA(leftOperand.type)) {
                ClassDeclaration.typeError(leftOperand.type, rightOperand.position);
            }
            type = ClassDeclaration.boolType;
            break;
        default:
            assert false;
        }
        return this;
    }

    /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
    void optimizeTree(){
      leftOperand.optimizeTree();
      rightOperand.optimizeTree();
      //TODO do it
    }
    /** END Bonus Aufgabe 2*/

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println(operator + (type == null ? "" : " : " + type.identifier.name));
        tree.indent();
        leftOperand.print(tree);
        rightOperand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        /**BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE*/
        String skipLabel = null;
        if(operator == Symbol.Id.ORELSE || operator == Symbol.Id.ANDTHEN){
            skipLabel = code.nextLabel();
        }
        /**END Bonus Aufgabe 1*/
        leftOperand.generateCode(code);
        /**BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE*/
        code.println("MRM R5, (R2)");
        switch(operator){
          case ORELSE:
            code.println("JPC R5, "+skipLabel + " ; Sprung zum Ende der Auswertung");
            break;
          case ANDTHEN:
            code.println("ISZ R5, R5");
            code.println("JPC R5, "+skipLabel + " ; Sprung zum Ende der Auswertung");
            break;
        }
        /**END Bonus Aufgabe 1*/
        rightOperand.generateCode(code);
        code.println("; " + operator);
        code.println("MRM R5, (R2)");
        code.println("SUB R2, R1");
        code.println("MRM R6, (R2)");
        switch (operator) {
        case PLUS:
            code.println("ADD R6, R5");
            break;
        case MINUS:
            code.println("SUB R6, R5");
            break;
        case TIMES:
            code.println("MUL R6, R5");
            break;
        case DIV:
            code.println("DIV R6, R5");
            break;
        case MOD:
            code.println("MOD R6, R5");
            break;
        case GT:
            code.println("SUB R6, R5");
            code.println("ISP R6, R6");
            break;
        case GTEQ:
            code.println("SUB R6, R5");
            code.println("ISN R6, R6");
            code.println("XOR R6, R1");
            break;
        case LT:
            code.println("SUB R6, R5");
            code.println("ISN R6, R6");
            break;
        case LTEQ:
            code.println("SUB R6, R5");
            code.println("ISP R6, R6");
            code.println("XOR R6, R1");
            break;
        case EQ:
            code.println("SUB R6, R5");
            code.println("ISZ R6, R6");
            break;
        case NEQ:
            code.println("SUB R6, R5");
            code.println("ISZ R6, R6");
            code.println("XOR R6, R1");
            break;
        /** BEGIN Aufgabe (c): AND, OR, NOT */
        case AND:
        case ANDTHEN: // Bonus Aufgabe 1: AND THEN und OR ELSE
            code.println("AND R6, R5");
            break;
        case OR:
        case ORELSE: // Bonus Aufgabe 1: AND THEN und OR ELSE
            code.println("OR R6, R5");
            break;
        /** END Aufgabe (c)*/
        default:
            assert false;
        }
        /** BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE */
        code.println("MMR (R2), R6");
        if(skipLabel != null){
            code.println(""+skipLabel+":");
        }
        /** END Bonus Aufgabe 1*/
    }
}

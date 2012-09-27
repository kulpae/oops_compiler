import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Die Klasse realisiert die syntaktische Analyse für die folgende Grammatik.
 * Terminale stehen dabei in Hochkommata oder sind groß geschrieben:
 * <pre>
 * program      ::= {classdecl}
 *
 * classdecl    ::= CLASS identifier IS
 *                  { memberdecl }
 *                  END CLASS
 *
 * memberdecl   ::= vardecl ';'
 *                | METHOD identifier ['(' vardecl { ';' vardecl } ')']
 *                  [':' identifier ] IS methodbody
 *
 * vardecl      ::= identifier { ',' identifier } ':' identifier
 *
 * methodbody   ::= { vardecl ';' }
 *                  BEGIN statements
 *                  END METHOD
 *
 * statements   ::= { statement }
 *
 * statement    ::= READ memberaccess ';'
 *                | WRITE expression ';'
 *                | IF logicalOR
 *                  THEN statements
 *                  { ELSEIF logicalOR
 *                  THEN statements }
 *                  [ ELSE
 *                  statements ]
 *                  END IF
 *                | WHILE logicalOR
 *                  DO statements
 *                  END WHILE
 *                | memberaccess [ ':=' expression ] ';'
 *                | RETURN [ logicalOR ] ';'
 *
 * logicalOR    ::= logicalAND { 'OR' logicalAND }
 * logicalAND   ::= relation { 'AND' relation }
 * relation     ::= expression [ ( '=' | '#' | '<' | '>' | '<=' | '>=' ) expression ]
 *
 * expression   ::= term { ( '+' | '-' ) term }
 *
 * term         ::= factor { ( '*' | '/' | MOD ) factor }
 *
 * factor       ::= '-' factor
 *                | 'NOT' factor
 *                | memberaccess
 *
 * memberaccess ::= literal { '.' varorcall }
 *
 * literal    ::= number
 *                | character
 *                | NULL
 *                | TRUE
 *                | FALSE
 *                | SELF
 *                | NEW identifier
 *                | '(' logicalOR ')'
 *                | varorcall
 *
 * varorcall    ::= identifier ['(' logicalOR {',' logicalOR } ')']
 * </pre>
 * Daraus wird der Syntaxbaum aufgebaut, dessen Wurzel die Klasse
 * {@link Program Program} ist.
 */
class SyntaxAnalysis extends LexicalAnalysis {
    /**
     * Die Methode erzeugt einen "Unerwartetes Symbol"-Fehler.
     * @throws CompileException Die entsprechende Fehlermeldung.
     */
    private void unexpectedSymbol() throws CompileException {
        throw new CompileException("Unerwartetes Symbol " + symbol.id.toString(), symbol);
    }

    /**
     * Die Methode überprüft, ob das aktuelle Symbol das erwartete ist. Ist dem so,
     * wird das nächste Symbol gelesen, ansonsten wird eine Fehlermeldung erzeugt.
     * @param id Das erwartete Symbol.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void expectSymbol(Symbol.Id id) throws CompileException, IOException {
        if (id != symbol.id) {
            unexpectedSymbol();
        }
        nextSymbol();
    }

    /**
     * Die Methode überprüft, ob das aktuelle Symbol ein Bezeichner ist. Ist dem so,
     * wird er zurückgeliefert, ansonsten wird eine Fehlermeldung erzeugt.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Identifier expectIdent() throws CompileException, IOException {
        if (symbol.id != Symbol.Id.IDENT) {
            unexpectedSymbol();
        }
        Identifier i = new Identifier(symbol.ident, new Position(symbol.line, symbol.column));
        nextSymbol();
        return i;
    }

    /**
     * Die Methode überprüft, ob das aktuelle Symbol ein Bezeichner ist. Ist dem so,
     * wird er in Form eines Bezeichners mit noch aufzulösender Vereinbarung
     * zurückgeliefert, ansonsten wird eine Fehlermeldung erzeugt.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private ResolvableIdentifier expectResolvableIdent() throws CompileException, IOException {
        if (symbol.id != Symbol.Id.IDENT) {
            unexpectedSymbol();
        }
        ResolvableIdentifier r = new ResolvableIdentifier(symbol.ident, new Position(symbol.line, symbol.column));
        nextSymbol();
        return r;
    }

    /**
     * Die Methode parsiert eine Klassendeklaration entsprechend der oben angegebenen
     * Syntax und liefert diese zurück.
     * @return Die Klassendeklaration.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private ClassDeclaration classdecl() throws CompileException, IOException {
        expectSymbol(Symbol.Id.CLASS);
        ClassDeclaration c = new ClassDeclaration(expectIdent());
        expectSymbol(Symbol.Id.IS);
        while (symbol.id != Symbol.Id.END) {
            memberdecl(c.attributes, c.methods);
        }
        nextSymbol();
        expectSymbol(Symbol.Id.CLASS);
        return c;
    }

    /**
     * Die Methode parsiert die Deklaration eines Attributs bzw. einer Methode
     * entsprechend der oben angegebenen Syntax und hängt sie an eine von
     * zwei Listen an.
     * @param attributes Die Liste der Attributdeklarationen der aktuellen Klasse.
     * @param methods Die Liste der Methodendeklarationen der aktuellen Klasse.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void memberdecl(LinkedList<VarDeclaration> attributes,
            LinkedList<MethodDeclaration> methods)
            throws CompileException, IOException {
        if (symbol.id == Symbol.Id.METHOD) {
            nextSymbol();
            MethodDeclaration m = new MethodDeclaration(expectIdent());
            /** BEGIN Aufgabe (f): Methoden Parameter */
            if(symbol.id == Symbol.Id.LPAREN){
              do {
                nextSymbol();
                vardecl(m.params, false);
              } while(symbol.id == Symbol.Id.SEMICOLON);
              expectSymbol(Symbol.Id.RPAREN);
            }
            /** END Aufgabe (f) */
            /** BEGIN Aufgabe (g): Return */
            if(symbol.id == Symbol.Id.COLON){
              nextSymbol();
              m.returnType = expectResolvableIdent();
            }
            /** END Aufgabe (g) */
            expectSymbol(Symbol.Id.IS);
            methodbody(m.vars, m.statements);
            methods.add(m);
        } else {
            vardecl(attributes, true);
            expectSymbol(Symbol.Id.SEMICOLON);
        }
    }

    /**
     * Die Methode parsiert die Deklaration eines Attributs bzw. einer Variablen
     * entsprechend der oben angegebenen Syntax und hängt sie an eine Liste an.
     * @param vars Die Liste der Attributdeklarationen der aktuellen Klasse oder
     *         der Variablen der aktuellen Methode.
     * @param isAttribute Ist die Variable ein Attribut?.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void vardecl(LinkedList<VarDeclaration> vars, boolean isAttribute) throws CompileException, IOException {
        LinkedList<VarDeclaration> temp = new LinkedList<VarDeclaration>();
        temp.add(new VarDeclaration(expectIdent(), isAttribute));
        while (symbol.id == Symbol.Id.COMMA) {
            nextSymbol();
            temp.add(new VarDeclaration(expectIdent(), isAttribute));
        }
        expectSymbol(Symbol.Id.COLON);
        ResolvableIdentifier ident = expectResolvableIdent();
        for (VarDeclaration v : temp) {
            v.type = ident;
            vars.add(v);
        }
    }

    /**
     * Die Methode parsiert die Deklaration eines Methodenrumpfes entsprechend der
     * oben angegebenen Syntax. Lokale Variablendeklarationen und Anweisungen werden
     * an die entsprechenden Listen angehängt.
     * @param vars Die Liste der lokalen Variablendeklarationen der aktuellen Methode.
     * @param statements Die Liste der Anweisungen der aktuellen Methode.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void methodbody(LinkedList<VarDeclaration> vars, LinkedList<Statement> statements) throws CompileException, IOException {
        while (symbol.id != Symbol.Id.BEGIN) {
            vardecl(vars, false);
            expectSymbol(Symbol.Id.SEMICOLON);
        }
        nextSymbol();
        statements(statements);
        expectSymbol(Symbol.Id.END);
        expectSymbol(Symbol.Id.METHOD);
    }

    /**
     * Die Methode parsiert eine Folge von Anweisungen entsprechend der
     * oben angegebenen Syntax und hängt sie an eine Liste an.
     * @param statements Die Liste der Anweisungen.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void statements(LinkedList<Statement> statements) throws CompileException, IOException {
        // Veraendert fuer Aufgabe (b): ELSEIF und ELSE
        while (symbol.id != Symbol.Id.END && symbol.id != Symbol.Id.ELSE && symbol.id != Symbol.Id.ELSEIF) {
            statement(statements);
        }
    }

    /**
     * Die Methode parsiert eine Anweisung entsprechend der oben angegebenen
     * Syntax und hängt sie an eine Liste an.
     * @param statements Die Liste der Anweisungen.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void statement(LinkedList<Statement> statements) throws CompileException, IOException {
        switch (symbol.id) {
        case READ:
            nextSymbol();
            statements.add(new ReadStatement(memberAccess()));
            expectSymbol(Symbol.Id.SEMICOLON);
            break;
        case WRITE:
            nextSymbol();
            statements.add(new WriteStatement(expression()));
            expectSymbol(Symbol.Id.SEMICOLON);
            break;
        case IF:
            nextSymbol();
            IfStatement s = new IfStatement(logicalOR()); // Erweitert bei Aufgabe (c): AND, OR, NOT
            statements.add(s);
            expectSymbol(Symbol.Id.THEN);
            statements(s.thenStatements);
            /** BEGIN Aufgabe(b): ELSEIF und ELSE */
            if(symbol.id == Symbol.Id.ELSEIF){
              symbol.id = Symbol.Id.IF;
              statement(s.elseStatements);
              break;
            } else if(symbol.id == Symbol.Id.ELSE){
              nextSymbol();
              statements(s.elseStatements);
            }
            /** END Aufgabe (b) */
            expectSymbol(Symbol.Id.END);
            expectSymbol(Symbol.Id.IF);
            break;
        case WHILE:
            nextSymbol();
            WhileStatement w = new WhileStatement(logicalOR()); //Erweitert fuer Aufgabe (c): AND, OR, NOT
            statements.add(w);
            expectSymbol(Symbol.Id.DO);
            statements(w.statements);
            expectSymbol(Symbol.Id.END);
            expectSymbol(Symbol.Id.WHILE);
            break;
        case RETURN:
            nextSymbol();
            ReturnStatement r =  new ReturnStatement(new Position(symbol.line, symbol.column));
            statements.add(r);
            if(symbol.id != Symbol.Id.SEMICOLON){
              r.value = logicalOR();
            }
            expectSymbol(Symbol.Id.SEMICOLON);
            break;
        default:
            Expression e = memberAccess();
            if (symbol.id == Symbol.Id.BECOMES) {
                nextSymbol();
                // statements.add(new Assignment(e, expression()));
                statements.add(new Assignment(e, logicalOR())); /** Erweitern fuer Aufgabe (c): AND, OR, NOT */
            } else {
                statements.add(new CallStatement(e));
            }
            expectSymbol(Symbol.Id.SEMICOLON);
        }
    }

    /** BEGIN Aufgabe (c): AND, OR, NOT */

    /**
     * Die Methode parsiert eine logische OR Relation entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression logicalOR() throws CompileException, IOException {
        Expression e = logicalAND();
        while (symbol.id == Symbol.Id.OR) {
          Symbol.Id operator = symbol.id;
          nextSymbol();
          /** BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE */
          if(symbol.id == Symbol.Id.ELSE){
            operator = Symbol.Id.ORELSE;
            nextSymbol();
          }
          /** END Bonus Aufgabe 1*/
          e = new BinaryExpression(e, operator, logicalAND());
        }
        return e;
    }

    /**
     * Die Methode parsiert eine logische UND Relation entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression logicalAND() throws CompileException, IOException {
        Expression e = relation();
        while (symbol.id == Symbol.Id.AND) {
          Symbol.Id operator = symbol.id;
          nextSymbol();
          /** BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE */
          if(symbol.id == Symbol.Id.THEN){
            operator = Symbol.Id.ANDTHEN;
            nextSymbol();
          }
          /** END Bonus Aufgabe 1*/
          e = new BinaryExpression(e, operator, relation());
        }
        return e;
    }

    /** END Aufgabe (c) */

    /**
     * Die Methode parsiert eine Relation entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression relation() throws CompileException, IOException {
        Expression e = expression();
        switch (symbol.id) {
        case EQ:
        case NEQ:
        case GT:
        case GTEQ:
        case LT:
        case LTEQ:
            Symbol.Id operator = symbol.id;
            nextSymbol();
            e = new BinaryExpression(e, operator, expression());
        }
        return e;
    }

    /**
     * Die Methode parsiert einen Ausdruck entsprechend der oben angegebenen
     * Syntax und liefert ihn zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression expression() throws CompileException, IOException {
        Expression e = term();
        while (symbol.id == Symbol.Id.PLUS || symbol.id == Symbol.Id.MINUS) {
            Symbol.Id operator = symbol.id;
            nextSymbol();
            e = new BinaryExpression(e, operator, term());
        }
        return e;
    }

    /**
     * Die Methode parsiert einen Term entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression term() throws CompileException, IOException {
        Expression e = factor();
        while (symbol.id == Symbol.Id.TIMES || symbol.id == Symbol.Id.DIV ||
                symbol.id == Symbol.Id.MOD) {
            Symbol.Id operator = symbol.id;
            nextSymbol();
            e = new BinaryExpression(e, operator, factor());
        }
        return e;
    }

    /**
     * Die Methode parsiert einen Faktor entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression factor() throws CompileException, IOException {
        switch (symbol.id) {
        case NOT: /** Aufgabe (c): AND, OR, NOT */
        case MINUS:
            Symbol.Id operator = symbol.id;
            Position position = new Position(symbol.line, symbol.column);
            nextSymbol();
            return new UnaryExpression(operator, factor(), position);
        default:
            return memberAccess();
        }
    }

    /**
     * Die Methode parsiert den Zugriff auf ein Objektattribut bzw. eine
     * Objektmethode entsprechend der oben angegebenen Syntax und liefert
     * den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression memberAccess() throws CompileException, IOException {
        Expression e = literal();
        while (symbol.id == Symbol.Id.PERIOD) {
            nextSymbol();
            e = new AccessExpression(e, varorcall());
        }
        return e;
    }

    /**
     * Die Methode parsiert ein Literal, die Erzeugung eines Objekts, einen
     * geklammerten Ausdruck oder einen einzelnen Zugriff auf eine Variable,
     * ein Attribut oder eine Methode entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression literal() throws CompileException, IOException {
        Expression e = null;
        switch (symbol.id) {
        case NUMBER:
            e = new LiteralExpression(symbol.number, ClassDeclaration.intType, new Position(symbol.line, symbol.column));
            nextSymbol();
            break;
        /** BEGIN Aufgabe (a): TRUE und FALSE */
        case TRUE:
            e = new LiteralExpression(1, ClassDeclaration.boolType, new Position(symbol.line, symbol.column));
            nextSymbol();
            break;
        case FALSE:
            e = new LiteralExpression(0, ClassDeclaration.boolType, new Position(symbol.line, symbol.column));
            nextSymbol();
            break;
        /** END Aufgabe (a)*/
        case NULL:
            e = new LiteralExpression(0, ClassDeclaration.nullType, new Position(symbol.line, symbol.column));
            nextSymbol();
            break;
        case SELF:
            e = new VarOrCall(new ResolvableIdentifier("_self", new Position(symbol.line, symbol.column)));
            nextSymbol();
            break;
        case NEW:
            Position position = new Position(symbol.line, symbol.column);
            nextSymbol();
            e = new NewExpression(expectResolvableIdent(), position);
            break;
        case LPAREN:
            nextSymbol();
            // e = expression();
            e = logicalOR(); /** Aufgabe (c): AND, OR, NOT */
            expectSymbol(Symbol.Id.RPAREN);
            break;
        case IDENT:
            e = varorcall();
            break;
        default:
            unexpectedSymbol();
        }
        return e;
    }

    /** BEGIN Aufgabe (f): Methoden Parameter */
    /** Diese Methode parsiert einen Methodenaufruf mit Parametern
     * oder ein Zugriff auf einen Attribut oder eine Variable.
     * @return der parsierte Ausdruck
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private VarOrCall varorcall() throws CompileException, IOException{
      VarOrCall e = new VarOrCall(expectResolvableIdent());
      //optional, Klammern mit Parameterwerten
      if(symbol.id == Symbol.Id.LPAREN){
        do {
          nextSymbol();
          e.params.add(logicalOR());
        } while(symbol.id == Symbol.Id.COMMA);

        //muss geschlossen werden
        expectSymbol(Symbol.Id.RPAREN);
      }
      return e;
    }
    /** END Aufgabe (f) */

    /**
     * Konstruktor.
     * @param fileName Der Name des Quelltexts.
     * @param printSymbols Die lexikalische Analyse gibt die erkannten
     *         Symbole auf der Konsole aus.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws FileNotFoundException Der Quelltext wurde nicht gefunden.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    SyntaxAnalysis(String fileName, boolean printSymbols)
            throws CompileException, FileNotFoundException, IOException {
        super(fileName, printSymbols);
        ResolvableIdentifier.init();
    }

    /**
     * Die Methode parsiert den Quelltext und liefert die Wurzel des
     * Syntaxbaums zurück.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    Program parse() throws CompileException, IOException {
        nextSymbol();
        /**BEGIN Aufgabe (e): mehrere Klassen */
        LinkedList<ClassDeclaration> classes = new LinkedList<ClassDeclaration>();
        while(symbol.id == Symbol.Id.CLASS){
          classes.add(classdecl());
        }
        Program p = new Program(classes);
        /** END Aufgabe (e) */
        expectSymbol(Symbol.Id.EOF);
        return p;
    }
}

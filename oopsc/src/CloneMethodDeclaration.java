/** BEGIN Aufgabe (j): Garbage Collector */
import java.util.LinkedList;

/**
 * Die Klasse repräsentiert die KlonMethode einer Klasse im Syntaxbaum.
 */
class CloneMethodDeclaration extends MethodDeclaration {

    /** Atrribute die geklont werden sollen */
    LinkedList<VarDeclaration> cloneAttributes;

    Identifier classIdent;

    /**
     * Konstruktor.
     * @param klass Der Identifier der zu klonenden Klasse.
     */
    CloneMethodDeclaration(Identifier classIdent) {
        super(new Identifier("clone", null));
        this.classIdent = classIdent;
        cloneAttributes = new LinkedList<VarDeclaration>();
    }

    void addAttribute(VarDeclaration attr){
      cloneAttributes.add(attr);
    }

    /**
     * Führt die Kontextanalyse für diese Methoden-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
      //macht es stabil bei mehrfachem Aufruf
      statements.clear();
      vars.clear();
      params.clear();
      VarDeclaration objVar = new VarDeclaration(new Identifier("c", null), false);
      objVar.type = new ResolvableIdentifier(classIdent.name, null);
      vars.add(objVar);
      // if _newAddr == NULL:
      IfStatement ifs = new IfStatement(new BinaryExpression(
            new VarOrCall(new ResolvableIdentifier("_newAddr", null)),
            Symbol.Id.EQ,
            new LiteralExpression(0, ClassDeclaration.nullType, null)
            ));
      statements.add(ifs);
      // c = NEW classIdent;
      ifs.thenStatements.add(new Assignment(
          new VarOrCall(new ResolvableIdentifier("c", null)),
          new NewExpression(new ResolvableIdentifier(classIdent.name, null), null)
        )
      );
      // foreach attrib as a:
      //   c.a = SELF.a;
      for(VarDeclaration a: cloneAttributes){
        String attrname = a.identifier.name;
        ifs.thenStatements.add(new Assignment(
            new AccessExpression(
              new VarOrCall(new ResolvableIdentifier("c", null)),
              new VarOrCall(new ResolvableIdentifier(attrname, null))),
            new AccessExpression(
              new VarOrCall(new ResolvableIdentifier("_self", null)),
              new VarOrCall(new ResolvableIdentifier(attrname, null)))
          )
        );
      }
      // _newAddr = c;
      ifs.thenStatements.add(new Assignment(
        new VarOrCall(new ResolvableIdentifier("_newAddr", null)),
        new VarOrCall(new ResolvableIdentifier("c", null))
      ));
      // END IF
      // RETURN _newAddr;
      ReturnStatement retStat = new ReturnStatement(null);
      retStat.value = new VarOrCall(new ResolvableIdentifier("_newAddr", null));
      statements.add(retStat);
      super.contextAnalysis(declarations);
    }


}

/** END Aufgabe (j) */

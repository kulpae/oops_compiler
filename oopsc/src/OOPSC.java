/**
 * Diese Klasse stellt die Hauptmethode des Übersetzers für OOPS 
 * dar. Sie wertet die Kommandozeilen-Optionen aus und bietet 
 * eine Hilfe an, falls diese falsch sind.
 */
class OOPSC {

    /** BEGIN Bonus Aufgabe 3: Mehrere Fehlermeldungen*/
    /** Dieser Flag merkt, ob beim Uebersetzen Fehler entdeckt wurden und die
     * Kompilierung ausgelassen werden soll*/
    static boolean buggyCode;
    /** END Bonus Aufgabe 3*/
    /**
     * Die Hauptmethode des Übersetzers. 
     * Sie wertet die Kommandozeilen-Optionen aus und bietet eine Hilfe an, falls diese falsch sind.
     * Sind sie gültig, wird zuerst die Syntaxanalyse durchgeführt. Diese erzeugt den
     * Syntaxbaum des Programms, in dem dann die Kontextanalyse durchgeführt wird. Zum
     * Schluss wird dann der Code generiert.
     * @param args Die Kommandozeilenargumente. Diese sind im Quelltext der Methode 
     * {@link #usage usage} nachzulesen.
     */
    public static void main(String[] args) throws Exception {
        String inFile = null;
        String outFile = null;
        boolean showContext = false;
        boolean showSymbols = false;
        boolean showIdentifiers = false;
	/**Bonus Aufgabe 2: Konstante Ausdrücke */
        boolean showOptimizedTree = false;
        boolean showSyntax = false;
        int heapSize = 100;
        int stackSize = 100;

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-c")) {
                showContext = true;
	/**BEGIN Bonus Aufgabe 2: Konstante Ausdrücke */
	    }else if (arg.equals("-o")) {
                showOptimizedTree = true;
	/**END Bonus Aufgabe 2*/
            } else if (arg.equals("-h")) {
                usage();
                return;
            } else if (arg.equals("-hs")) {
                if (++i < args.length) {
                    heapSize = Integer.parseInt(args[i]);
                } else {
                    System.out.println("Fehlendes Argument fuer " + arg);
                    usage();
                }
            } else if (arg.equals("-i")) {
                showIdentifiers = true;
            } else if (arg.equals("-l")) {
                showSymbols = true;
            } else if (arg.equals("-s")) {
                showSyntax = true;
            } else if (arg.equals("-ss")) {
                if (++i < args.length) {
                    stackSize = Integer.parseInt(args[i]);
                } else {
                    System.out.println("Fehlendes Argument fuer " + arg);
                    usage();
                }
            } else if (arg.length() > 0 && arg.charAt(0) == '-') {
                System.out.println("Unbekannte Option " + arg);
                usage();
                return;
            } else if (outFile != null) {
                System.out.println("Nur zwei Dateinamen erlaubt");
                usage();
                return;
            } else if (inFile != null) {
                outFile = arg;
            } else {
                inFile = arg;
            }
        }

        /** BEGIN Bonus Aufgabe 3: Mehrere Fehlermeldungen*/
        buggyCode = false;
        /** END Bonus Aufgabe 3*/
            
        if (inFile == null) {
            System.out.println("Keine Quelldatei angegeben");
            usage();
            return;
        }
        
        try {
            Program p = new SyntaxAnalysis(inFile, showSymbols).parse();
            if (showSyntax) {
                p.printTree();
            }
            p.contextAnalysis();
            if (showIdentifiers) {
                ResolvableIdentifier.print();
            }
            if (showContext) {
                p.printTree();
            }

            /** BEGIN Bonus Aufgabe 3: Mehrere Fehlermeldungen*/
            if(buggyCode){
              throw new CompileException("Fehler gefunden. Kompilierung abgebrochen.", null);
            }
            /** END Bonus Aufgabe 3*/

            /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
            // optimiert den Syntaxbaum
            p.optimizeTree();

            if (showOptimizedTree) {
                p.printTree();
            }
            /** END Bonus Aufgabe 2*/
            
            CodeStream stream = outFile == null ? new CodeStream() : new CodeStream(outFile);
            p.generateCode(stream, stackSize, heapSize);
            if (outFile != null) {
                stream.close();
            }
        } catch (CompileException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Die Methode gibt eine Hilfe auf der Konsole aus.
     */
    private static void usage() {
        System.out.println("java -jar OOPSC.jar [-c] [-h] [-hs <n>] [-i] [-l] [-s] [-ss <n>] <quelldatei> [<ausgabedatei>]");
        System.out.println("    -c       Zeige das Ergebnis der Kontextanalyse");
        System.out.println("    -h       Zeige diese Hilfe");
        System.out.println("    -hs <n>  Reserviere <n> Worte fuer den Heap (Standard ist 100)");
        System.out.println("    -i       Zeige die Zuordnung der Bezeichner");
        System.out.println("    -l       Zeige das Ergebnis der lexikalischen Analyse");
        System.out.println("    -s       Zeige das Ergebnis der syntaktischen Analyse");
        System.out.println("    -ss <n>  Reserviere <n> Worte fuer den Stapel (Standard ist 100)");
    }
}

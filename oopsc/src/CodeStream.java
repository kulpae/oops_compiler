import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Die Klasse repräsentiert einen Datenstrom, in der Assemblercode des
 * auszugebenen Programms geschrieben wird. Da die Klasse von
 * {@link java.io.PrintStream PrintStream} erbt, können alle Methoden
 * verwendet werden, mit denen man auch auf die Konsole schreiben kann.
 * Zusätzlich kann die Klasse eindeutige Marken für den Assemblerquelltext
 * generieren.
 */
class CodeStream extends PrintStream {
    /** Das Attribut enthält den gerade gültigen Namensraum (Klasse + Methode). */
    private String namespace;

    /** Das Attribut ist ein Zähler zur Generierung eindeutiger Bezeichner. */
    private int counter;

    /**
     * Konstruktor zur Ausgabe auf die Konsole.
     */
    CodeStream() {
        super(System.out);
    }

    /**
     * Konstruktor zur Ausgabe in eine Datei.
     * @param fileName Der Name der Ausgabedatei.
     * @throws FileNotFoundException Die Datei kann nicht erzeugt werden.
     */
    CodeStream(String fileName) throws FileNotFoundException {
        super(new File(fileName));
    }

    /**
     * Die Methode setzt den aktuell gültigen Namensraum.
     * Dieser wird verwendet, um eindeutige Marken zu generieren.
     * Derselbe Namensraum darf nur einmal während der Code-Erzeugung
     * gesetzt werden.
     * @param namespace Den ab jetzt gültigen Namensraum (Klasse + Methode).
     */
    void setNamespace(String namespace) {
        this.namespace = namespace;
        counter = 1;
    }

    /**
     * Die Methode erzeugt eine eindeutige Marke im aktuellen Namensraum.
     * @return Die Marke.
     */
    String nextLabel() {
        return namespace + "_" + counter++;
    }

    /** BEGIN Aufgabe (g): Return*/
    /**
     * Label zum Verlassen der aktuellen Methode.
     * Nur gueltig in einer Methode, die dieses Label angelegt hat.
     * @return Name des Labels, das zum Ausgang der Methode fuehrt.
     */
    String returnLabel(){
      return namespace + "_return";
    }
    /** END Aufgabe (g)*/
    /** BEGIN Aufgabe (h): Ausnahmebehandlung */
    /**
     * Korrigiere den Ausnahmerahmen.
     * Verwendet Register R5 und R6.
     * Verkleinert den Stack.
     */
    void correctExceptionFrame(){
        println("; Stack korrigieren");
        println("MRI R5, _exception ; _exception Adresse holen");
        println("MRM R6, (R5) ; _exception holen");

        println("ADD R6, R1 ; gesicherten R2 holen");
        println("MRM R2, (R6) ; R2 wiederherstellen");
        println("ADD R6, R1 ; gesicherten R3 holen");
        println("MRM R3, (R6) ; R3 wiederherstellen");
        /**BEGIN Aufgabe (j): Garbage Collector*/
        println("ADD R6, R1 ; gesicherten R4 holen");
        println("MRM R3, (R6) ; R4 wiederherstellen");
        /** END Aufgabe (j)*/

        println("; _exception auf den naechsten Ausnahmerahmen setzen");
        println("MRM R6, (R5) ; _exception holen");
        println("MRM R6, (R6) ; naechsten Ausnahmerahmen holen");
        println("MMR (R5), R6; _exception zeigt auf den naechsten Ausnahmerahmen ");
    }
    /** END Aufgabe (h)*/
}

/**
 * Die Klasse repräsentiert einen Bezeichner im Quelltext.
 */
class Identifier {
    /** Der Name des Bezeichners. */
    String name;
    
    /** Die Quelltextstelle, an der der Bezeichner gelesen wurde. */
    Position position;
    
    /**
     * Konstruktor.
     * @param name Der Name des Bezeichners.
     * @param position Die Quelltextstelle, an der der Bezeichner gelesen wurde.
     */
    Identifier(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /** BEGIN Aufgabe (i): Vererbung */
    /**
     * Vergleicht Identifier
     * @param o zu vergleichende Identifier
     * @return true, wenn beide gleich sind
     */
    public boolean equals(Object o){
        return (o instanceof Identifier) && ((Identifier)o).name.equals(name);
    }
    /** END Aufgabe (i) */
}

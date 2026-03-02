package metier;

/**
 * Représente un arc dirigé depuis un noeud vers une destination, avec un poids.
 */
public class Arc 
{

    private final Noeud destination;
    private final int poids;

    public Arc(Noeud destination, int poids) 
    {
        this.destination = destination;
        this.poids = poids;
    }

    public Noeud getDestination() 
    {
        return this.destination;
    }

    public int getPoids() 
    {
        return this.poids;
    }

    @Override
    public String toString() 
    {
        return "-> " + destination.getNom() + " (poids: " + poids + ")";
    }
}

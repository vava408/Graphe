package metier;

/**
 * Représente un arc orienté dans le graphe.
 * Un arc va d'un noeud source (celui qui le possède dans sa liste)
 * vers un noeud destination, avec un poids (la distance ou le coût).
 *
 * Le poids peut être négatif — c'est justement pour ça qu'on a besoin
 * de Bellman-Ford dans certains cas.
 */
public class Arc
{
    // Le noeud vers lequel cet arc pointe
    private final Noeud destination;

    // Le poids de l'arc (peut être négatif)
    private final int   poids;

    public Arc(Noeud destination, int poids)
    {
        this.destination = destination;
        this.poids       = poids;
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
        return "-> " + this.destination.getNom() + " (poids: " + this.poids + ")";
    }
}
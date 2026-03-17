package metier;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un sommet du graphe.
 * Chaque noeud a un nom (ex: "A") et une liste des arcs qui en partent.
 * C'est une liste d'adjacence : pour savoir où on peut aller depuis A,
 * on regarde la liste arcsSortants de A.
 */
public class Noeud
{
    // Le nom du noeud (ex: "A", "Paris", etc.)
    private final String     nom;

    // La liste de tous les arcs qui partent de ce noeud
    private final List<Arc>  arcsSortants;

    public Noeud(String nom)
    {
        this.nom          = nom;
        this.arcsSortants = new ArrayList<>();
    }

    /**
     * Ajoute un arc sortant vers un autre noeud avec un poids donné.
     * Appelé depuis Graphe.ajouterArc().
     */
    public void ajouterArc(Noeud destination, int poids)
    {
        this.arcsSortants.add(new Arc(destination, poids));
    }

    public String getNom()
    {
        return this.nom;
    }

    public List<Arc> getArcsSortants()
    {
        return this.arcsSortants;
    }

    @Override
    public String toString()
    {
        return "Noeud(" + this.nom + ")";
    }
}
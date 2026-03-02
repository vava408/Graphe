package metier;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un sommet (noeud) du graphe.
 * Chaque noeud a un nom et une liste d'arcs sortants.
 */
public class Noeud 
{

    private final String nom;
    private final List<Arc> arcsSortants;

    public Noeud(String nom) 
    {
        this.nom = nom;
        this.arcsSortants = new ArrayList<>();
    }

    /**
     * Ajoute un arc sortant vers un noeud destination avec un poids donné.
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
        return "Noeud(" + nom + ")";
    }
}

package metier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contient le résultat d'un algorithme du plus court chemin.
 * Pour chaque noeud : la distance minimale depuis la source + le chemin emprunté.
 */
public class Resultat 
{

    private final String nomSource;
    // Distance minimale depuis la source pour chaque noeud
    private final Map<String, Integer> distances;
    // Chemin complet (liste de noms de noeuds) pour chaque noeud
    private final Map<String, List<String>> chemins;

    public Resultat(String nomSource) 
    {
        this.nomSource = nomSource;
        this.distances = new HashMap<>();
        this.chemins   = new HashMap<>();
    }

    public void setDistance(String nomNoeud, int distance) 
    {
        distances.put(nomNoeud, distance);
    }

    // Put je rapelle quelle ecrase l'acienne valeur
    // Ce qui évite que les chemins ce superpose
    public void setChemin(String nomNoeud, List<String> chemin) 
    {
        chemins.put(nomNoeud, chemin);
    }

    public int getDistance(String nomNoeud) 
    {
        return distances.getOrDefault(nomNoeud, Integer.MAX_VALUE);
    }

    public List<String> getChemin(String nomNoeud) 
    {
        return chemins.get(nomNoeud);
    }

    public Map<String, Integer> getDistances() 
    {
        return distances;
    }

    public String getNomSource() 
    {
        return nomSource;
    }
}

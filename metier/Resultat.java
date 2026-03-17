package metier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contient tout ce qu'un algorithme nous retourne après son exécution.
 *
 * Pour chaque noeud du graphe, on stocke :
 *   - la distance minimale depuis la source
 *   - le chemin complet pour y arriver (liste de noeuds dans l'ordre)
 *
 * On stocke aussi les logs (les étapes de l'algo) pour les afficher
 * dans le terminal d'affichage. Et un flag pour signaler si Bellman-Ford
 * a détecté un circuit absorbant (dans ce cas le résultat n'est pas valide).
 */
public class Resultat
{
    // Le noeud depuis lequel on a lancé le calcul
    private final String nomSource;

    // La distance minimale trouvée pour chaque noeud
    private final Map<String, Integer>       distances;

    // Le chemin complet pour aller de la source à chaque noeud
    private final Map<String, List<String>>  chemins;

    // Les lignes de log générées par l'algo (initialisation, itérations, etc.)
    private final List<String>               logs;

    // Mis à true par Bellman-Ford si un circuit absorbant est détecté
    private boolean circuitAbsorbant;

    public Resultat(String nomSource)
    {
        this.nomSource        = nomSource;
        this.distances        = new HashMap<>();
        this.chemins          = new HashMap<>();
        this.logs             = new ArrayList<>();
        this.circuitAbsorbant = false;
    }

    // Ajoute une ligne dans les logs — appelé par Dijkstra et BellmanFord
    public void ajouterLog(String ligne)
    {
        this.logs.add(ligne);
    }

    // Retourne toutes les lignes de log pour les afficher dans le terminal
    public List<String> getLogs()
    {
        return this.logs;
    }

    // Appelé par BellmanFord quand il détecte un circuit absorbant
    public void signalerCircuitAbsorbant()
    {
        this.circuitAbsorbant = true;
    }

    // Le Controller vérifie ça après le calcul pour savoir si le résultat est utilisable
    public boolean aUnCircuitAbsorbant()
    {
        return this.circuitAbsorbant;
    }

    // Enregistre la distance minimale pour un noeud donné
    public void setDistance(String nomNoeud, int distance)
    {
        this.distances.put(nomNoeud, distance);
    }

    // Enregistre le chemin pour un noeud donné
    // put() écrase l'ancienne valeur donc pas de doublon possible
    public void setChemin(String nomNoeud, List<String> chemin)
    {
        this.chemins.put(nomNoeud, chemin);
    }

    public int getDistance(String nomNoeud)
    {
        return this.distances.getOrDefault(nomNoeud, Integer.MAX_VALUE);
    }

    public List<String> getChemin(String nomNoeud)
    {
        return this.chemins.get(nomNoeud);
    }

    public Map<String, Integer> getDistances()
    {
        return this.distances;
    }

    public String getNomSource()
    {
        return this.nomSource;
    }
}
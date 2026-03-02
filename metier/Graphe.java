package metier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente le graphe orienté.
 * Contient tous les noeuds et permet d'ajouter des arcs entre eux.
 */
public class Graphe 
{

    // LinkedHashMap pour conserver l'ordre d'insertion
    private final Map<String, Noeud> mapNoeuds;

    public Graphe() 
    {
        this.mapNoeuds = new LinkedHashMap<>();
    }

    /**
     * Ajoute un noeud au graphe. Ne fait rien si le nom existe déjà.
     */
    public void ajouterNoeud(String nom) 
    {
        mapNoeuds.putIfAbsent(nom, new Noeud(nom));
    }

    /**
     * Ajoute un arc dirigé entre deux noeuds existants.
     * @return false si l'un des noeuds n'existe pas
     */
    public boolean ajouterArc(String nomSource, String nomDestination, int poids) 
    {
        Noeud source = mapNoeuds.get(nomSource);
        Noeud destination = mapNoeuds.get(nomDestination);

        if (source == null || destination == null) 
        {
            return false;
        }

        source.ajouterArc(destination, poids);
        return true;
    }

    public Noeud getNoeud(String nom) 
    {
        return mapNoeuds.get(nom);
    }

    public Collection<Noeud> getNoeuds() 
    {
        return mapNoeuds.values();
    }

    public boolean contientNoeud(String nom) 
    {
        return mapNoeuds.containsKey(nom);
    }

    public int getNombreNoeuds() 
    {
        return mapNoeuds.size();
    }
}

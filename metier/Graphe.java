package metier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente un graphe — c'est la structure principale de notre appli.
 * On stocke tous les noeuds dans une Map pour pouvoir les retrouver
 * rapidement par leur nom (genre graphe.getNoeud("A")).
 *
 * On utilise une LinkedHashMap et pas une HashMap classique parce qu'on
 * veut garder l'ordre dans lequel on a ajouté les noeuds. Ça aide pour
 * l'affichage et pour les itérations de Bellman-Ford.
 */
public class Graphe
{
    // On associe chaque nom de noeud à l'objet Noeud correspondant
    private final Map<String, Noeud> mapNoeuds;

    public Graphe()
    {
        this.mapNoeuds = new LinkedHashMap<>();
    }

    /**
     * Ajoute un noeud dans le graphe.
     * Si un noeud avec ce nom existe déjà, on ne fait rien (putIfAbsent).
     */
    public void ajouterNoeud(String nom)
    {
        this.mapNoeuds.putIfAbsent(nom, new Noeud(nom));
    }

    /**
     * Ajoute un arc orienté entre deux noeuds qui existent déjà.
     * Si l'un des deux noeuds n'existe pas dans la map, on retourne false.
     *
     * @return true si l'arc a bien été ajouté, false sinon
     */
    public boolean ajouterArc(String nomSource, String nomDestination, int poids)
    {
        Noeud source      = this.mapNoeuds.get(nomSource);
        Noeud destination = this.mapNoeuds.get(nomDestination);

        // Si l'un des noeuds n'existe pas on ne peut pas créer l'arc
        if (source == null || destination == null)
            return false;

        source.ajouterArc(destination, poids);
        return true;
    }

    // Retourne le noeud qui a ce nom, ou null s'il n'existe pas
    public Noeud getNoeud(String nom)
    {
        return this.mapNoeuds.get(nom);
    }

    // Retourne tous les noeuds du graphe (utile pour les boucles dans les algos)
    public Collection<Noeud> getNoeuds()
    {
        return this.mapNoeuds.values();
    }

    // Vérifie si un noeud avec ce nom existe dans le graphe
    public boolean contientNoeud(String nom)
    {
        return this.mapNoeuds.containsKey(nom);
    }

    // Retourne le nombre de noeuds — utilisé par Bellman-Ford pour ses (n-1) itérations
    public int getNombreNoeuds()
    {
        return this.mapNoeuds.size();
    }
}
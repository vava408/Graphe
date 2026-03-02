package vue;

import metier.Resultat;
import java.util.List;

/**
 * Interface que TOUTE vue doit implémenter.
 * Le contrôleur ne connaît que cette interface → les vues sont totalement interchangeables.
 */
public interface IVue 
{

    /**
     * Affiche un message d'information simple.
     */
    void afficherMessage(String message);

    /**
     * Affiche un message d'erreur.
     */
    void afficherErreur(String erreur);

    /**
     * Demande à l'utilisateur de choisir un algorithme parmi une liste.
     * @param algorithmes Liste des noms d'algorithmes disponibles
     * @return L'index choisi (0-based)
     */
    int demanderChoixAlgorithme(List<String> algorithmes);

    /**
     * Demande la liste des noms de noeuds à créer.
     * @return Liste des noms saisis par l'utilisateur
     */
    List<String> demanderNoeuds();

    /**
     * Demande les arcs sortants pour un noeud donné.
     * @param nomNoeud     Le noeud source dont on saisit les arcs
     * @param noeudsDispos Les noeuds vers lesquels on peut créer un arc
     * @return Liste de tableaux [nomDestination, poids] sous forme de String[]
     */
    List<String[]> demanderArcsPourNoeud(String nomNoeud, List<String> noeudsDispos);

    /**
     * Demande le nom du noeud source pour le calcul.
     * @param noeudsDispos Liste des noeuds disponibles
     * @return Le nom du noeud source choisi
     */
    String demanderNoeudSource(List<String> noeudsDispos);

    boolean demanderSiOriente();

    /**
     * Affiche le résultat du calcul.
     * @param resultat  Le résultat produit par l'algorithme
     * @param nomAlgo   Le nom de l'algorithme utilisé
     */
    void afficherResultat(Resultat resultat, String nomAlgo);
}

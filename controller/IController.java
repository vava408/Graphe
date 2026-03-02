package controller;

import java.util.List;

/**
 * Interface du contrôleur.
 * La vue appelle ces méthodes pour piloter la logique métier.
 * Grâce à cette interface, on peut avoir plusieurs contrôleurs (ex: test, prod).
 */
public interface IController 
{

    /**
     * Lance le flux complet de l'application :
     * choix algo → saisie noeuds → saisie arcs → calcul → affichage résultat.
     */
    void lancerApplication();

    /**
     * Crée les noeuds du graphe à partir d'une liste de noms.
     * @param noms Les noms des noeuds à créer
     */
    void creerNoeuds(List<String> noms);

    /**
     * Ajoute un arc dans le graphe.
     * @param nomSource      Noeud de départ
     * @param nomDestination Noeud d'arrivée
     * @param poids          Poids de l'arc
     * @return true si l'ajout a réussi, false sinon
     */
    boolean ajouterArc(String nomSource, String nomDestination, int poids);

    /**
     * Lance le calcul de l'algorithme choisi depuis le noeud source.
     * @param nomSource Noeud de départ
     */
    void lancerCalcul(String nomSource);
}

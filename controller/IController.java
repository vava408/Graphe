package controller;

import java.util.List;

/**
 * Interface du contrôleur principal.
 *
 * Définit le contrat entre la vue et la logique métier.
 * Toute vue (IHM Swing ou console) passe par ces méthodes pour piloter l'application.
 *
 * Avantage : on peut remplacer Controller par une autre implémentation
 * (ex : mock pour les tests) sans toucher à la vue.
 */
public interface IController
{
    /**
     * Lance l'IHM Swing.
     * Crée et affiche la fenêtre principale (Frame).
     */
    void lancerIHM();

    /**
     * Lance le mode console système.
     * Lit sur System.in et écrit sur System.out.
     * Utilisé quand l'application est démarrée avec l'argument "console".
     */
    void lancerConsole();

    /**
     * Crée (ou recrée) tous les noeuds du graphe à partir d'une liste de noms.
     * Réinitialise le graphe existant avant d'ajouter les nouveaux noeuds.
     *
     * @param noms Liste des noms de noeuds à créer
     */
    void creerNoeuds(List<String> noms);

    /**
     * Ajoute un arc orienté entre deux noeuds existants du graphe.
     *
     * @param nomSource      Noeud de départ
     * @param nomDestination Noeud d'arrivée
     * @param poids          Poids de l'arc (peut être négatif pour Bellman-Ford)
     * @return true si l'arc a été ajouté, false si l'un des noeuds est introuvable
     */
    boolean ajouterArc(String nomSource, String nomDestination, int poids);

    /**
     * Lance le calcul du plus court chemin depuis l'IHM (PanelTableau).
     * Le résultat est affiché selon les options choisies dans le menu (popup, terminal).
     *
     * @param nomSource       Noeud de départ
     * @param algorithmeIndex Index de l'algorithme : 0 = Dijkstra, 1 = Bellman-Ford
     * @param estOriente      true = graphe orienté, false = arcs retour ajoutés automatiquement
     */
    void lancerCalcul(String nomSource, int algorithmeIndex, boolean estOriente);
}
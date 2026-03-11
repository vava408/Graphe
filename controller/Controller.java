package controller;

import metier.*;
import vue.Frame;
import vue.PanelTerminal;
import vue.VueConsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contrôleur principal de l'application.
 *
 * Fait le lien entre la vue (Frame / VueConsole) et le modèle (Graphe + algorithmes).
 * Gère trois modes de fonctionnement :
 *
 *   1. lancerIHM()                → ouvre la fenêtre Swing, résultat affiché selon les options du menu
 *   2. lancerConsole()            → interaction dans le terminal système (System.in / System.out)
 *   3. lancerConsoleTerminal(...) → interaction dans le PanelTerminal intégré à la fenêtre Swing
 *
 * La logique de saisie console est factorisée dans executerFluxConsole()
 * pour éviter toute duplication entre les modes 2 et 3.
 */
public class Controller implements IController
{
    /** Graphe courant sur lequel on travaille. Réinitialisé à chaque creerNoeuds(). */
    private Graphe      graphe;

    /** Algorithme sélectionné pour le calcul en cours. */
    private IAlgorithme algorithme;

    /** Fenêtre principale — null jusqu'à l'appel de lancerIHM(). */
    private Frame      frame;

    /** Vue console — réinstanciée selon le mode (système ou terminal intégré). */
    private VueConsole vueConsole;

    /** Dernier résultat calculé, utilisé par la vue graphique pour la mise en évidence. */
    private Resultat dernierResultat;

    /**
     * Liste fixe des algorithmes disponibles dans l'application.
     * Index 0 = Dijkstra, Index 1 = Bellman-Ford.
     * Correspond aux indices utilisés dans PanelTableau et lancerCalcul().
     */
    private final List<IAlgorithme> algorithmesDisponibles;

    public Controller()
    {
        this.graphe                 = new Graphe();
        this.vueConsole             = new VueConsole();
        this.dernierResultat        = null;
        this.algorithmesDisponibles = Arrays.asList(new Dijkstra(), new BellmanFord());
        // Frame intentionnellement non créée ici : elle est créée dans lancerIHM()
    }

    // =========================================================================
    // Lancement
    // =========================================================================

    /**
     * Crée et affiche la fenêtre Swing.
     * La Frame se charge elle-même d'afficher PanelTableau et PanelGraphe.
     */
    @Override
    public void lancerIHM()
    {
        this.frame = new Frame(this);
    }

    /**
     * Lance la saisie interactive en mode console système.
     * Crée un VueConsole branché sur System.in / System.out.
     */
    @Override
    public void lancerConsole()
    {
        this.vueConsole = new VueConsole();
        executerFluxConsole(vueConsole);
    }

    /**
     * Lance la saisie interactive dans le terminal intégré à l'IHM.
     * Appelé par Frame dans un thread dédié (car readLine() est bloquant).
     *
     * @param terminal Le PanelTerminal dans lequel lire et écrire
     */
    public void lancerConsoleTerminal(PanelTerminal terminal)
    {
        VueConsole vueTerminal = new VueConsole(terminal);
        executerFluxConsole(vueTerminal);
    }

    // =========================================================================
    // Flux console commun
    // =========================================================================

    /**
     * Enchaîne toutes les étapes de saisie et de calcul en mode console.
     * Utilisé aussi bien par lancerConsole() que lancerConsoleTerminal()
     * pour éviter la duplication de code.
     *
     * Étapes :
     *   1. Choix de l'algorithme
     *   2. Saisie des noeuds
     *   3. Graphe orienté ou non
     *   4. Saisie des arcs
     *   5. Choix du noeud source + calcul + affichage du résultat
     *
     * @param vue La VueConsole à utiliser (système ou terminal intégré)
     */
    private void executerFluxConsole(VueConsole vue)
    {
        // --- Étape 1 : choix de l'algorithme ---
        List<String> nomsAlgos = new ArrayList<>();
        for (IAlgorithme algo : algorithmesDisponibles)
            nomsAlgos.add(algo.getNom());

        int choix       = vue.demanderChoixAlgorithme(nomsAlgos);
        this.algorithme = algorithmesDisponibles.get(choix);
        vue.afficherMessage("Algorithme sélectionné : " + algorithme.getNom());

        // --- Étape 2 : saisie des noeuds ---
        List<String> nomsNoeuds = vue.demanderNoeuds();
        creerNoeuds(nomsNoeuds);
        vue.afficherMessage(nomsNoeuds.size() + " noeud(s) créé(s).");

        // --- Étape 3 : orienté ou non ---
        boolean oriente = vue.demanderSiOriente();
        vue.afficherMessage(oriente ? "Graphe orienté." : "Graphe non-orienté.");

        // --- Étape 4 : saisie des arcs ---
        for (String nomNoeud : nomsNoeuds)
        {
            List<String[]> arcs = vue.demanderArcsPourNoeud(nomNoeud, nomsNoeuds);
            for (String[] arc : arcs)
            {
                String destination = arc[0];
                try
                {
                    int poids = Integer.parseInt(arc[1]);
                    ajouterArc(nomNoeud, destination, poids);

                    // Graphe non orienté : on ajoute l'arc dans le sens inverse aussi
                    if (!oriente)
                        ajouterArc(destination, nomNoeud, poids);
                }
                catch (NumberFormatException e)
                {
                    vue.afficherErreur("Poids invalide pour l'arc " + nomNoeud + " -> " + destination);
                }
            }
        }

        // --- Étape 5 : calcul et affichage ---
        String   source   = vue.demanderNoeudSource(nomsNoeuds);
        Resultat resultat = algorithme.calculer(graphe, source);

        if (resultat == null)
        {
            vue.afficherErreur("Calcul impossible : cycle de poids négatif détecté.");
            return;
        }

        vue.afficherResultat(resultat, algorithme.getNom());
    }

    // =========================================================================
    // Opérations sur le graphe — appelées par PanelTableau (mode IHM)
    // =========================================================================

    /**
     * Recrée le graphe depuis zéro avec la liste de noms fournie.
     * Toute donnée précédente est perdue.
     */
    @Override
    public void creerNoeuds(List<String> noms)
    {
        this.graphe = new Graphe();
        for (String nom : noms)
            graphe.ajouterNoeud(nom.trim());
    }

    /**
     * Ajoute un arc orienté dans le graphe courant.
     * Les espaces en début/fin de nom sont ignorés.
     */
    @Override
    public boolean ajouterArc(String nomSource, String nomDestination, int poids)
    {
        return graphe.ajouterArc(nomSource.trim(), nomDestination.trim(), poids);
    }

    /**
     * Calcule le plus court chemin depuis nomSource et affiche le résultat.
     * Appelé par PanelTableau quand l'utilisateur clique sur "Calculer".
     *
     * Si le graphe est non orienté, les arcs retour sont ajoutés avant le calcul.
     * Le résultat est transmis à Frame qui l'affiche selon les options du menu.
     */
    @Override
    public void lancerCalcul(String nomSource, int algorithmeIndex, boolean estOriente)
    {
        // Vérification de l'index
        if (algorithmeIndex < 0 || algorithmeIndex >= algorithmesDisponibles.size())
        {
            frame.afficherErreur("Index d'algorithme invalide : " + algorithmeIndex);
            return;
        }

        this.algorithme = algorithmesDisponibles.get(algorithmeIndex);

        // Vérification que le noeud source existe dans le graphe
        if (!graphe.contientNoeud(nomSource))
        {
            frame.afficherErreur("Le noeud source '" + nomSource + "' n'existe pas.");
            return;
        }

        // Lancement du calcul
        Resultat resultat = algorithme.calculer(graphe, nomSource);

        if (resultat == null)
        {
            this.dernierResultat = null;
            frame.afficherErreur("Calcul impossible : cycle de poids négatif détecté.");
            return;
        }

        this.dernierResultat = resultat;

        // Transmission du résultat à la Frame — c'est elle qui décide comment l'afficher
        frame.afficherResultat(resultat, algorithme.getNom());
    }
    public List<String> getNomsNoeuds()
    {
        List<String> noms = new ArrayList<>();
        for (Noeud noeud : this.graphe.getNoeuds())
            noms.add(noeud.getNom());
        return noms;
    }

    public List<String[]> getArcsPourAffichage()
    {
        List<String[]> arcs = new ArrayList<>();

        for (Noeud source : this.graphe.getNoeuds())
        {
            for (Arc arc : source.getArcsSortants())
            {
                arcs.add(new String[]
                {
                    source.getNom(),
                    arc.getDestination().getNom(),
                    String.valueOf(arc.getPoids())
                });
            }
        }

        return arcs;
    }

    public Set<String> getArcsPlusCourtsChemins()
    {
        Set<String> arcs = new HashSet<>();

        if (this.dernierResultat == null)
            return arcs;

        for (String nomNoeud : this.dernierResultat.getDistances().keySet())
        {
            List<String> chemin = this.dernierResultat.getChemin(nomNoeud);
            if (chemin == null || chemin.size() < 2)
                continue;

            for (int i = 0; i < chemin.size() - 1; i++)
            {
                arcs.add(chemin.get(i) + "->" + chemin.get(i + 1));
            }
        }

        return arcs;
    }

}
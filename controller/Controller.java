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
 */
public class Controller implements IController
{
    private Graphe      graphe;
    private IAlgorithme algorithme;
    private Frame       frame;
    private VueConsole  vueConsole;
    private Resultat    dernierResultat;

    // Index du chemin actuellement affiché (pour le bouton "Chemin suivant")
    private int indexCheminActuel = 0;

    // Liste des noms de noeuds destination dans l'ordre du résultat
    // (pour naviguer chemin par chemin)
    private List<String> nomsDestinations = new ArrayList<>();

    private final List<IAlgorithme> algorithmesDisponibles;

    public Controller()
    {
        this.graphe                 = new Graphe();
        this.vueConsole             = new VueConsole();
        this.dernierResultat        = null;
        this.algorithmesDisponibles = Arrays.asList(new Dijkstra(), new BellmanFord());
    }

    // =========================================================================
    // Lancement
    // =========================================================================

    @Override
    public void lancerIHM()
    {
        this.frame = new Frame(this);
    }

    @Override
    public void lancerConsole()
    {
        this.vueConsole = new VueConsole();
        executerFluxConsole(vueConsole);
    }

    public void lancerConsoleTerminal(PanelTerminal terminal)
    {
        VueConsole vueTerminal = new VueConsole(terminal);
        executerFluxConsole(vueTerminal);
    }

    // =========================================================================
    // Flux console
    // =========================================================================

    private void executerFluxConsole(VueConsole vue)
    {
        List<String> nomsAlgos = new ArrayList<>();
        for (IAlgorithme algo : algorithmesDisponibles)
            nomsAlgos.add(algo.getNom());

        int choix       = vue.demanderChoixAlgorithme(nomsAlgos);
        this.algorithme = algorithmesDisponibles.get(choix);
        vue.afficherMessage("Algorithme sélectionné : " + this.algorithme.getNom());

        List<String> nomsNoeuds = vue.demanderNoeuds();
        creerNoeuds(nomsNoeuds);
        vue.afficherMessage(nomsNoeuds.size() + " noeud(s) créé(s).");

        boolean oriente = vue.demanderSiOriente();
        vue.afficherMessage(oriente ? "Graphe orienté." : "Graphe non-orienté.");

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
                    if (!oriente && poids >= 0)
                        ajouterArc(destination, nomNoeud, poids);
                }
                catch (NumberFormatException e)
                {
                    vue.afficherErreur("Poids invalide pour l'arc " + nomNoeud + " -> " + destination);
                }
            }
        }

        String   source   = vue.demanderNoeudSource(nomsNoeuds);
        Resultat resultat = this.algorithme.calculer(graphe, source);

        if (resultat == null)
        {
            vue.afficherErreur("Erreur inattendue lors du calcul.");
            return;
        }

        this.envoyerLogsTerminalAffichage(resultat);

        if (resultat.aUnCircuitAbsorbant())
        {
            vue.afficherErreur("Circuit absorbant détecté. Voir le terminal d'affichage.");
            return;
        }

        vue.afficherResultat(resultat, this.algorithme.getNom());
    }

    // =========================================================================
    // Opérations sur le graphe
    // =========================================================================

    @Override
    public void creerNoeuds(List<String> noms)
    {
        this.graphe = new Graphe();
        for (String nom : noms)
            graphe.ajouterNoeud(nom.trim());
    }

    @Override
    public boolean ajouterArc(String nomSource, String nomDestination, int poids)
    {
        return graphe.ajouterArc(nomSource.trim(), nomDestination.trim(), poids);
    }

    @Override
    public void lancerCalcul(String nomSource, int algorithmeIndex, boolean estOriente)
    {
        if (algorithmeIndex < 0 || algorithmeIndex >= this.algorithmesDisponibles.size())
        {
            this.frame.afficherErreur("Index d'algorithme invalide : " + algorithmeIndex);
            return;
        }

        this.algorithme = this.algorithmesDisponibles.get(algorithmeIndex);

        if (!this.graphe.contientNoeud(nomSource))
        {
            this.frame.afficherErreur("Le noeud source '" + nomSource + "' n'existe pas.");
            return;
        }

        Resultat resultat = this.algorithme.calculer(this.graphe, nomSource);

        if (resultat == null)
        {
            this.dernierResultat = null;
            this.frame.afficherErreur("Erreur inattendue lors du calcul.");
            return;
        }

        this.envoyerLogsTerminalAffichage(resultat);

        if (resultat.aUnCircuitAbsorbant())
        {
            this.dernierResultat = null;
            this.frame.afficherErreur(
                "Calcul impossible : circuit absorbant détecté.\n" +
                "Voir le terminal d'affichage pour le détail."
            );
            return;
        }

        this.dernierResultat = resultat;

        // On prépare la liste des destinations pour la navigation chemin par chemin
        // On ignore la source elle-même (chemin vide)
        this.nomsDestinations = new ArrayList<>();
        for (String nom : resultat.getDistances().keySet())
        {
            List<String> chemin = resultat.getChemin(nom);
            if (chemin != null && chemin.size() >= 2)
                this.nomsDestinations.add(nom);
        }

        // On repart toujours du premier chemin après un nouveau calcul
        this.indexCheminActuel = 0;

        this.frame.afficherResultat(resultat, this.algorithme.getNom());
    }

    /**
     * Passe au chemin suivant dans la liste et demande au panel graphe de l'afficher.
     * Appelé par PanelTableau quand l'utilisateur clique sur "Chemin suivant".
     *
     * @param estOriente pour savoir comment dessiner les arcs
     * @param destination si non null, on affiche uniquement ce chemin précis
     */
    public void afficherCheminSuivant(boolean estOriente, String destination)
    {
        if (this.dernierResultat == null) return;

        // Cas avec point d'arrivée choisi : on affiche juste ce chemin
        if (destination != null && !destination.isEmpty())
        {
            this.frame.demanderAffichageChemin(
                this.dernierResultat.getChemin(destination),
                0,
                estOriente
            );
            return;
        }

        // Cas sans point d'arrivée : on navigue chemin par chemin
        if (this.nomsDestinations.isEmpty()) return;

        String nomDest = this.nomsDestinations.get(this.indexCheminActuel);
        List<String> chemin = this.dernierResultat.getChemin(nomDest);

        // On envoie l'index pour la couleur et le numéro du chemin
        this.frame.demanderAffichageChemin(chemin, this.indexCheminActuel, estOriente);

        // On passe au chemin suivant (boucle sur la liste)
        this.indexCheminActuel = (this.indexCheminActuel + 1) % this.nomsDestinations.size();
    }

    /**
     * Retourne le nombre total de chemins disponibles.
     * Utilisé par PanelTableau pour afficher "Chemin X / Y" sur le bouton.
     */
    public int getNombreChemins()
    {
        return this.nomsDestinations.size();
    }

    /**
     * Retourne l'index du chemin actuellement affiché.
     */
    public int getIndexCheminActuel()
    {
        return this.indexCheminActuel;
    }

    private void envoyerLogsTerminalAffichage(Resultat resultat)
    {
        if (this.frame != null)
            this.frame.afficherLogsTerminal(resultat.getLogs());
    }

    // =========================================================================
    // Getters pour PanelGraphe
    // =========================================================================

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
        if (this.dernierResultat == null) return arcs;

        for (String nomNoeud : this.dernierResultat.getDistances().keySet())
        {
            List<String> chemin = this.dernierResultat.getChemin(nomNoeud);
            if (chemin == null || chemin.size() < 2) continue;

            for (int i = 0; i < chemin.size() - 1; i++)
                arcs.add(chemin.get(i) + "->" + chemin.get(i + 1));
        }
        return arcs;
    }
}
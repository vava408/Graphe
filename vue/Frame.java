package vue;

import controller.Controller;
import metier.Resultat;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * La fenêtre principale de l'application.
 *
 * Elle est découpée en trois zones côte à côte :
 *   - Gauche  => terminal d'affichage (logs de l'algo, lecture seule)
 *   - Milieu  => tableau de saisie OU terminal de saisie
 *   - Droite  => visualisation graphique du graphe
 *
 * Le menu "Options" en haut propose trois cases à cocher :
 *   - Popup résultat       => affiche les résultats dans une boîte de dialogue
 *   - Terminal de saisie   => remplace le tableau par un terminal interactif
 *   - Terminal d'affichage => ajoute un terminal à gauche pour voir le déroulé de l'algo
 *
 * Les trois options sont indépendantes, on peut tout cocher en même temps.
 */
public class Frame extends JFrame
{
    // Vaut true si l'utilisateur a coché "Popup résultat" dans le menu
    private boolean afficherPopup             = false;

    // Vaut true si l'utilisateur a coché "Terminal de saisie"
    private boolean afficherTerminalSaisie    = false;

    // Vaut true si l'utilisateur a coché "Terminal d'affichage"
    private boolean afficherTerminalAffichage = false;

    // Le contrôleur, on en a besoin pour créer les panels et lancer les sessions
    private final Controller controller;

    // Zone gauche => terminal d'affichage, cachée par défaut
    private final JPanel panelGauche;

    // Zone milieu => alterne entre le tableau et le terminal de saisie
    private final JPanel panelMilieu;

    // Zone droite => le graphe dessiné avec GraphStream
    private final PanelGraphe panelGraphe;

    // Le tableau de saisie des arcs, affiché par défaut dans le panel milieu
    private final PanelTableau panelTableau;

    // Le terminal de saisie, créé seulement si l'utilisateur coche l'option
    // (on ne le crée pas au démarrage pour ne pas charger inutilement)
    private PanelTerminal panelTerminalSaisie;

    // Le terminal d'affichage, idem, créé à la demande
    private PanelTerminal panelTerminalAffichage;

    public Frame(Controller controller)
    {
        this.controller = controller;

        this.setTitle("Algorithme de Graphes");
        this.setSize(1300, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // On crée les trois zones principales
        this.panelGraphe  = new PanelGraphe(this.controller);
        this.panelMilieu  = new JPanel(new BorderLayout());
        this.panelGauche  = new JPanel(new BorderLayout());
        this.panelTableau = new PanelTableau(this.controller, this.panelGraphe);

        // Par défaut, le milieu affiche le tableau de saisie
        this.panelMilieu.add(this.panelTableau, BorderLayout.CENTER);

        // Au démarrage on n'a que deux colonnes : milieu + droite
        this.setLayout(new GridLayout(1, 2));
        this.add(this.panelMilieu);
        this.add(this.panelGraphe);

        this.setJMenuBar(this.creerMenuBar());
        this.setVisible(true);
    }

    // =========================================================================
    // Construction du menu
    // =========================================================================

    /**
     * Crée la barre de menu avec les trois options cochables.
     * Chaque option est indépendante des autres.
     */
    private JMenuBar creerMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu    menu    = new JMenu("Options");

        JCheckBoxMenuItem itemPopup             = new JCheckBoxMenuItem("Popup résultat",       false);
        JCheckBoxMenuItem itemTerminalSaisie    = new JCheckBoxMenuItem("Terminal de saisie",   false);
        JCheckBoxMenuItem itemTerminalAffichage = new JCheckBoxMenuItem("Terminal d'affichage", false);

        // Quand on coche/décoche "Popup résultat", on met juste à jour le booléen
        itemPopup.addActionListener(e ->
        {
            this.afficherPopup = itemPopup.isSelected();
        });

        // Quand on coche "Terminal de saisie" => on bascule vers le terminal
        // Quand on décoche => on remet le tableau
        itemTerminalSaisie.addActionListener(e ->
        {
            this.afficherTerminalSaisie = itemTerminalSaisie.isSelected();
            if (this.afficherTerminalSaisie)
                this.basculerVersTerminalSaisie();
            else
                this.afficherPanelTableau();
        });

        // Quand on coche "Terminal d'affichage" => on ajoute la colonne gauche
        // Quand on décoche => on la retire et on repasse en 2 colonnes
        itemTerminalAffichage.addActionListener(e ->
        {
            this.afficherTerminalAffichage = itemTerminalAffichage.isSelected();
            if (this.afficherTerminalAffichage)
                this.afficherTerminalAffichagePanel();
            else
                this.masquerTerminalAffichagePanel();
        });

        menu.add(itemPopup);
        menu.add(itemTerminalSaisie);
        menu.add(itemTerminalAffichage);
        menuBar.add(menu);

        return menuBar;
    }

    // =========================================================================
    // Gestion du panel milieu (tableau <=> terminal de saisie)
    // =========================================================================

    /**
     * Remet le tableau de saisie dans le panel milieu.
     * Appelé quand l'utilisateur décoche "Terminal de saisie".
     */
    private void afficherPanelTableau()
    {
        this.panelMilieu.removeAll();
        this.panelMilieu.add(this.panelTableau, BorderLayout.CENTER);
        this.panelMilieu.revalidate();
        this.panelMilieu.repaint();
    }

    /**
     * Remplace le tableau par le terminal de saisie dans le panel milieu.
     *
     * Le terminal est créé une seule fois (la première fois qu'on coche l'option).
     * La session console tourne dans un thread daemon séparé parce que readLine()
     * est bloquant => si on l'appelait dans l'EDT, toute l'interface serait gelée.
     */
    private void basculerVersTerminalSaisie()
    {
        // On crée le terminal seulement la première fois
        if (this.panelTerminalSaisie == null)
            this.panelTerminalSaisie = new PanelTerminal(false);

        this.panelMilieu.removeAll();
        this.panelMilieu.add(this.panelTerminalSaisie, BorderLayout.CENTER);
        this.panelMilieu.revalidate();
        this.panelMilieu.repaint();

        // Thread daemon => s'arrête automatiquement quand la fenêtre se ferme
        Thread threadConsole = new Thread(() ->
        {
            this.panelTerminalSaisie.effacer();
            this.panelTerminalSaisie.afficherLigne("=== Terminal de saisie — entrez votre graphe ===\n");
            this.controller.lancerConsoleTerminal(this.panelTerminalSaisie);
        });
        threadConsole.setDaemon(true);
        threadConsole.start();
    }

    // =========================================================================
    // Gestion du panel gauche (terminal d'affichage)
    // =========================================================================

    /**
     * Ajoute le terminal d'affichage à gauche et passe en 3 colonnes.
     *
     * Ce terminal est en lecture seule, l'utilisateur ne peut rien y taper.
     * Il reçoit les logs envoyés par le Controller après chaque calcul
     * => initialisation, itérations, détection de circuit absorbant, etc.
     */
    private void afficherTerminalAffichagePanel()
    {
        // On crée le terminal d'affichage seulement la première fois
        if (this.panelTerminalAffichage == null)
            this.panelTerminalAffichage = new PanelTerminal(true);

        // On reconstruit la fenêtre avec 3 colonnes
        this.getContentPane().removeAll();
        this.setLayout(new GridLayout(1, 3));
        this.add(this.panelGauche);
        this.add(this.panelMilieu);
        this.add(this.panelGraphe);

        this.panelGauche.removeAll();
        this.panelGauche.add(this.panelTerminalAffichage, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    /**
     * Retire le terminal d'affichage et repasse en 2 colonnes.
     * Appelé quand l'utilisateur décoche "Terminal d'affichage".
     */
    private void masquerTerminalAffichagePanel()
    {
        this.getContentPane().removeAll();
        this.setLayout(new GridLayout(1, 2));
        this.add(this.panelMilieu);
        this.add(this.panelGraphe);

        this.revalidate();
        this.repaint();
    }

    // =========================================================================
    // Méthodes appelées par le Controller
    // =========================================================================

    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * Toujours visible quelle que soit l'option choisie dans le menu.
     */
    public void afficherErreur(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Envoie les logs de l'algorithme dans le terminal d'affichage.
     * Si le terminal d'affichage n'est pas actif, on ne fait rien.
     *
     * @param logs => les lignes générées par Dijkstra ou Bellman-Ford pendant le calcul
     */
    public void afficherLogsTerminal(List<String> logs)
    {
        // Si le terminal d'affichage n'est pas coché, on sort directement
        if (!this.afficherTerminalAffichage || this.panelTerminalAffichage == null)
            return;

        // On efface les logs du calcul précédent avant d'afficher les nouveaux
        this.panelTerminalAffichage.effacer();

        for (String ligne : logs)
            this.panelTerminalAffichage.afficherLigne(ligne);
    }

    /**
     * Affiche le résultat dans une popup si l'option est cochée.
     * Si "Popup résultat" est décoché, on ne fait rien du tout.
     *
     * @param resultat => le résultat produit par l'algo
     * @param nomAlgo  => le nom de l'algo utilisé, affiché en titre
     */
    public void afficherResultat(Resultat resultat, String nomAlgo)
    {
        if (!this.afficherPopup)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("Algorithme : ").append(nomAlgo).append("\n");
        sb.append("Source     : ").append(resultat.getNomSource()).append("\n\n");
        sb.append(String.format("%-10s %-10s %s%n", "Noeud", "Distance", "Chemin"));
        sb.append("--------------------------------------------\n");

        for (Map.Entry<String, Integer> entry : resultat.getDistances().entrySet())
        {
            String       nom    = entry.getKey();
            int          dist   = entry.getValue();
            List<String> chemin = resultat.getChemin(nom);

            // Integer.MAX_VALUE = inaccessible, on affiche ∞ à la place
            String distStr   = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
            String cheminStr = (chemin == null || chemin.isEmpty())
                               ? "Inaccessible"
                               : String.join(" -> ", chemin);

            sb.append(String.format("%-10s %-10s %s%n", nom, distStr, cheminStr));
        }

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Résultat — " + nomAlgo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Appelée par le Controller pour colorier un chemin sur le graphe.
     * On délègue directement au PanelGraphe qui s'occupe du rendu.
     *
     * @param chemin     => la liste des noeuds du chemin dans l'ordre
     * @param index      => l'index de couleur (0=rouge, 1=bleu, etc.)
     * @param estOriente => pour savoir dans quel sens chercher les arcs
     */
    public void demanderAffichageChemin(List<String> chemin, int index, boolean estOriente)
    {
        this.panelGraphe.afficherChemin(chemin, index, estOriente);
    }
}
package vue;

import controller.Controller;
import metier.Resultat;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Fenêtre principale de l'application (IHM Swing).
 *
 * Contient deux zones côte à côte :
 *   - Gauche : soit PanelTableau (saisie IHM), soit PanelTerminal (terminal intégré)
 *   - Droite : PanelGraphe (visualisation du graphe — à implémenter)
 *
 * Un menu "Options" en haut permet à l'utilisateur de choisir librement
 * comment afficher les résultats. Les deux options sont des cases indépendantes
 * (JCheckBoxMenuItem) : elles peuvent être cochées, décochées, combinées ou toutes deux inactives.
 *
 *   Popup résultat   => affiche le résultat dans une boîte de dialogue JOptionPane
 *   Terminal intégré => remplace le panel gauche par un terminal texte interactif
 *
 * C'est la Frame qui décide comment afficher le résultat (méthode afficherResultat).
 * Le Controller lui transmet simplement l'objet Resultat sans se soucier de l'affichage.
 */
public class Frame extends JFrame
{
    /**
     * true si l'utilisateur a coché "Popup résultat" dans le menu.
     * Quand false, afficherResultat() ne fait rien (pas de popup).
     */
    private boolean afficherPopup    = false;

    /**
     * true si l'utilisateur a coché "Terminal intégré" dans le menu.
     * Quand true, le panel gauche affiche PanelTerminal au lieu de PanelTableau.
     */
    private boolean afficherTerminal = false;

    /** Référence au contrôleur — transmise à PanelTableau et utilisée pour le terminal. */
    private final Controller    controller;

    /** Conteneur gauche qui alterne entre PanelTableau et PanelTerminal selon le mode. */
    private final JPanel        panelGauche;

    /** Panel de saisie IHM (tableau d'arcs + boutons). Toujours créé, affiché en mode par défaut. */
    private final PanelTableau  panelTableau;

    /** Panel de visualisation graphique du graphe (côté droit, à implémenter). */
    private final PanelGraphe   panelGraphe;

    /** Terminal intégré — créé à la demande lors du premier passage en mode terminal. */
    private PanelTerminal panelTerminal;

    public Frame(Controller controller)
    {
        this.controller = controller;

        this.setTitle("Algorithme de Graphes");
        this.setSize(900, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        panelGraphe = new PanelGraphe(this.controller);


        // Panel gauche : démarre avec PanelTableau
        panelGauche  = new JPanel(new BorderLayout());
        panelTableau = new PanelTableau(controller, panelGraphe);
        panelGauche.add(panelTableau, BorderLayout.CENTER);


        this.setLayout(new GridLayout(1, 2));
        this.add(panelGauche);
        this.add(panelGraphe);

        this.setJMenuBar(creerMenuBar());
        this.setVisible(true);
    }

    // =========================================================================
    // Construction du menu
    // =========================================================================

    /**
     * Crée la barre de menu avec le menu "Options".
     *
     * Deux JCheckBoxMenuItem indépendants — aucun ButtonGroup.
     * L'utilisateur peut cocher/décocher chaque option librement,
     * y compris ne rien cocher du tout (aucun résultat affiché).
     */
    private JMenuBar creerMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu    menu    = new JMenu("Options");

        JCheckBoxMenuItem itemPopup    = new JCheckBoxMenuItem("Popup résultat",   false);
        JCheckBoxMenuItem itemTerminal = new JCheckBoxMenuItem("Terminal intégré", false);

        // Quand l'utilisateur coche/décoche "Popup résultat"
        itemPopup.addActionListener(e ->
        {
            afficherPopup = itemPopup.isSelected();
            // Si le terminal n'est pas actif, on s'assure que le tableau est bien affiché
            if (!afficherTerminal)
                afficherPanelTableau();
        });

        // Quand l'utilisateur coche/décoche "Terminal intégré"
        itemTerminal.addActionListener(e ->
        {
            afficherTerminal = itemTerminal.isSelected();
            if (afficherTerminal)
                basculerVersTerminal();   // swap vers le terminal + démarrage de la session
            else
                afficherPanelTableau();   // swap retour vers le tableau
        });

        menu.add(itemPopup);
        menu.add(itemTerminal);
        menuBar.add(menu);

        return menuBar;
    }

    // =========================================================================
    // Gestion du swap de panel gauche
    // =========================================================================

    /**
     * Affiche PanelTableau dans le panel gauche.
     * Appelé au démarrage et quand l'utilisateur décoche "Terminal intégré".
     */
    private void afficherPanelTableau()
    {
        panelGauche.removeAll();
        panelGauche.add(panelTableau, BorderLayout.CENTER);
        panelGauche.revalidate();
        panelGauche.repaint();
    }

    /**
     * Remplace le panel gauche par le terminal intégré et démarre une session console.
     *
     * Le PanelTerminal est créé une seule fois (lazy init).
     * La session console tourne dans un thread daemon séparé car readLine() est bloquant :
     * si on l'appelait dans l'EDT (Event Dispatch Thread), toute l'IHM serait gelée.
     */
    private void basculerVersTerminal()
    {
        // Création du terminal au premier passage
        if (panelTerminal == null)
            panelTerminal = new PanelTerminal();

        panelGauche.removeAll();
        panelGauche.add(panelTerminal, BorderLayout.CENTER);
        panelGauche.revalidate();
        panelGauche.repaint();

        // Thread daemon : s'arrête automatiquement quand la JVM se ferme
        Thread threadConsole = new Thread(() ->
        {
            panelTerminal.effacer();
            panelTerminal.afficherLigne("=== Mode terminal — saisissez votre graphe ===\n");
            controller.lancerConsoleTerminal(panelTerminal);
        });
        threadConsole.setDaemon(true);
        threadConsole.start();
    }

    // =========================================================================
    // Méthodes appelées par le Controller
    // =========================================================================

    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     * Toujours visible, quelle que soit l'option choisie dans le menu.
     *
     * @param message Le message à afficher
     */
    public void afficherErreur(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Affiche le résultat du calcul selon les options cochées dans le menu.
     *
     * Si "Popup résultat" est décoché → rien n'est affiché (comportement voulu par l'utilisateur).
     * Si "Popup résultat" est coché   → une boîte de dialogue résume distances et chemins.
     * Le terminal intégré affiche lui-même son résultat via VueConsole.afficherResultat().
     *
     * @param resultat Le résultat produit par l'algorithme
     * @param nomAlgo  Le nom de l'algorithme utilisé (pour l'en-tête)
     */
    public void afficherResultat(Resultat resultat, String nomAlgo)
    {
        // Popup désactivé : on ne fait rien
        if (!afficherPopup)
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

            String distStr   = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
            String cheminStr = (chemin == null || chemin.isEmpty())
                               ? "Inaccessible"
                               : String.join(" -> ", chemin);

            sb.append(String.format("%-10s %-10s %s%n", nom, distStr, cheminStr));
        }

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Résultat — " + nomAlgo, JOptionPane.INFORMATION_MESSAGE);
    }
}
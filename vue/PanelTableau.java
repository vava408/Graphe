package vue;

import controller.Controller;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.*;

/**
 * Le panel de saisie des arcs du graphe.
 *
 * C'est ici que l'utilisateur entre les arcs sous forme de tableau
 * (Source | Destination | Poids) et choisit les options de calcul.
 *
 * En bas du panel il y a les contrôles :
 *   - Les boutons pour gérer les lignes du tableau
 *   - Le choix de l'algorithme (auto-basculé sur Bellman-Ford si poids négatif)
 *   - La case à cocher "Orienté"
 *   - Le point de départ et le point d'arrivée (optionnel)
 *   - Le bouton Calculer
 *
 * En haut du panel, le bouton "Chemin suivant" apparaît après le calcul
 * pour naviguer entre les différents chemins calculés.
 */
public class PanelTableau extends JPanel
{
    // Le modèle du tableau, c'est lui qui contient les données des lignes
    private final DefaultTableModel model;

    // Le tableau Swing qui affiche les arcs
    private final JTable            table;

    // Combobox pour choisir le noeud de départ du calcul
    private final JComboBox<String> cbPointDepart;

    // Combobox pour choisir un noeud d'arrivée (optionnel)
    // Si "Aucun" est sélectionné, on calcule tous les chemins
    private final JComboBox<String> cbPointArrivee;

    // Combobox pour choisir l'algorithme (Dijkstra ou Bellman-Ford)
    // Auto-bloquée sur Bellman-Ford si un poids négatif est détecté
    private final JComboBox<String> cbAlgorithme;

    // Case à cocher pour dire si le graphe est orienté ou non
    private final JCheckBox         cbOriente;

    // Bouton pour passer au chemin suivant, caché au départ
    // Apparaît après le calcul si aucun point d'arrivée n'est choisi
    private final JButton           btnCheminSuivant;

    // Le contrôleur qu'on appelle pour créer le graphe et lancer le calcul
    private final Controller        controller;

    // Le panel graphique, on l'appelle pour rafraîchir l'affichage après le calcul
    private final PanelGraphe       panelGraphe;

    public PanelTableau(Controller controller, PanelGraphe panelGraphe)
    {
        this.controller  = controller;
        this.panelGraphe = panelGraphe;

        this.setLayout(new BorderLayout(8, 8));
        this.setBorder(BorderFactory.createTitledBorder("Saisie des arcs"));

        // Le modèle avec 3 colonnes, toutes les cellules sont éditables
        this.model = new DefaultTableModel(new Object[]{"Source", "Destination", "Poids"}, 0)
        {
            public boolean isCellEditable(int row, int column) { return true; }
        };

        this.table = new JTable(this.model);
        this.table.setFillsViewportHeight(true);
        this.add(new JScrollPane(this.table), BorderLayout.CENTER);

        JButton btnAjouter   = new JButton("+ Ligne");
        JButton btnSupprimer = new JButton("- Ligne");
        JButton btnCalculer  = new JButton("Calculer");
        JButton btnVider     = new JButton("Vider");

        this.cbOriente     = new JCheckBox("Orienté");
        this.cbAlgorithme  = new JComboBox<>(new String[]{"Dijkstra", "Bellman-Ford"});
        this.cbPointDepart = new JComboBox<>();
        this.cbPointDepart.setEditable(true);

        // Point d'arrivée => "Aucun" par défaut pour que ce soit optionnel
        this.cbPointArrivee = new JComboBox<>();
        this.cbPointArrivee.setEditable(true);

        // Le bouton chemin suivant est caché au départ, on le rend visible après le calcul
        this.btnCheminSuivant = new JButton("Chemin suivant (1/?)");
        this.btnCheminSuivant.setVisible(false);

        // Panel des contrôles en bas : 4 lignes x 3 colonnes
        JPanel panelActions = new JPanel(new GridLayout(4, 3, 8, 8));
        panelActions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelActions.setPreferredSize(new Dimension(10, 170));

        // Ligne 1 => + Ligne | - Ligne | Vider
        panelActions.add(btnAjouter);
        panelActions.add(btnSupprimer);
        panelActions.add(btnVider);

        // Ligne 2 => label Algorithme | combobox algo | case Orienté
        panelActions.add(new JLabel("Algorithme"));
        panelActions.add(this.cbAlgorithme);
        panelActions.add(this.cbOriente);

        // Ligne 3 => label Point de départ | combobox départ | case vide
        panelActions.add(new JLabel("Point de départ"));
        panelActions.add(this.cbPointDepart);
        panelActions.add(new JLabel(""));

        // Ligne 4 => label Point d'arrivée | combobox arrivée | bouton Calculer
        panelActions.add(new JLabel("Point d'arrivée (optionnel)"));
        panelActions.add(this.cbPointArrivee);
        panelActions.add(btnCalculer);

        // Le bouton chemin suivant est en haut, caché par défaut
        this.add(this.btnCheminSuivant, BorderLayout.NORTH);
        this.add(panelActions, BorderLayout.SOUTH);

        // Les actions des boutons
        btnAjouter.addActionListener(e   -> this.model.addRow(new Object[]{"", "", ""}));
        btnSupprimer.addActionListener(e ->
        {
            int row = this.table.getSelectedRow();
            if (row >= 0) this.model.removeRow(row);
        });
        btnCalculer.addActionListener(e      -> this.creerGraphe());
        btnVider.addActionListener(e         -> this.model.setRowCount(0));
        btnCheminSuivant.addActionListener(e -> this.afficherCheminSuivant());

        // À chaque modification du tableau, on met à jour les combobox
        this.model.addTableModelListener(e -> this.remplirPointDepart());

        // On ajoute une ligne vide au démarrage pour que le tableau ne soit pas vide
        this.model.addRow(new Object[]{"", "", ""});
    }

    // =========================================================================
    // Méthodes privées
    // =========================================================================

    /**
     * Lit la valeur d'une cellule du tableau et retourne une String propre.
     * Retourne "" si la cellule est null ou vide.
     */
    private String valeur(int row, int col)
    {
        Object v = this.model.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }

    /**
     * Met à jour les combobox de départ et d'arrivée avec les sommets du tableau.
     *
     * Appelée automatiquement à chaque modification du tableau (TableModelListener).
     * Vérifie aussi si un poids négatif impose Bellman-Ford.
     * Si c'est le cas => on sélectionne Bellman-Ford et on grise la combobox.
     * Dès que tous les poids redeviennent positifs => on réactive le choix.
     */
    private void remplirPointDepart()
    {
        // On réactive le choix par défaut avant de revérifier
        this.cbAlgorithme.setEnabled(true);

        Set<String> points = this.getNoeud();

        this.cbPointDepart.removeAllItems();

        // Pour l'arrivée on met "Aucun" en premier pour que ce soit optionnel
        this.cbPointArrivee.removeAllItems();
        this.cbPointArrivee.addItem("Aucun");

        for (String p : points)
        {
            this.cbPointDepart.addItem(p);
            this.cbPointArrivee.addItem(p);
        }

        // On parcourt les poids pour voir si un est négatif
        for (int cpt = 0; cpt < this.table.getRowCount(); cpt++)
        {
            Object value = this.table.getValueAt(cpt, 2);
            if (value == null || value.toString().trim().isEmpty()) continue;

            try
            {
                int poids = Integer.parseInt(value.toString().trim());
                if (poids < 0)
                {
                    // Poids négatif => on force Bellman-Ford et on grise la combobox
                    this.cbAlgorithme.setSelectedItem("Bellman-Ford");
                    this.cbAlgorithme.setEnabled(false);
                    break;
                }
            }
            catch (NumberFormatException e)
            {
                // La valeur n'est pas encore un nombre valide, on ignore
            }
        }

        this.repaint();
    }

    /**
     * Lit le tableau, crée le graphe dans le métier et lance le calcul.
     *
     * Après le calcul :
     *   - Si un point d'arrivée est choisi => on affiche directement ce chemin
     *   - Sinon => on affiche le bouton "Chemin suivant" et le premier chemin
     */
    public void creerGraphe()
    {
        Set<String>  noeud      = this.getNoeud();
        List<String> lstNoeud   = new ArrayList<>(noeud);
        boolean      estOriente = this.cbOriente.isSelected();

        this.controller.creerNoeuds(lstNoeud);

        for (int cpt = 0; cpt < this.model.getRowCount(); cpt++)
        {
            String valSource      = (String) this.model.getValueAt(cpt, 0);
            String valDestination = (String) this.model.getValueAt(cpt, 1);
            String valPoid        = (String) this.model.getValueAt(cpt, 2);

            // Si une cellule est vide, on signale l'erreur et on passe à la ligne suivante
            if (valSource.isEmpty() || valDestination.isEmpty() || valPoid.isEmpty())
            {
                JOptionPane.showMessageDialog(this,
                        "Certaines cellules sont vides à la ligne " + (cpt + 1),
                        "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            String source      = valSource.trim();
            String destination = valDestination.trim();
            int    poid;

            try
            {
                poid = Integer.parseInt(valPoid.trim());

                // Si on trouve un poids négatif, on force Bellman-Ford ici aussi
                // (au cas où remplirPointDepart n'aurait pas encore eu le temps de tourner)
                if (poid < 0)
                {
                    this.cbAlgorithme.setSelectedItem("Bellman-Ford");
                    this.cbAlgorithme.setEnabled(false);
                }
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(this,
                        "Poids invalide à la ligne " + (cpt + 1) + " : " + valPoid,
                        "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // On ajoute l'arc dans le graphe du métier
            this.controller.ajouterArc(source, destination, poid);

            // En non orienté => on ajoute aussi l'arc dans l'autre sens
            // SAUF si le poids est négatif, sinon ça créerait un circuit absorbant
            if (!estOriente && poid >= 0)
                this.controller.ajouterArc(destination, source, poid);
        }

        // On lit l'algo APRÈS la boucle pour être sûr que le bon algo est sélectionné
        String pointDepart = (String) this.cbPointDepart.getSelectedItem();
        String algo        = (String) this.cbAlgorithme.getSelectedItem();
        int    algoInt     = "Dijkstra".equals(algo) ? 0 : 1;

        this.controller.lancerCalcul(pointDepart, algoInt, estOriente);
        this.panelGraphe.afficherGraphes(estOriente);

        String pointArrivee = (String) this.cbPointArrivee.getSelectedItem();
        int    nbChemins    = this.controller.getNombreChemins();

        if (pointArrivee != null && !pointArrivee.isEmpty() && !pointArrivee.equals("Aucun"))
        {
            // Point d'arrivée choisi => on affiche directement ce chemin unique
            this.btnCheminSuivant.setVisible(false);
            this.controller.afficherCheminSuivant(estOriente, pointArrivee);
        }
        else if (nbChemins > 0)
        {
            // Pas de point d'arrivée => on affiche le bouton et le premier chemin direct
            this.btnCheminSuivant.setVisible(true);

            // On affiche le premier chemin directement sans attendre que l'utilisateur clique
            this.controller.afficherCheminSuivant(estOriente, null);

            // L'index a déjà été incrémenté par afficherCheminSuivant, donc on ajuste
            int indexActuel = this.controller.getIndexCheminActuel();
            int affiche     = (indexActuel == 0) ? nbChemins : indexActuel;
            this.btnCheminSuivant.setText("Chemin suivant (" + affiche + "/" + nbChemins + ")");
        }
    }

    /**
     * Appelée quand l'utilisateur clique sur "Chemin suivant".
     * Demande au controller d'afficher le prochain chemin et met à jour le bouton.
     */
    private void afficherCheminSuivant()
    {
        boolean estOriente = this.cbOriente.isSelected();
        int     nbChemins  = this.controller.getNombreChemins();

        this.controller.afficherCheminSuivant(estOriente, null);

        // On met à jour le numéro affiché sur le bouton
        int indexActuel = this.controller.getIndexCheminActuel();
        int affiche     = (indexActuel == 0) ? nbChemins : indexActuel;
        this.btnCheminSuivant.setText("Chemin suivant (" + affiche + "/" + nbChemins + ")");
    }

    /**
     * Récupère tous les noms de noeuds présents dans le tableau.
     * On utilise un LinkedHashSet pour éviter les doublons et garder l'ordre.
     */
    private Set<String> getNoeud()
    {
        Set<String> points = new LinkedHashSet<>();
        for (int cpt = 0; cpt < this.model.getRowCount(); cpt++)
        {
            String source      = this.valeur(cpt, 0);
            String destination = this.valeur(cpt, 1);
            if (!source.isEmpty())      points.add(source);
            if (!destination.isEmpty()) points.add(destination);
        }
        return points;
    }
}
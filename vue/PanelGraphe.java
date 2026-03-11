package vue;
import controller.Controller; 


import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/**
 * Vue graphique du graphe dans l'IHM Swing.
 *
 * Le panel ne manipule pas directement le modèle metier :
 * il récupère des données prêtes à afficher depuis le Controller.
 */
public class PanelGraphe extends JPanel
{
    private final Graph graph;
    private final Controller controller;

    public PanelGraphe(Controller controller)
    {
        this.controller = controller;
        // Force GraphStream à utiliser son backend Swing.
        System.setProperty("org.graphstream.ui", "swing");
        this.setLayout(new BorderLayout());

        graph = new SingleGraph("Graphe Orienté");

        // Le viewer est embarqué dans ce JPanel (pas de fenêtre externe graph.display()).
        Viewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();

        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);
        this.add(viewPanel, BorderLayout.CENTER);
    }

    /**
     * Reconstruit totalement l'affichage du graphe.
     *
     * @param estOriente true si le graphe doit être rendu orienté
     */
    public void afficherGraphes(boolean estOriente)
    {
        // On repart d'un graphe visuel vide à chaque rafraîchissement.
        graph.clear();

        // Style global des noeuds/arêtes et style spécial pour le plus court chemin.
        graph.setAttribute("ui.stylesheet",
            "node { size: 30px; fill-color: lightblue; text-size: 18; }" +
            "edge { arrow-size: 15px, 15px; fill-color: gray; text-size: 16; }" +
            "edge.shortest { fill-color: red; size: 3px; }"
        );

        // Ajouter tous les noeuds (données fournies par le controller)
        for (String nom : this.controller.getNomsNoeuds())
        {
            if (graph.getNode(nom) == null)
                graph.addNode(nom).setAttribute("ui.label", nom);
        }

        // Empêche le doublon visuel en non orienté (A-B et B-A).
        int indexArc = 0;
        Set<String> aretesNonOrientees = new HashSet<>();

        // Arêtes appartenant aux plus courts chemins calculés.
        Set<String> arcsPlusCourts = this.controller.getArcsPlusCourtsChemins();
        List<String[]> arcs = this.controller.getArcsPourAffichage();
        for (String[] arc : arcs)
        {
            String source = arc[0];
            String destination = arc[1];
            String poids = arc[2];

            if (!estOriente)
            {
            // Clé canonique pour traiter A-B et B-A comme la même arête.
                String a = source.compareTo(destination) <= 0 ? source : destination;
                String b = source.compareTo(destination) <= 0 ? destination : source;
                String cle = a + "|" + b + "|" + poids;
                if (!aretesNonOrientees.add(cle))
                    continue;
            }

            String idArc = source + "_" + destination + "_" + indexArc++;

            Edge edge = graph.addEdge(idArc, source, destination, estOriente);
            edge.setAttribute("ui.label", poids);

            String cleDirecte = source + "->" + destination;
            String cleInverse = destination + "->" + source;
            // En non orienté, on accepte aussi la correspondance inverse.
            if (arcsPlusCourts.contains(cleDirecte) || (!estOriente && arcsPlusCourts.contains(cleInverse)))
                edge.setAttribute("ui.class", "shortest");
        }
    }
}

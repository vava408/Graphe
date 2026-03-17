package vue;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/**
 * Panel qui affiche le graphe visuellement grâce à la bibliothèque GraphStream.
 *
 * Après chaque calcul, on reconstruit tout le graphe en gris avec afficherGraphes().
 * Ensuite, quand l'utilisateur navigue avec "Chemin suivant", on appelle afficherChemin()
 * qui colorie les arcs et noeuds du chemin avec une couleur choisie selon l'index.
 *
 * On a 8 couleurs disponibles => si il y a plus de 8 chemins, on boucle sur les couleurs.
 */
public class PanelGraphe extends JPanel
{
    // Les couleurs disponibles pour les chemins
    // On en a mis 8, c'est largement suffisant pour un graphe de cours
    private static final String[] COULEURS_CHEMINS = {
        "red", "blue", "green", "orange", "purple", "cyan", "magenta", "yellow"
    };

    // Le graphe GraphStream qui contient les noeuds et arcs à afficher
    private final Graph      graph;

    // Le contrôleur pour récupérer les données à afficher
    private final Controller controller;

    public PanelGraphe(Controller controller)
    {
        this.controller = controller;

        // On force GraphStream à utiliser le rendu Swing (et pas JavaFX)
        System.setProperty("org.graphstream.ui", "swing");
        this.setLayout(new BorderLayout());

        // MultiGraph => permet d'avoir plusieurs arcs entre les mêmes noeuds
        // C'est utile par exemple quand on a A=>B et B=>A en orienté
        this.graph = new MultiGraph("Graphe");

        Viewer    viewer    = new SwingViewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);
        this.add(viewPanel, BorderLayout.CENTER);
    }

    /**
     * Reconstruit le graphe visuellement depuis zéro avec tous les arcs en gris.
     * Appelé après chaque calcul pour repartir d'une base propre.
     *
     * @param estOriente => si true les arcs ont des flèches, sinon non
     */
    public void afficherGraphes(boolean estOriente)
    {
        // On repart d'un graphe vide à chaque fois
        this.graph.clear();
        this.appliquerStyle();

        // On ajoute tous les noeuds
        for (String nom : this.controller.getNomsNoeuds())
        {
            if (this.graph.getNode(nom) == null)
                this.graph.addNode(nom).setAttribute("ui.label", nom);
        }

        int         indexArc           = 0;
        Set<String> aretesNonOrientees = new HashSet<>();

        for (String[] arc : this.controller.getArcsPourAffichage())
        {
            String source      = arc[0];
            String destination = arc[1];
            String poids       = arc[2];

            // En mode non orienté, on évite d'afficher A-B et B-A comme deux arcs séparés
            // On crée une clé canonique (le plus petit nom en premier) pour détecter les doublons
            if (!estOriente)
            {
                String a   = source.compareTo(destination) <= 0 ? source : destination;
                String b   = source.compareTo(destination) <= 0 ? destination : source;
                String cle = a + "|" + b + "|" + poids;
                if (!aretesNonOrientees.add(cle)) continue;
            }

            String idArc = source + "_" + destination + "_" + indexArc++;
            Edge   edge  = this.graph.addEdge(idArc, source, destination, estOriente);
            edge.setAttribute("ui.label", poids);
        }
    }

    /**
     * Colorie les arcs et noeuds d'un chemin précis avec une couleur choisie.
     * Les arcs qui ne font pas partie du chemin repassent en gris.
     *
     * @param chemin       => la liste des noeuds dans l'ordre (ex: [A, C, E])
     * @param indexCouleur => l'index dans COULEURS_CHEMINS (0=rouge, 1=bleu, etc.)
     * @param estOriente   => pour accepter aussi la correspondance inverse en non orienté
     */
    public void afficherChemin(List<String> chemin, int indexCouleur, boolean estOriente)
    {
        if (chemin == null || chemin.size() < 2) return;

        // On choisit la couleur selon l'index, avec un modulo pour boucler si besoin
        String couleur = COULEURS_CHEMINS[indexCouleur % COULEURS_CHEMINS.length];

        // On construit l'ensemble des arcs du chemin sous forme "source->dest"
        // pour pouvoir vérifier rapidement si un arc en fait partie
        Set<String> arcsDuChemin = new HashSet<>();
        for (int i = 0; i < chemin.size() - 1; i++)
            arcsDuChemin.add(chemin.get(i) + "->" + chemin.get(i + 1));

        // On parcourt tous les arcs du graphe et on colorie selon si ils sont dans le chemin
        for (org.graphstream.graph.Edge edge : this.graph.edges().toList())
        {
            String src        = edge.getSourceNode().getId();
            String dest       = edge.getTargetNode().getId();
            String cle        = src + "->" + dest;
            String cleInverse = dest + "->" + src;

            if (arcsDuChemin.contains(cle) || (!estOriente && arcsDuChemin.contains(cleInverse)))
            {
                // Cet arc fait partie du chemin => on lui donne la couleur
                edge.setAttribute("ui.style",
                    "fill-color: " + couleur + "; size: 4px; arrow-size: 15px, 10px;");
            }
            else
            {
                // Cet arc n'est pas dans le chemin => on le remet en gris
                edge.setAttribute("ui.style",
                    "fill-color: gray; size: 2px; arrow-size: 15px, 10px;");
            }
        }

        // On colorie aussi les noeuds qui font partie du chemin
        for (org.graphstream.graph.Node node : this.graph.nodes().toList())
        {
            if (chemin.contains(node.getId()))
                node.setAttribute("ui.style",
                    "fill-color: " + couleur + "; size: 35px; text-size: 18;");
            else
                node.setAttribute("ui.style",
                    "fill-color: lightblue; size: 30px; text-size: 18;");
        }
    }

    /**
     * Applique le style de base au graphe => noeuds bleus, arcs gris.
     * Appelé au début de afficherGraphes() pour remettre tout à zéro.
     */
    private void appliquerStyle()
    {
        this.graph.setAttribute("ui.stylesheet",
            "node { size: 30px; fill-color: lightblue; text-size: 18; }" +
            "edge { arrow-size: 15px, 10px; fill-color: gray; text-size: 16; size: 2px; }"
        );
    }
}
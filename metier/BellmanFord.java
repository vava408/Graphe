package metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de Bellman-Ford.
 *
 * Le principe : on répète (n-1) fois un passage sur TOUS les arcs du graphe
 * pour essayer d'améliorer les distances. À chaque passage on "relaxe" les arcs,
 * c'est-à-dire qu'on vérifie si passer par cet arc donne une meilleure distance.
 *
 * Pourquoi (n-1) fois ? Parce que le plus long chemin possible sans cycle
 * a au maximum (n-1) arcs (n = nombre de noeuds). Donc après (n-1) passages,
 * toutes les distances sont forcément stables si il n'y a pas de cycle négatif.
 *
 * Avantage par rapport à Dijkstra : ça marche avec les poids négatifs.
 * Inconvénient : c'est plus lent (O(n*m) contre O(m*log(n)) pour Dijkstra).
 *
 * Si après (n-1) itérations on peut encore améliorer une distance, c'est qu'il
 * y a un circuit absorbant (cycle dont la somme des poids est négative).
 */
public class BellmanFord implements IAlgorithme
{
    public String getNom()
    {
        return "Bellman-Ford";
    }

    public Resultat calculer(Graphe graphe, String nomSource)
    {
        Noeud source = graphe.getNoeud(nomSource);
        if (source == null) return null;

        int      n        = graphe.getNombreNoeuds();
        Resultat resultat = new Resultat(nomSource);

        Map<String, Integer> dist         = new HashMap<>();
        Map<String, String>  predecesseur = new HashMap<>();

        // --- Initialisation ---
        // Au départ on ne connaît aucun chemin donc tout est à l'infini,
        // sauf la source qui est à 0 (distance d'un point à lui-même)
        for (Noeud noeud : graphe.getNoeuds())
        {
            dist.put(noeud.getNom(), Integer.MAX_VALUE);
            predecesseur.put(noeud.getNom(), null);
        }
        dist.put(nomSource, 0);

        resultat.ajouterLog("=== Initialisation ===");
        resultat.ajouterLog(this.distancesEnLigne(dist, graphe));
        resultat.ajouterLog("");

        // --- Relaxations ---
        // On fait (n-1) passages sur tous les arcs
        for (int i = 0; i < n - 1; i++)
        {
            boolean modifie = false;

            resultat.ajouterLog("--- Itération " + (i + 1) + " ---");

            for (Noeud noeud : graphe.getNoeuds())
            {
                String nomNoeud = noeud.getNom();

                // Si ce noeud est encore inaccessible, ses arcs ne peuvent
                // pas améliorer quoi que ce soit
                if (dist.get(nomNoeud) == Integer.MAX_VALUE) continue;

                for (Arc arc : noeud.getArcsSortants())
                {
                    String nomVoisin        = arc.getDestination().getNom();
                    int    distActuelle     = dist.get(nomVoisin);
                    int    nouvelleDistance = dist.get(nomNoeud) + arc.getPoids();

                    String avant = (distActuelle == Integer.MAX_VALUE)
                                   ? "∞" : String.valueOf(distActuelle);

                    // Si on trouve un chemin plus court, on met à jour
                    if (nouvelleDistance < distActuelle)
                    {
                        dist.put(nomVoisin, nouvelleDistance);
                        predecesseur.put(nomVoisin, nomNoeud);
                        modifie = true;
                        resultat.ajouterLog("  " + nomNoeud + "->" + nomVoisin
                                + " (poids " + arc.getPoids() + ")"
                                + "  " + avant + " => " + nouvelleDistance + "  ✓");
                    }
                }
            }

            // On affiche les distances après cette itération
            resultat.ajouterLog("  " + this.distancesEnLigne(dist, graphe));

            // Optimisation : si rien n'a bougé, les distances sont stables,
            // inutile de continuer les itérations suivantes
            if (!modifie)
            {
                resultat.ajouterLog("  Stable, arrêt à l'itération " + (i + 1) + ".");
                break;
            }
            resultat.ajouterLog("");
        }

        resultat.ajouterLog("");

        // --- Détection du circuit absorbant ---
        // On fait un passage de plus. Si une distance peut encore être améliorée,
        // c'est qu'il y a un cycle dont la somme des poids est négative.
        // Ce cycle est appelé "circuit absorbant" et rend le problème impossible
        // à résoudre (on pourrait tourner en boucle pour aller vers -infini).
        resultat.ajouterLog("=== Détection circuit absorbant ===");

        String noeudDansLeCycle = null;

        for (Noeud noeud : graphe.getNoeuds())
        {
            int distNoeud = dist.get(noeud.getNom());

            // Un noeud inaccessible ne peut pas faire partie d'un circuit
            // détectable depuis la source
            if (distNoeud == Integer.MAX_VALUE) continue;

            for (Arc arc : noeud.getArcsSortants())
            {
                String nomVoisin  = arc.getDestination().getNom();
                int    distVoisin = dist.get(nomVoisin);

                if (distNoeud + arc.getPoids() < distVoisin)
                {
                    // On met à jour le prédécesseur pour pointer dans le cycle
                    predecesseur.put(nomVoisin, noeud.getNom());
                    noeudDansLeCycle = nomVoisin;
                    break;
                }
            }
            if (noeudDansLeCycle != null) break;
        }

        if (noeudDansLeCycle != null)
        {
            // On remonte n fois depuis ce noeud pour être sûr d'être
            // à l'intérieur du cycle (et pas juste sur un chemin qui y mène)
            String pointDansCycle = noeudDansLeCycle;
            for (int k = 0; k < n; k++)
                pointDansCycle = predecesseur.get(pointDansCycle);

            // On reconstruit le cycle en suivant les prédécesseurs
            // jusqu'à retomber sur le point de départ
            List<String> cycle = new ArrayList<>();
            String       cur   = pointDansCycle;

            do
            {
                cycle.add(cur);
                cur = predecesseur.get(cur);
            }
            while (!cur.equals(pointDansCycle));

            // On ferme le cycle et on inverse pour avoir le bon sens de lecture
            cycle.add(pointDansCycle);
            Collections.reverse(cycle);

            int    sommePoids  = this.calculerSommeCycle(cycle, graphe);
            String detailSomme = this.detailSommeCycle(cycle, graphe);

            resultat.ajouterLog("  Circuit absorbant détecté !");
            resultat.ajouterLog("  Cycle : " + String.join(" -> ", cycle));
            resultat.ajouterLog("  Somme des poids : " + detailSomme + " = " + sommePoids);
            resultat.ajouterLog("  => Somme négative (" + sommePoids
                    + ") : le plus court chemin n'existe pas.");

            // On signale le problème dans le résultat et on le retourne quand même
            // pour que les logs soient affichés dans le terminal
            resultat.signalerCircuitAbsorbant();
            return resultat;
        }

        resultat.ajouterLog("  Aucun circuit absorbant détecté.");
        resultat.ajouterLog("");

        // --- Construction du résultat final ---
        for (Noeud noeud : graphe.getNoeuds())
        {
            String nom = noeud.getNom();
            resultat.setDistance(nom, dist.get(nom));
            resultat.setChemin(nom, this.reconstruireChemin(predecesseur, nomSource, nom));
        }

        return resultat;
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================

    /**
     * Affiche toutes les distances sur une seule ligne compacte.
     * Ex : "A=0  B=12  C=∞  E=27"
     * On utilise ∞ pour Integer.MAX_VALUE qui représente "inaccessible".
     */
    private String distancesEnLigne(Map<String, Integer> dist, Graphe graphe)
    {
        StringBuilder sb = new StringBuilder();
        for (Noeud noeud : graphe.getNoeuds())
        {
            String nom     = noeud.getNom();
            String distStr = (dist.get(nom) == Integer.MAX_VALUE)
                             ? "∞" : String.valueOf(dist.get(nom));
            sb.append(nom).append("=").append(distStr).append("  ");
        }
        return sb.toString().trim();
    }

    /**
     * Calcule la somme des poids des arcs qui forment le cycle.
     * Le cycle est une liste de noeuds où le dernier est égal au premier.
     */
    private int calculerSommeCycle(List<String> cycle, Graphe graphe)
    {
        int somme = 0;
        for (int i = 0; i < cycle.size() - 1; i++)
        {
            Noeud noeud = graphe.getNoeud(cycle.get(i));
            for (Arc arc : noeud.getArcsSortants())
            {
                if (arc.getDestination().getNom().equals(cycle.get(i + 1)))
                {
                    somme += arc.getPoids();
                    break;
                }
            }
        }
        return somme;
    }

    /**
     * Retourne le détail de la somme sous forme lisible pour l'affichage.
     * Ex : "(-48) + 5 + 6"
     * Les poids négatifs sont mis entre parenthèses pour la lisibilité.
     */
    private String detailSommeCycle(List<String> cycle, Graphe graphe)
    {
        List<String> parties = new ArrayList<>();
        for (int i = 0; i < cycle.size() - 1; i++)
        {
            Noeud noeud = graphe.getNoeud(cycle.get(i));
            for (Arc arc : noeud.getArcsSortants())
            {
                if (arc.getDestination().getNom().equals(cycle.get(i + 1)))
                {
                    int p = arc.getPoids();
                    parties.add(p < 0 ? "(" + p + ")" : String.valueOf(p));
                    break;
                }
            }
        }
        return String.join(" + ", parties);
    }

    /**
     * Remonte les prédécesseurs pour reconstruire le chemin de la source
     * jusqu'à la destination. Retourne une liste vide si inaccessible.
     */
    private List<String> reconstruireChemin(Map<String, String> predecesseur,
                                             String source, String destination)
    {
        List<String> chemin  = new ArrayList<>();
        String       courant = destination;

        while (courant != null)
        {
            chemin.add(0, courant);
            courant = predecesseur.get(courant);
        }

        if (chemin.isEmpty() || !chemin.get(0).equals(source))
            return Collections.emptyList();

        return chemin;
    }
}
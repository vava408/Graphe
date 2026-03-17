package metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de Dijkstra.
 *
 * Le principe : à chaque étape, on choisit le noeud non encore traité
 * qui a la plus petite distance connue depuis la source. On regarde ensuite
 * tous ses voisins et on met à jour leurs distances si on trouve mieux.
 * Une fois un noeud traité, sa distance est définitive (on n'y revient plus).
 *
 * C'est pour ça qu'on utilise une PriorityQueue (file de priorité) :
 * elle nous donne toujours le noeud avec la plus petite distance en premier.
 *
 * Pourquoi ça ne marche pas avec les poids négatifs ?
 * Parce qu'on suppose que quand on sort un noeud de la file, sa distance
 * est définitive. Avec des poids négatifs, un arc ultérieur pourrait
 * encore améliorer cette distance — l'hypothèse de base ne tient plus.
 */
public class Dijkstra implements IAlgorithme
{
    public String getNom()
    {
        return "Dijkstra";
    }

    public Resultat calculer(Graphe graphe, String nomSource)
    {
        Noeud source = graphe.getNoeud(nomSource);
        if (source == null) return null;

        Resultat resultat = new Resultat(nomSource);

        // Distance minimale connue pour chaque noeud
        Map<String, Integer> dist         = new HashMap<>();
        // Le noeud par lequel on est passé pour arriver à chaque noeud
        Map<String, String>  predecesseur = new HashMap<>();
        // Les noeuds dont la distance est définitive (déjà traités)
        Set<String>          visite       = new HashSet<>();

        // --- Initialisation ---
        // Tout le monde à l'infini sauf la source à 0
        for (Noeud noeud : graphe.getNoeuds())
        {
            dist.put(noeud.getNom(), Integer.MAX_VALUE);
            predecesseur.put(noeud.getNom(), null);
        }
        dist.put(nomSource, 0);

        resultat.ajouterLog("=== Initialisation ===");
        resultat.ajouterLog(this.distancesEnLigne(dist, visite, graphe));
        resultat.ajouterLog("");

        // File de priorité : on traite toujours le noeud avec la plus petite distance
        // Chaque élément est un tableau {distance, nomNoeud}
        PriorityQueue<String[]> pq = new PriorityQueue<>(
            Comparator.comparingInt(e -> Integer.parseInt(e[0]))
        );
        pq.offer(new String[]{"0", nomSource});

        int etape = 1;

        while (!pq.isEmpty())
        {
            String[] courant      = pq.poll();
            int      distCourante = Integer.parseInt(courant[0]);
            String   nomCourant   = courant[1];

            // Si déjà visité, c'est une entrée obsolète dans la file — on ignore
            if (visite.contains(nomCourant)) continue;
            visite.add(nomCourant);

            resultat.ajouterLog("--- Étape " + etape + " : " + nomCourant
                    + " (dist=" + distCourante + ", définitif) ---");

            Noeud noeudCourant = graphe.getNoeud(nomCourant);

            // On regarde tous les voisins et on essaie d'améliorer leurs distances
            for (Arc arc : noeudCourant.getArcsSortants())
            {
                String nomVoisin        = arc.getDestination().getNom();
                int    distActuelle     = dist.get(nomVoisin);
                int    nouvelleDistance = distCourante + arc.getPoids();

                // Un noeud déjà visité a une distance définitive, on ne le retouche pas
                if (visite.contains(nomVoisin)) continue;

                String avant = (distActuelle == Integer.MAX_VALUE)
                               ? "∞" : String.valueOf(distActuelle);

                if (nouvelleDistance < distActuelle)
                {
                    dist.put(nomVoisin, nouvelleDistance);
                    predecesseur.put(nomVoisin, nomCourant);
                    // On remet le voisin dans la file avec sa nouvelle distance
                    pq.offer(new String[]{String.valueOf(nouvelleDistance), nomVoisin});
                    resultat.ajouterLog("  " + nomCourant + "->" + nomVoisin
                            + " (" + arc.getPoids() + ")"
                            + "  " + avant + " => " + nouvelleDistance + "  ✓");
                }
            }

            // État des distances après cette étape
            // Les noeuds marqués * ont une distance définitive
            resultat.ajouterLog("  " + this.distancesEnLigne(dist, visite, graphe));
            resultat.ajouterLog("");

            etape++;
        }

        resultat.ajouterLog("=== Terminé ===");
        resultat.ajouterLog(this.distancesEnLigne(dist, visite, graphe));

        // --- Construction du résultat final ---
        for (Noeud noeud : graphe.getNoeuds())
        {
            String nom = noeud.getNom();
            resultat.setDistance(nom, dist.get(nom));
            resultat.setChemin(nom, this.reconstruireChemin(predecesseur, nomSource, nom));
        }

        return resultat;
    }

    /**
     * Affiche toutes les distances sur une ligne.
     * Les noeuds avec une distance définitive sont marqués avec *.
     * Ex : "A=0*  B=12*  C=27  E=∞"
     */
    private String distancesEnLigne(Map<String, Integer> dist,
                                     Set<String> visite, Graphe graphe)
    {
        StringBuilder sb = new StringBuilder();
        for (Noeud noeud : graphe.getNoeuds())
        {
            String nom     = noeud.getNom();
            String distStr = (dist.get(nom) == Integer.MAX_VALUE)
                             ? "∞" : String.valueOf(dist.get(nom));
            String marque  = visite.contains(nom) ? "*" : "";
            sb.append(nom).append("=").append(distStr).append(marque).append("  ");
        }
        return sb.toString().trim();
    }

    /**
     * Remonte les prédécesseurs pour reconstruire le chemin complet
     * de la source jusqu'à la destination.
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
package metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de Dijkstra.
 * Fonctionne sur les graphes à poids positifs uniquement.
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

        // Distance de chaque noeud (infini au départ)
        Map<String, Integer> dist = new HashMap<>();
        // Prédécesseur de chaque noeud (pour reconstruire le chemin)
        Map<String, String> predecesseur = new HashMap<>();
        // Noeuds déjà traités
        Set<String> visite = new HashSet<>();

        // Initialisation : tous à +infini sauf la source
        for (Noeud n : graphe.getNoeuds()) 
        {
            dist.put(n.getNom(), Integer.MAX_VALUE);
            predecesseur.put(n.getNom(), null);
        }
        dist.put(nomSource, 0);

        // File de priorité : traite d'abord le noeud avec la plus petite distance
        // Entry : (distance, nomNoeud)
        PriorityQueue<int[]> file = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        // On utilise un Map<nom, index> pour simplifier : on stocke (dist, hash du nom)
        // Approche simple : stocker le nom dans une liste parallèle
        Map<Integer, String> indexNom = new HashMap<>();

        // On utilise une PriorityQueue de String[] {distStr, nom} — plus lisible
        PriorityQueue<String[]> pq = new PriorityQueue<>(
            Comparator.comparingInt(e -> Integer.parseInt(e[0]))
        );
        pq.offer(new String[]{"0", nomSource});

        while (!pq.isEmpty()) 
        {
            String[] courant = pq.poll();
            int distCourante = Integer.parseInt(courant[0]);
            String nomCourant = courant[1];

            // Si déjà visité, on ignore (entrée obsolète dans la PQ)
            if (visite.contains(nomCourant)) continue;
            visite.add(nomCourant);

            Noeud noeudCourant = graphe.getNoeud(nomCourant);

            // Relaxation de chaque arc sortant
            for (Arc arc : noeudCourant.getArcsSortants()) 
            {
                String nomVoisin = arc.getDestination().getNom();
                if (visite.contains(nomVoisin)) continue;

                int nouvelleDistance = distCourante + arc.getPoids();
                if (nouvelleDistance < dist.get(nomVoisin)) 
                {
                    dist.put(nomVoisin, nouvelleDistance);
                    predecesseur.put(nomVoisin, nomCourant);
                    pq.offer(new String[]{String.valueOf(nouvelleDistance), nomVoisin});
                }
            }
        }

        // Construction du résultat final
        for (Noeud n : graphe.getNoeuds()) 
        {
            String nom = n.getNom();
            resultat.setDistance(nom, dist.get(nom));
            resultat.setChemin(nom, reconstruireChemin(predecesseur, nomSource, nom));
        }

        return resultat;
    }

    /**
     * Remonte les prédécesseurs pour reconstruire le chemin source -> destination.
     */
    private List<String> reconstruireChemin(Map<String, String> predecesseur,
                                             String source, String destination) 
    {
        List<String> chemin = new ArrayList<>();
        String courant = destination;

        while (courant != null) 
        {
            chemin.add(0, courant);
            courant = predecesseur.get(courant);
        }

        // Si le chemin ne commence pas par la source, c'est inatteignable
        if (chemin.isEmpty() || !chemin.get(0).equals(source)) 
        {
            return Collections.emptyList();
        }

        return chemin;
    }
}

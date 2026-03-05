package metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de Bellman-Ford.
 * Fonctionne même avec des poids négatifs.
 * Détecte les cycles de poids négatif.
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

        int n = graphe.getNombreNoeuds();
        Resultat resultat = new Resultat(nomSource);

        // Distance de chaque noeud (infini au départ)
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> predecesseur = new HashMap<>();

        for (Noeud noeud : graphe.getNoeuds()) 
        {
            dist.put(noeud.getNom(), Integer.MAX_VALUE);
            predecesseur.put(noeud.getNom(), null);
        }
        dist.put(nomSource, 0);

        // Relaxation : on répète (n-1) fois sur tous les arcs
        for (int i = 0; i < n - 1; i++) 
        {
            boolean modifie = false;

            for (Noeud noeud : graphe.getNoeuds()) 
            {
                String nomNoeud = noeud.getNom();
                if (dist.get(nomNoeud) == Integer.MAX_VALUE) continue;

                for (Arc arc : noeud.getArcsSortants()) 
                {
                    String nomVoisin = arc.getDestination().getNom();
                    int nouvelleDistance = dist.get(nomNoeud) + arc.getPoids();

                    if (nouvelleDistance < dist.get(nomVoisin))  
                    {
                        dist.put(nomVoisin, nouvelleDistance);
                        predecesseur.put(nomVoisin, nomNoeud);
                        modifie = true;
                    }
                }
            }

            // Optimisation : si aucune modification, on peut s'arrêter
            if (!modifie) break;
        }

        // Détection de cycle négatif : si on peut encore relaxer, cycle détecté
        for (Noeud noeud : graphe.getNoeuds()) 
        {
            if (dist.get(noeud.getNom()) == Integer.MAX_VALUE) continue;
            for (Arc arc : noeud.getArcsSortants()) 
            {
                String nomVoisin = arc.getDestination().getNom();
                if (dist.get(noeud.getNom()) + arc.getPoids() < dist.get(nomVoisin)) 
                {
                    // Cycle négatif détecté : on retourne null pour signaler l'erreur
                    return null;
                }
            }
        }

        // Construction du résultat
        for (Noeud noeud : graphe.getNoeuds()) 
        {
            String nom = noeud.getNom();
            resultat.setDistance(nom, dist.get(nom));
            resultat.setChemin(nom, reconstruireChemin(predecesseur, nomSource, nom));
        }

        return resultat;
    }

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

        if (chemin.isEmpty() || !chemin.get(0).equals(source)) 
        {
            return Collections.emptyList();
        }

        return chemin;
    }
}

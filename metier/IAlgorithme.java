package metier;

/**
 * Interface commune pour tous les algorithmes de plus court chemin.
 * Pratique pour les utiliser de manière interchangeable dans le contrôleur.
 */
public interface IAlgorithme 
{

    /**
     * Calcule les plus courts chemins depuis le noeud source.
     * @param graphe  Le graphe sur lequel travailler
     * @param source  Le nom du noeud de départ
     * @return        Un Resultat contenant distances et chemins, ou null si erreur
     */
    Resultat calculer(Graphe graphe, String source);

    /**
     * Retourne le nom de l'algorithme (pour affichage).
     */
    String getNom();
}

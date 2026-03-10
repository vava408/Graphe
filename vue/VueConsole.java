package vue;

import metier.Resultat;

import java.util.*;

/**
 * Vue console de l'application.
 *
 * Gère toute l'interaction texte avec l'utilisateur : affichage de messages,
 * saisie des noeuds, des arcs, du noeud source, et affichage du résultat final.
 *
 * Fonctionne dans deux modes selon le constructeur utilisé :
 *
 *   new VueConsole()
 *     => Mode console système : lit sur System.in, écrit sur System.out.
 *       Utilisé quand l'application est lancée avec l'argument "console".
 *
 *   new VueConsole(PanelTerminal)
 *     => Mode terminal intégré : lit et écrit dans le PanelTerminal Swing.
 *       Utilisé quand l'utilisateur coche "Terminal intégré" dans le menu Options.
 *
 * Les méthodes privées print(), println() et nextLine() abstraient la différence
 * entre les deux modes : tout le reste du code est identique.
 */
public class VueConsole
{
    /**
     * Scanner sur System.in — utilisé uniquement en mode console système.
     * null en mode terminal intégré.
     */
    private final Scanner       scanner;

    /**
     * Terminal Swing intégré — utilisé uniquement en mode terminal intégré.
     * null en mode console système.
     */
    private final PanelTerminal terminal;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    /**
     * Mode console système.
     * Lit sur System.in et écrit sur System.out / System.err.
     * Sans terminal intégré, elle lit/écrit dans le vrai terminal système
     */
    public VueConsole()
    {
        this.scanner  = new Scanner(System.in);
        this.terminal = null;
    }

    /**
     * Mode terminal intégré dans l'IHM Swing.
     * Toutes les entrées/sorties passent par le PanelTerminal fourni.
     * 
     * Enn claire => Avec terminal intégré, elle doit lire/écrire dans le PanelTerminal Swing 
     *
     * @param terminal Le terminal Swing dans lequel lire et écrire
     */
    public VueConsole(PanelTerminal terminal)
    {
        this.terminal = terminal;
        this.scanner  = null;
    }

    // =========================================================================
    // Méthodes d'entrée/sortie internes (abstraction des deux modes)
    // =========================================================================

    /**
     * Écrit du texte sans saut de ligne (utilisé pour les prompts).
     */
    private void print(String texte)
    {
        if (terminal != null)
            terminal.afficher(texte);
        else
            System.out.print(texte);
    }

    /**
     * Écrit une ligne avec saut de ligne.
     */
    private void println(String texte)
    {
        if (terminal != null)
            terminal.afficherLigne(texte);
        else
            System.out.println(texte);
    }

    /**
     * Lit une ligne saisie par l'utilisateur.
     * Bloquant dans les deux modes : attend que l'utilisateur appuie sur Entrée.
     */
    private String nextLine()
    {
        if (terminal != null)
            return terminal.readLine();
        else
            return scanner.nextLine().trim();
    }

    // =========================================================================
    // Messages
    // =========================================================================

    /**
     * Affiche un message d'information préfixé par [INFO].
     *
     * @param message Le message à afficher
     */
    public void afficherMessage(String message)
    {
        println("[INFO] " + message);
    }

    /**
     * Affiche un message d'erreur préfixé par [ERREUR].
     *
     * @param erreur Le message d'erreur à afficher
     */
    public void afficherErreur(String erreur)
    {
        println("[ERREUR] " + erreur);
    }

    // =========================================================================
    // Saisies interactives
    // =========================================================================

    /**
     * Affiche la liste des algorithmes disponibles et demande à l'utilisateur
     * d'en choisir un par son numéro.
     *
     * @param algorithmes Liste des noms d'algorithmes à proposer
     * @return L'index (0-based) de l'algorithme choisi
     */
    public int demanderChoixAlgorithme(List<String> algorithmes)
    {
        println("\n=== Choix de l'algorithme ===");
        for (int i = 0; i < algorithmes.size(); i++)
            println("  " + (i + 1) + ". " + algorithmes.get(i));

        int choix = -1;
        while (choix < 0 || choix >= algorithmes.size())
        {
            print("Votre choix (1-" + algorithmes.size() + ") : ");
            try
            {
                choix = Integer.parseInt(nextLine()) - 1;
                if (choix < 0 || choix >= algorithmes.size())
                    println("Choix invalide, réessayez.");
            }
            catch (NumberFormatException e)
            {
                println("Veuillez entrer un nombre.");
            }
        }
        return choix;
    }

    /**
     * Demande la liste des noms de noeuds séparés par des virgules.
     * Propose une confirmation et recommence si l'utilisateur répond "n".
     *
     * @return Liste non vide des noms de noeuds confirmés
     */
    public List<String> demanderNoeuds()
    {
        println("\n=== Saisie des noeuds ===");
        print("Noms séparés par des virgules (ex: A,B,C) : ");

        String[] parts = nextLine().split(",");
        List<String> noeuds = new ArrayList<>();
        for (String part : parts)
        {
            String nom = part.trim();
            if (!nom.isEmpty()) noeuds.add(nom);
        }

        println("Noeuds saisis : " + noeuds);
        print("Confirmer ? (o/n) : ");

        // Récursion si l'utilisateur veut recommencer
        if (nextLine().equalsIgnoreCase("n"))
            return demanderNoeuds();

        return noeuds;
    }

    /**
     * Demande si le graphe est orienté ou non.
     *
     * @return true = orienté, false = non orienté
     */
    public boolean demanderSiOriente()
    {
        print("\nGraphe orienté ? (o/n) : ");
        return nextLine().equalsIgnoreCase("o");
    }

    /**
     * Demande les arcs sortants pour un noeud donné.
     * L'utilisateur saisit une destination et un poids, puis "fin" pour terminer.
     *
     * @param nomNoeud     Le noeud source dont on saisit les arcs
     * @param noeudsDispos Les noeuds vers lesquels un arc est autorisé
     * @return Liste de tableaux [nomDestination, poids] sous forme de String[]
     */
    public List<String[]> demanderArcsPourNoeud(String nomNoeud, List<String> noeudsDispos)
    {
        println("\n=== Arcs sortants de " + nomNoeud + " ===");
        println("Noeuds disponibles : " + noeudsDispos);
        println("(Tapez 'fin' pour passer au noeud suivant)");

        List<String[]> lstArcs = new ArrayList<>();

        while (true)
        {
            print("  Destination depuis " + nomNoeud + " (ou 'fin') : ");
            String destination = nextLine();

            if (destination.equalsIgnoreCase("fin")) break;

            if (!noeudsDispos.contains(destination))
            {
                println("  Noeud inconnu. Choisissez parmi : " + noeudsDispos);
                continue;
            }
            if (destination.equals(nomNoeud))
            {
                println("  Arc vers soi-même ignoré.");
                continue;
            }

            print("  Poids de l'arc " + nomNoeud + " -> " + destination + " : ");
            String poids = nextLine();

            try
            {
                Integer.parseInt(poids); // validation — exception si non numérique
                lstArcs.add(new String[]{destination, poids});
                println("  Arc ajouté : " + nomNoeud + " -> " + destination + " (poids: " + poids + ")");
            }
            catch (NumberFormatException e)
            {
                println("  Poids invalide, arc ignoré.");
            }
        }

        return lstArcs;
    }

    /**
     * Demande le noeud de départ pour le calcul parmi les noeuds disponibles.
     *
     * @param noeudsDispos Liste des noeuds existants dans le graphe
     * @return Le nom du noeud source choisi
     */
    public String demanderNoeudSource(List<String> noeudsDispos)
    {
        println("\n=== Noeud source ===");
        println("Noeuds disponibles : " + noeudsDispos);

        String source = "";
        while (!noeudsDispos.contains(source))
        {
            print("Noeud de départ : ");
            source = nextLine();
            if (!noeudsDispos.contains(source))
                println("Noeud inconnu, réessayez.");
        }
        return source;
    }

    // =========================================================================
    // Affichage du résultat
    // =========================================================================

    /**
     * Affiche le tableau des résultats : pour chaque noeud, sa distance minimale
     * depuis la source et le chemin emprunté.
     *
     * @param resultat Le résultat produit par l'algorithme
     * @param nomAlgo  Le nom de l'algorithme utilisé (pour l'en-tête)
     */
    public void afficherResultat(Resultat resultat, String nomAlgo)
    {
        println("\n========================================");
        println("  Résultats - " + nomAlgo);
        println("  Source : " + resultat.getNomSource());
        println("========================================");
        println(String.format("%-10s %-10s %s", "Noeud", "Distance", "Chemin"));
        println("----------------------------------------");

        for (Map.Entry<String, Integer> entry : resultat.getDistances().entrySet())
        {
            String       nom    = entry.getKey();
            int          dist   = entry.getValue();
            List<String> chemin = resultat.getChemin(nom);

            // Integer.MAX_VALUE représente "infini" (noeud inaccessible)
            String distStr   = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
            String cheminStr = (chemin == null || chemin.isEmpty())
                               ? "Inaccessible"
                               : String.join(" -> ", chemin);

            println(String.format("%-10s %-10s %s", nom, distStr, cheminStr));
        }
        println("========================================");
    }
}
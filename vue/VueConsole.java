package vue;

import metier.Resultat;

import java.util.*;

/**
 * Gère toute l'interaction texte avec l'utilisateur.
 *
 * Elle fonctionne dans deux modes selon le constructeur utilisé :
 *
 *   new VueConsole()
 *     => Mode console système : lit sur System.in, écrit sur System.out.
 *        Utilisé quand on lance l'application avec l'argument "console".
 *
 *   new VueConsole(PanelTerminal)
 *     => Mode terminal intégré : lit et écrit dans le PanelTerminal Swing.
 *        Utilisé quand on coche "Terminal de saisie" dans le menu.
 *
 * Les méthodes privées print(), println() et nextLine() cachent la différence
 * entre les deux modes => tout le reste du code est identique dans les deux cas.
 */
public class VueConsole
{
    // Le scanner sur System.in, utilisé uniquement en mode console système
    // null en mode terminal intégré
    private final Scanner       scanner;

    // Le terminal Swing, utilisé uniquement en mode terminal intégré
    // null en mode console système
    private final PanelTerminal terminal;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    /**
     * Mode console système => lit/écrit dans le vrai terminal.
     */
    public VueConsole()
    {
        this.scanner  = new Scanner(System.in);
        this.terminal = null;
    }

    /**
     * Mode terminal intégré => lit/écrit dans le PanelTerminal Swing.
     *
     * @param terminal => le panel terminal dans lequel on va lire et écrire
     */
    public VueConsole(PanelTerminal terminal)
    {
        this.terminal = terminal;
        this.scanner  = null;
    }

    // =========================================================================
    // Méthodes internes (abstraction des deux modes)
    // =========================================================================

    // Écrit du texte sans saut de ligne (pour les prompts)
    private void print(String texte)
    {
        if (this.terminal != null)
            this.terminal.afficher(texte);
        else
            System.out.print(texte);
    }

    // Écrit une ligne avec saut de ligne
    private void println(String texte)
    {
        if (this.terminal != null)
            this.terminal.afficherLigne(texte);
        else
            System.out.println(texte);
    }

    // Lit une ligne saisie par l'utilisateur (bloquant dans les deux modes)
    private String nextLine()
    {
        if (this.terminal != null)
            return this.terminal.readLine();
        else
            return this.scanner.nextLine().trim();
    }

    // =========================================================================
    // Messages
    // =========================================================================

    /**
     * Affiche un message d'information préfixé par [INFO].
     */
    public void afficherMessage(String message)
    {
        this.println("[INFO] " + message);
    }

    /**
     * Affiche un message d'erreur préfixé par [ERREUR].
     */
    public void afficherErreur(String erreur)
    {
        this.println("[ERREUR] " + erreur);
    }

    // =========================================================================
    // Saisies interactives
    // =========================================================================

    /**
     * Affiche la liste des algorithmes et demande à l'utilisateur d'en choisir un.
     *
     * @param algorithmes => la liste des noms à proposer
     * @return l'index (0-based) de l'algorithme choisi
     */
    public int demanderChoixAlgorithme(List<String> algorithmes)
    {
        this.println("\n=== Choix de l'algorithme ===");
        for (int i = 0; i < algorithmes.size(); i++)
            this.println("  " + (i + 1) + ". " + algorithmes.get(i));

        int choix = -1;
        while (choix < 0 || choix >= algorithmes.size())
        {
            this.print("Votre choix (1-" + algorithmes.size() + ") : ");
            try
            {
                choix = Integer.parseInt(this.nextLine()) - 1;
                if (choix < 0 || choix >= algorithmes.size())
                    this.println("Choix invalide, réessayez.");
            }
            catch (NumberFormatException e)
            {
                this.println("Veuillez entrer un nombre.");
            }
        }
        return choix;
    }

    /**
     * Demande à l'utilisateur de saisir les noeuds séparés par des virgules.
     * Propose une confirmation et recommence si l'utilisateur répond "n".
     *
     * @return la liste des noms de noeuds confirmée
     */
    public List<String> demanderNoeuds()
    {
        this.println("\n=== Saisie des noeuds ===");
        this.print("Noms séparés par des virgules (ex: A,B,C) : ");

        String[]     parts  = this.nextLine().split(",");
        List<String> noeuds = new ArrayList<>();

        for (String part : parts)
        {
            String nom = part.trim();
            if (!nom.isEmpty()) noeuds.add(nom);
        }

        this.println("Noeuds saisis : " + noeuds);
        this.print("Confirmer ? (o/n) : ");

        // Si l'utilisateur répond "n", on recommence depuis le début (récursion)
        if (this.nextLine().equalsIgnoreCase("n"))
            return this.demanderNoeuds();

        return noeuds;
    }

    /**
     * Demande si le graphe est orienté ou non.
     *
     * @return true si orienté, false sinon
     */
    public boolean demanderSiOriente()
    {
        this.print("\nGraphe orienté ? (o/n) : ");
        return this.nextLine().equalsIgnoreCase("o");
    }

    /**
     * Demande les arcs sortants pour un noeud donné.
     * L'utilisateur entre une destination et un poids, puis "fin" pour terminer.
     *
     * @param nomNoeud     => le noeud source dont on saisit les arcs
     * @param noeudsDispos => les noeuds vers lesquels un arc est autorisé
     * @return la liste des arcs sous forme de String[] {destination, poids}
     */
    public List<String[]> demanderArcsPourNoeud(String nomNoeud, List<String> noeudsDispos)
    {
        this.println("\n=== Arcs sortants de " + nomNoeud + " ===");
        this.println("Noeuds disponibles : " + noeudsDispos);
        this.println("(Tapez 'fin' pour passer au noeud suivant)");

        List<String[]> lstArcs = new ArrayList<>();

        while (true)
        {
            this.print("  Destination depuis " + nomNoeud + " (ou 'fin') : ");
            String destination = this.nextLine();

            if (destination.equalsIgnoreCase("fin")) break;

            if (!noeudsDispos.contains(destination))
            {
                this.println("  Noeud inconnu. Choisissez parmi : " + noeudsDispos);
                continue;
            }
            if (destination.equals(nomNoeud))
            {
                this.println("  Arc vers soi-même ignoré.");
                continue;
            }

            this.print("  Poids de l'arc " + nomNoeud + " -> " + destination + " : ");
            String poids = this.nextLine();

            try
            {
                // On vérifie que le poids est bien un entier (peut être négatif)
                Integer.parseInt(poids);
                lstArcs.add(new String[]{destination, poids});
                this.println("  Arc ajouté : " + nomNoeud + " -> " + destination
                        + " (poids: " + poids + ")");
            }
            catch (NumberFormatException e)
            {
                this.println("  Poids invalide, arc ignoré.");
            }
        }

        return lstArcs;
    }

    /**
     * Demande le noeud de départ pour le calcul parmi les noeuds disponibles.
     *
     * @param noeudsDispos => la liste des noeuds existants dans le graphe
     * @return le nom du noeud source choisi
     */
    public String demanderNoeudSource(List<String> noeudsDispos)
    {
        this.println("\n=== Noeud source ===");
        this.println("Noeuds disponibles : " + noeudsDispos);

        String source = "";
        while (!noeudsDispos.contains(source))
        {
            this.print("Noeud de départ : ");
            source = this.nextLine();
            if (!noeudsDispos.contains(source))
                this.println("Noeud inconnu, réessayez.");
        }
        return source;
    }

    // =========================================================================
    // Affichage du résultat
    // =========================================================================

    /**
     * Affiche un tableau récapitulatif avec les distances et chemins trouvés.
     *
     * @param resultat => le résultat produit par l'algorithme
     * @param nomAlgo  => le nom de l'algorithme utilisé pour l'en-tête
     */
    public void afficherResultat(Resultat resultat, String nomAlgo)
    {
        this.println("\n========================================");
        this.println("  Résultats - " + nomAlgo);
        this.println("  Source : " + resultat.getNomSource());
        this.println("========================================");
        this.println(String.format("%-10s %-10s %s", "Noeud", "Distance", "Chemin"));
        this.println("----------------------------------------");

        for (Map.Entry<String, Integer> entry : resultat.getDistances().entrySet())
        {
            String       nom    = entry.getKey();
            int          dist   = entry.getValue();
            List<String> chemin = resultat.getChemin(nom);

            // Integer.MAX_VALUE = noeud inaccessible => on affiche ∞
            String distStr   = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
            String cheminStr = (chemin == null || chemin.isEmpty())
                               ? "Inaccessible"
                               : String.join(" -> ", chemin);

            this.println(String.format("%-10s %-10s %s", nom, distStr, cheminStr));
        }
        this.println("========================================");
    }
}
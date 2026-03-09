package vue;

import metier.Resultat;
import vue.Frame;

import java.util.*;

/**
 * Vue console : interaction via le terminal.
 * Implémente IVue → peut être remplacée par n'importe quelle autre vue (Swing, web…)
 * sans modifier une seule ligne du contrôleur ou du modèle.
 */
public class VueConsole
{

    private final Scanner scanner;
	private Frame frame;

    public VueConsole() 
    {
        this.scanner = new Scanner(System.in);
    }

   
    public void afficherMessageConsole(String message) 
    {
        System.out.println("[INFO] " + message);
    }


    public void afficherErreurConsole(String erreur) 
    {
        System.err.println("[ERREUR] " + erreur);
    }

    public int demanderChoixAlgorithme(List<String> algorithmes) 
    {
        System.out.println("\n=== Choix de l'algorithme ===");
        for (int i = 0; i < algorithmes.size(); i++) 
        {
            System.out.println("  " + (i + 1) + ". " + algorithmes.get(i));
        }

        int choix = -1;
        while (choix < 0 || choix >= algorithmes.size()) 
        {
            System.out.print("Votre choix (1-" + algorithmes.size() + ") : ");
            try 
            {
                choix = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (choix < 0 || choix >= algorithmes.size()) 
                {
                    System.out.println("Choix invalide, réessayez.");
                }

            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Veuillez entrer un nombre.");
            }
        }
        return choix;
    }

    public List<String> demanderNoeuds() 
    {
        System.out.println("\n=== Saisie des noeuds ===");
        System.out.print("Entrez les noms des noeuds séparés par des virgules (ex: A,B,C,S) : ");
       
        String   ligne      = scanner.nextLine().trim();
        String[] parts      = ligne.split(",");

        // Le but ici ces de verifier qui ya pas de vide pour les nom de Noeud
        // Une fois fait, on crée une List extenssible et saine
        List<String> noeuds = new ArrayList<>();
        for (String part : parts) 
        {
            String nom = part.trim();
            if (!nom.isEmpty()) noeuds.add(nom);
        }

        // Confirmation
        System.out.println("Noeuds saisis : " + noeuds);
        System.out.print("Confirmer ? (o/n) o = OUI et n = NON : ");
        String reponse = scanner.nextLine().trim();

        // equalsIgnoreCase() compare deux chaînes de caractères sans tenir compte des majuscules/minuscules
        // (Ces moin casse pied pour les verifications individuel)
        if (reponse.equalsIgnoreCase("n")) 
        {
            return demanderNoeuds(); // Inpeu de récursivité si l'utilisateur veut recommencer
        }

        return noeuds;
    }

    public List<String[]> demanderArcsPourNoeud(String nomNoeud, List<String> noeudsDispos) 
    {
        System.out.println("\n=== Arcs sortants de " + nomNoeud + " ===");
        System.out.println("Noeuds disponibles : " + noeudsDispos);
        System.out.println("(Tapez 'fin' pour passer au noeud suivant)");

        List<String[]> lstArcs = new ArrayList<>();

        while (true) 
        {
            System.out.print("  Destination depuis " + nomNoeud + " (ou 'fin') : ");
            String destination = scanner.nextLine().trim();

            if (destination.equalsIgnoreCase("fin")) break;

            if (!noeudsDispos.contains(destination)) 
            {
                System.out.println("  Ce noeud n'existe pas. Choisissez parmi : " + noeudsDispos);
                continue;
            }

            if (destination.equals(nomNoeud)) 
            {
                System.out.println("  Un arc vers soi-même n'est pas utile ici.");
                continue;
            }

            System.out.print("  Poids de l'arc " + nomNoeud + " -> " + destination + " : ");
            String poids = scanner.nextLine().trim();

            try 
            {
                Integer.parseInt(poids); // validation
                lstArcs.add(new String[]{destination, poids});
                System.out.println("  Arc " + nomNoeud + " -> " + destination + " (poids: " + poids + ") ajouté.");
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("  Poids invalide, cet arc est ignoré.");
            }
        }

        return lstArcs;
    }


    public String demanderNoeudSource(List<String> noeudsDispos) 
    {
        System.out.println("\n=== Noeud source ===");
        System.out.println("Noeuds disponibles : " + noeudsDispos);

        String source = "";
        while (!noeudsDispos.contains(source)) 
        {
            System.out.print("Choisissez le noeud de départ : ");
            source = scanner.nextLine().trim();

            if (!noeudsDispos.contains(source)) 
            {
                System.out.println("Noeud inconnu, réessayez.");
            }
        }
        return source;
    }


    public boolean demanderSiOriente() 
    {
        System.out.print("Graphe orienté ou non-orienté ? (o/n) : ");
        return scanner.nextLine().trim().equalsIgnoreCase("o");
    }


    public void afficherResultat(Resultat resultat, String nomAlgo) 
    {
        System.out.println("\n========================================");
        System.out.println("  Résultats - " + nomAlgo);
        System.out.println("  Source : " + resultat.getNomSource());
        System.out.println("========================================");
        System.out.printf("%-10s %-10s %s%n", "Noeud", "Distance", "Chemin");
        System.out.println("----------------------------------------");

        for (Map.Entry<String, Integer> entry : resultat.getDistances().entrySet()) 
        {
            String       nom = entry.getKey();
            int          dist = entry.getValue();
            List<String> chemin = resultat.getChemin(nom);

            String distStr   = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
            String cheminStr = (chemin == null || chemin.isEmpty()) ? "Inaccessible"
                                                                     : String.join(" -> ", chemin);

            System.out.printf("%-10s %-10s %s%n", nom, distStr, cheminStr);
        }
        System.out.println("========================================");
    }
}

package controller;

import metier.*;
import vue.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur principal : orchestre la vue, le graphe et les algorithmes.
 * Ne connaît la vue que à travers IVue pour augmenter l'indépendance.
 */
public class Controller implements IController 
{

    private Graphe graphe;
    private IAlgorithme algorithme;

	private Frame      vueInterface;
    private VueConsole vueConsole;

    // Liste des algorithmes disponibles dans l'application
    private final List<IAlgorithme> algorithmesDisponibles;

    public Controller() 
    {
        this.vueInterface   = new Frame(this);
        this.vueConsole     = new VueConsole();
        this.graphe         = new Graphe();

        // asList crée une list non modifiable
        this.algorithmesDisponibles = Arrays.asList(new Dijkstra(), new BellmanFord());
    }

    @Override
    public void lancerApplication() 
    {
        // Étape 3 : type de graphe (orienté ou non)
        boolean oriente = vue.demanderSiOriente();
        vue.afficherMessage(oriente
            ? "Graphe orienté."
            : "Graphe non-orienté (arcs retour ajoutés automatiquement).");

        // Étape 4 : saisie des arcs pour chaque noeud
        for (String nomNoeud : lstNomsNoeuds) 
        {
            List<String[]> arcs = vue.demanderArcsPourNoeud(nomNoeud, lstNomsNoeuds);
            
            // Pour rapelle la list "arc" contien 1 arc
            // arc[0] => destination arc[1] => le poid
            for (String[] arc : arcs) 
            {
                String destination = arc[0];
                try 
                {
                    int poids  = Integer.parseInt(arc[1]);
                    boolean ok = ajouterArc(nomNoeud, destination, poids);
                    if (!ok) 
                    {
                        vue.afficherErreur("Arc invalide : " + nomNoeud + " -> " + destination);
                    }

                    // !!!!!!!! Si non-orienté : on ajoute l'arc dans l'autre sens automatiquement
                    if (!oriente) 
                    {
                        ajouterArc(destination, nomNoeud, poids);
                    }
                } 
                catch (NumberFormatException e) 
                {
                    vue.afficherErreur("Poids invalide pour l'arc " + nomNoeud + " -> " + destination);
                }
            }
        }

        // Étape 5 : choix du noeud source et lancement du calcul
        String source = vue.demanderNoeudSource(lstNomsNoeuds);
        lancerCalcul(source);
    }

    public void lancerConsole()
	{
		try
		{
			ProcessBuilder pb = null; // Déclaration et initialisation

			String os = System.getProperty("os.name").toLowerCase();

			switch (os)
			{
				case String s && s.contains("win") -> pb = new ProcessBuilder("cmd", "/c", "start", "cmd");
				case String s && s.contains("mac") -> pb = new ProcessBuilder("open", "-a", "Terminal");
				case String s && (s.contains("nix") || s.contains("nux")) -> pb = new ProcessBuilder("gnome-terminal");
				default -> 
				{
					System.out.println("OS non supporté");
					return;
				}
			}

			if (pb != null)
			{
				pb.start();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// --- Partie graphe ---
		// Étape 1 : choix de l'algorithme
		List<String> lstNoms = new ArrayList<>();
		for (IAlgorithme algo : algorithmesDisponibles)
		{
			lstNoms.add(algo.getNom());
		}
		int choix = this.vueConsole.demanderChoixAlgorithme(lstNoms);
		this.algorithme = algorithmesDisponibles.get(choix);
		this.vueConsole.afficherMessage("Algorithme sélectionné : " + this.algorithme.getNom());

		// Étape 2 : saisie des noeuds
		List<String> lstNomsNoeuds = this.vueConsole.demanderNoeuds();
		creerNoeuds(lstNomsNoeuds);
		this.vueConsole.afficherMessageConsole(lstNomsNoeuds.size() + " noeud(s) créé(s).");

		// Étape 3 : type de graphe (orienté ou non)
		boolean oriente = this.vueConsole.demanderSiOriente();
		this.vueConsole.afficherMessageConsole(oriente
			? "Graphe orienté."
			: "Graphe non-orienté (arcs retour ajoutés automatiquement).");

		// Étape 4 : saisie des arcs pour chaque noeud
		for (String nomNoeud : lstNomsNoeuds)
		{
			List<String[]> arcs = this.vueConsole.demanderArcsPourNoeud(nomNoeud, lstNomsNoeuds);

			for (String[] arc : arcs)
			{
				String destination = arc[0];
				try
				{
					int poids = Integer.parseInt(arc[1]);
					boolean ok = ajouterArc(nomNoeud, destination, poids);

					if (!ok)
					{
						this.vueConsole.afficherErreurConsole("Arc invalide : " + nomNoeud + " -> " + destination);
					}

					// Si non-orienté : on ajoute l'arc dans l'autre sens automatiquement
					if (!oriente)
					{
						ajouterArc(destination, nomNoeud, poids);
					}
				}
				catch (NumberFormatException e)
				{
					this.vueConsole.afficherErreurConsole("Poids invalide pour l'arc " + nomNoeud + " -> " + destination);
				}
			}
		}

		// Étape 5 : choix du noeud source et lancement du calcul
		String source = this.vueConsole.demanderNoeudSource(lstNomsNoeuds);
		lancerCalcul(source);
	}

    @Override
    public void creerNoeuds(List<String> noms) 
    {
        this.graphe = new Graphe(); // reset au cas où
        for (String nom : noms) 
        {
            graphe.ajouterNoeud(nom.trim());
        }
    }

    @Override
    public boolean ajouterArc(String nomSource, String nomDestination, int poids) 
    {
        return graphe.ajouterArc(nomSource.trim(), nomDestination.trim(), poids);
    }

    @Override
    public void lancerCalcul(String nomSource, int algorithme,boolean estOrienter) 
    {
		this.algorithme = algorithmesDisponibles.get(algorithme);

        if (algorithme == null) 
        {
            vue.afficherErreur("Aucun algorithme sélectionné.");
            return;
        }
        if (!graphe.contientNoeud(nomSource)) 
        {
            vue.afficherErreur("Le noeud source '" + nomSource + "' n'existe pas.");
            return;
        }

        Resultat resultat = algorithme.calculer(graphe, nomSource);

        if (resultat == null) 
        {
            vue.afficherErreur("Calcul impossible : cycle de poids négatif détecté dans le graphe.");
            return;
        }

        vue.afficherResultat(resultat, algorithme.getNom());
    }
}

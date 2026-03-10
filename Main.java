import controller.Controller;
import controller.IController;

/**
 * Point d'entrée de l'application.
 *
 * Deux modes de lancement possibles :
 *
 *   java Main
 *     => Lance l'IHM Swing. L'utilisateur interagit via la fenêtre graphique.
 *       Le menu "Options" permet de choisir le mode d'affichage du résultat.
 *
 *   java Main console
 *     => Lance le mode console système. Toute l'interaction se fait dans le terminal
 *       via System.in / System.out. Pas de fenêtre graphique.
 *
 * Le choix du mode est délégué au Controller via IController,
 * ce qui permet de changer d'implémentation sans toucher à ce fichier.
 */
public class Main
{
    public static void main(String[] args)
    {
        IController controller = new Controller();

        // Si le premier argument est "console", on lance le mode terminal système
        if (args.length > 0 && args[0].equalsIgnoreCase("console"))
            controller.lancerConsole();
        else
            controller.lancerIHM();
    }
}
import controller.Controller;
import controller.IController;
import vue.IVue;
import vue.VueConsole;

/**
 * Point d'entrée de l'application.
 *
 * Pour changer de vue (ex: passer à Swing), il suffit de remplacer
 * "new VueConsole()" par "new VueSwing()" — RIEN D'AUTRE ne change.
 */
public class Main 
{
    public static void main(String[] args) 
    {
        // Création de la vue (seul endroit où on choisit laquelle utiliser)
        IVue vue = new VueConsole();
        // IVue vue = new VueSwing(); // ← il suffirait de changer cette ligne
        
        // Injection de la vue dans le contrôleur
        IController controller = new Controller(vue);

        // Lancement
        controller.lancerApplication();
    }
}

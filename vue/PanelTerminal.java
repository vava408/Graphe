package vue;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Un panel qui simule un terminal texte dans l'interface Swing.
 *
 * Il peut fonctionner dans deux modes selon le paramètre du constructeur :
 *
 *   lectureSeule = false => terminal de saisie
 *     L'utilisateur peut taper du texte en bas et valider avec Entrée.
 *     Utilisé quand on coche "Terminal de saisie" dans le menu.
 *
 *   lectureSeule = true => terminal d'affichage
 *     La zone de saisie est absente, on ne peut rien taper.
 *     Il sert uniquement à afficher les logs de l'algo.
 *     Utilisé quand on coche "Terminal d'affichage" dans le menu.
 *
 * Le mécanisme de synchronisation entre les threads :
 *   readLine() est bloquant => il attend qu'une ligne arrive dans la BlockingQueue.
 *   Quand l'utilisateur appuie sur Entrée, la saisie est déposée dans la file
 *   via file.offer(), ce qui débloque immédiatement readLine().
 *
 *   IMPORTANT => readLine() doit toujours être appelé depuis un thread séparé,
 *   jamais depuis l'EDT (Event Dispatch Thread), sinon l'interface serait gelée.
 */
public class PanelTerminal extends JPanel
{
    // La grande zone noire qui affiche les messages, toujours en lecture seule
    private final JTextArea  zoneAffichage;

    // Le champ de saisie en bas => null si on est en mode lecture seule
    private final JTextField zoneSaisie;

    // La file bloquante entre l'EDT et le thread console
    // L'EDT dépose la saisie dedans, le thread console l'attend avec file.take()
    // null si on est en mode lecture seule (readLine() ne sera jamais appelé)
    private final BlockingQueue<String> file;

    /**
     * Crée un terminal texte.
     *
     * @param lectureSeule => true pour un terminal d'affichage sans saisie,
     *                        false pour un terminal de saisie interactif
     */
    public PanelTerminal(boolean lectureSeule)
    {
        this.setLayout(new BorderLayout(4, 4));

        // Le titre du panel change selon le mode pour que ce soit clair visuellement
        String titre = lectureSeule ? "Terminal d'affichage" : "Terminal de saisie";
        this.setBorder(BorderFactory.createTitledBorder(titre));

        // Zone d'affichage => commune aux deux modes, fond noir texte vert style terminal
        this.zoneAffichage = new JTextArea();
        this.zoneAffichage.setEditable(false);
        this.zoneAffichage.setBackground(Color.BLACK);
        this.zoneAffichage.setForeground(new Color(0, 220, 0));
        this.zoneAffichage.setCaretColor(new Color(0, 220, 0));
        this.zoneAffichage.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        this.zoneAffichage.setLineWrap(true);
        this.zoneAffichage.setWrapStyleWord(false);

        JScrollPane scroll = new JScrollPane(this.zoneAffichage);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        this.add(scroll, BorderLayout.CENTER);

        if (!lectureSeule)
        {
            // Mode saisie => on crée la file et le champ de saisie en bas
            this.file       = new ArrayBlockingQueue<>(1);
            this.zoneSaisie = new JTextField();
            this.zoneSaisie.setBackground(Color.BLACK);
            this.zoneSaisie.setForeground(new Color(0, 220, 0));
            this.zoneSaisie.setCaretColor(new Color(0, 220, 0));
            this.zoneSaisie.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            this.zoneSaisie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 180, 0)));

            // Quand l'utilisateur appuie sur Entrée :
            // 1. On récupère ce qu'il a tapé
            // 2. On vide le champ
            // 3. On affiche la saisie dans la zone d'affichage avec ">"
            // 4. On dépose dans la file => ça débloque readLine() dans le thread console
            this.zoneSaisie.addActionListener(e ->
            {
                String ligne = this.zoneSaisie.getText();
                this.zoneSaisie.setText("");
                this.afficherLigne("> " + ligne);
                this.file.offer(ligne);
            });

            this.add(this.zoneSaisie, BorderLayout.SOUTH);
        }
        else
        {
            // Mode lecture seule => pas de file, pas de zone de saisie
            this.file       = null;
            this.zoneSaisie = null;
        }
    }

    // =========================================================================
    // Méthodes publiques
    // =========================================================================

    /**
     * Affiche une ligne de texte suivie d'un saut de ligne.
     * Thread-safe => peut être appelé depuis n'importe quel thread.
     */
    public void afficherLigne(String ligne)
    {
        SwingUtilities.invokeLater(() ->
        {
            this.zoneAffichage.append(ligne + "\n");
            // Auto-scroll => on reste toujours visible en bas
            this.zoneAffichage.setCaretPosition(this.zoneAffichage.getDocument().getLength());
        });
    }

    /**
     * Affiche du texte sans saut de ligne (pour les prompts de saisie).
     * Thread-safe => peut être appelé depuis n'importe quel thread.
     */
    public void afficher(String texte)
    {
        SwingUtilities.invokeLater(() ->
        {
            this.zoneAffichage.append(texte);
            this.zoneAffichage.setCaretPosition(this.zoneAffichage.getDocument().getLength());
        });
    }

    /**
     * Attend que l'utilisateur tape une ligne et appuie sur Entrée, puis la retourne.
     *
     * BLOQUANT => à appeler uniquement depuis un thread séparé, jamais depuis l'EDT.
     * Ne doit jamais être appelé sur un terminal en mode lecture seule.
     *
     * @return la ligne saisie par l'utilisateur, ou "" en cas d'interruption
     */
    public String readLine()
    {
        // Si on est en mode lecture seule, c'est un bug => on signale et on sort
        if (this.file == null || this.zoneSaisie == null)
        {
            System.err.println("Erreur : readLine() appelé sur un terminal en lecture seule !");
            return "";
        }

        // On active le champ de saisie et on lui donne le focus
        this.zoneSaisie.setEnabled(true);
        SwingUtilities.invokeLater(this.zoneSaisie::requestFocusInWindow);

        try
        {
            // On attend ici que l'utilisateur appuie sur Entrée
            // file.take() est bloquant jusqu'à ce que file.offer() soit appelé
            return this.file.take();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return "";
        }
        finally
        {
            // Que ce soit un succès ou une interruption, on désactive la saisie
            this.zoneSaisie.setEnabled(false);
        }
    }

    /**
     * Efface tout le contenu de la zone d'affichage.
     * Thread-safe => peut être appelé depuis n'importe quel thread.
     */
    public void effacer()
    {
        SwingUtilities.invokeLater(() -> this.zoneAffichage.setText(""));
    }
}
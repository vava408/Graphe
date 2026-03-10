package vue;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Panel Swing qui simule un terminal texte interactif.
 *
 * Composé de deux zones :
 *   - zoneAffichage : JTextArea en lecture seule, fond noir / texte vert.
 *                     Reçoit tous les messages affichés par VueConsole.
 *   - zoneSaisie    : JTextField en bas de panel.
 *                     L'utilisateur y tape sa réponse et valide avec Entrée.
 *
 * Mécanisme de synchronisation :
 *   readLine() est bloquant — il attend qu'une ligne soit disponible dans la BlockingQueue.
 *   Quand l'utilisateur appuie sur Entrée dans zoneSaisie, le texte est placé dans la file
 *   via file.offer(), ce qui débloque immédiatement readLine().
 *
 *   IMPORTANT : readLine() doit être appelé depuis un thread non-EDT (Event Dispatch Thread).
 *   Si on l'appelait dans l'EDT, toute l'IHM serait gelée pendant l'attente de saisie.
 *   C'est pourquoi Frame démarre un thread daemon séparé pour la session console.
 */
public class PanelTerminal extends JPanel
{
    /** Zone d'affichage des messages — lecture seule. */
    private final JTextArea  zoneAffichage;

    /** Zone de saisie de l'utilisateur — en bas du panel. */
    private final JTextField zoneSaisie;

    /**
     * File bloquante de capacité 1.
     * Le thread console attend dedans (readLine → file.take()).
     * L'EDT y dépose la saisie quand l'utilisateur appuie sur Entrée (file.offer()).
     */
    private final BlockingQueue<String> file = new ArrayBlockingQueue<>(1);

    public PanelTerminal()
    {
        this.setLayout(new BorderLayout(4, 4));
        this.setBorder(BorderFactory.createTitledBorder("Terminal"));

        // --- Zone d'affichage ---
        zoneAffichage = new JTextArea();
        zoneAffichage.setEditable(false);
        zoneAffichage.setBackground(Color.BLACK);
        zoneAffichage.setForeground(new Color(0, 220, 0));
        zoneAffichage.setCaretColor(new Color(0, 220, 0));
        zoneAffichage.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        zoneAffichage.setLineWrap(true);
        zoneAffichage.setWrapStyleWord(false);

        JScrollPane scroll = new JScrollPane(zoneAffichage);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        this.add(scroll, BorderLayout.CENTER);

        // --- Zone de saisie ---
        zoneSaisie = new JTextField();
        zoneSaisie.setBackground(Color.BLACK);
        zoneSaisie.setForeground(new Color(0, 220, 0));
        zoneSaisie.setCaretColor(new Color(0, 220, 0));
        zoneSaisie.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        zoneSaisie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 180, 0)));

        // Quand l'utilisateur appuie sur Entrée :
        //   1. On récupère le texte saisi
        //   2. On vide le champ
        //   3. On affiche la saisie dans la zone d'affichage (préfixée par ">")
        //   4. On dépose la saisie dans la file → débloque readLine()
        zoneSaisie.addActionListener(e ->
        {
            String ligne = zoneSaisie.getText();
            zoneSaisie.setText("");
            afficherLigne("> " + ligne);
            file.offer(ligne);
        });

        this.add(zoneSaisie, BorderLayout.SOUTH);
    }

    // =========================================================================
    // API publique — utilisée par VueConsole
    // =========================================================================

    /**
     * Affiche une ligne de texte suivie d'un saut de ligne dans la zone d'affichage.
     * Thread-safe : peut être appelé depuis n'importe quel thread (invokeLater).
     *
     * @param ligne Le texte à afficher
     */
    public void afficherLigne(String ligne)
    {
        SwingUtilities.invokeLater(() ->
        {
            zoneAffichage.append(ligne + "\n");
            // Auto-scroll : toujours visible en bas
            zoneAffichage.setCaretPosition(zoneAffichage.getDocument().getLength());
        });
    }

    /**
     * Affiche du texte sans saut de ligne (utilisé pour les prompts de saisie).
     * Thread-safe : peut être appelé depuis n'importe quel thread (invokeLater).
     *
     * @param texte Le texte à afficher
     */
    public void afficher(String texte)
    {
        SwingUtilities.invokeLater(() ->
        {
            zoneAffichage.append(texte);
            zoneAffichage.setCaretPosition(zoneAffichage.getDocument().getLength());
        });
    }

    /**
     * Attend que l'utilisateur saisisse une ligne et appuie sur Entrée.
     *
     * BLOQUANT — doit être appelé depuis un thread non-EDT uniquement.
     * Active la zone de saisie avant d'attendre, la désactive après.
     *
     * @return La ligne saisie par l'utilisateur (jamais null, "" si interruption)
     */
    public String readLine()
    {
        // Active la saisie et donne le focus à l'utilisateur
        zoneSaisie.setEnabled(true);
        SwingUtilities.invokeLater(zoneSaisie::requestFocusInWindow);

        try
        {
            return file.take(); // bloque ici jusqu'à ce que l'utilisateur appuie sur Entrée
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return "";
        }
        finally
        {
            // Désactive la saisie pendant que le programme traite la réponse
            zoneSaisie.setEnabled(false);
        }
    }

    /**
     * Efface tout le contenu de la zone d'affichage.
     * Thread-safe : peut être appelé depuis n'importe quel thread.
     */
    public void effacer()
    {
        SwingUtilities.invokeLater(() -> zoneAffichage.setText(""));
    }
}
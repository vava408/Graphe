package vue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelTableau extends JPanel
{
    private final DefaultTableModel model;
    private final JTable table;
    private final List<String> noeuds;

    public PanelTableau()
    {
        this.noeuds = new ArrayList<>();

        this.setLayout(new BorderLayout(8, 8));
        this.setBorder(BorderFactory.createTitledBorder("Saisie des arcs"));

        this.model = new DefaultTableModel(new Object[]{"Source", "Destination", "Poids"}, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return true;
            }
        };

        this.table = new JTable(model);
        this.table.setFillsViewportHeight(true);
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        // Zone boutons: 2 lignes x 3 colonnes => tout visible
        JPanel panelActions = new JPanel(new GridLayout(2, 3, 8, 8));
        panelActions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelActions.setPreferredSize(new Dimension(10, 95));

        JButton btnAjouter = new JButton("+ Ligne");
        JButton btnSupprimer = new JButton("- Ligne");
        JCheckBox cbOriente = new JCheckBox("Orienté");
        JButton btnDijkstra = new JButton("Dijkstra");
        JButton btnBellmanFord = new JButton("Bellman-Ford");
        JButton btnVider = new JButton("Vider");

        panelActions.add(btnAjouter);
        panelActions.add(btnSupprimer);
        panelActions.add(cbOriente);
        panelActions.add(btnDijkstra);
        panelActions.add(btnBellmanFord);
        panelActions.add(btnVider);

        this.add(panelActions, BorderLayout.SOUTH);

        btnAjouter.addActionListener(e -> model.addRow(new Object[]{"", "", ""}));
        btnSupprimer.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) model.removeRow(row);
        });

		btnDijkstra.addActionListener(e -> {});
		btnBellmanFord.addActionListener(e -> {});
		cbOriente.addActionListener(e -> {});

        btnVider.addActionListener(e -> model.setRowCount(0));

        model.addRow(new Object[]{"", "", ""});
    }

    public void ajouterLigneVide()
    {
        model.addRow(new Object[]{"", "", ""});
    }

    private String valeur(int row, int col)
    {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }
}

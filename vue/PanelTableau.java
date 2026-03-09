package vue;

import controller.Controller; 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.*;



public class PanelTableau extends JPanel
{
    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> cbPointDepart;
    private final JComboBox<String> cbAlgorithme;
    private final JCheckBox cbOriente;
	private Controller controller;

    public PanelTableau(Controller controller)
    {
		this.controller = controller;
        this.setLayout(new BorderLayout(8, 8));
        this.setBorder(BorderFactory.createTitledBorder("Saisie des arcs"));

        this.model = new DefaultTableModel(new Object[]{"Source", "Destination", "Poids"}, 0)
        {
            public boolean isCellEditable(int row, int column)
            {
                return true;
            }
        };

        this.table = new JTable(model);
        this.table.setFillsViewportHeight(true);
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panelActions = new JPanel(new GridLayout(3, 3, 8, 8));
        panelActions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelActions.setPreferredSize(new Dimension(10, 130));

        JButton btnAjouter = new JButton("+ Ligne");
        JButton btnSupprimer = new JButton("- Ligne");
        this.cbOriente = new JCheckBox("Orienté");
        this.cbAlgorithme = new JComboBox<>(new String[]{"Dijkstra", "Bellman-Ford"});
        this.cbPointDepart = new JComboBox<>();
        this.cbPointDepart.setEditable(true);
        JButton btnCalculer = new JButton("Calculer");
        JButton btnVider = new JButton("Vider");

        panelActions.add(btnAjouter);
        panelActions.add(btnSupprimer);
        panelActions.add(btnVider);
        panelActions.add(new JLabel("Algorithme"));
        panelActions.add(cbAlgorithme);
        panelActions.add(cbOriente);
        panelActions.add(new JLabel("Point de départ"));
        panelActions.add(cbPointDepart);
        panelActions.add(btnCalculer);

        this.add(panelActions, BorderLayout.SOUTH);

        btnAjouter.addActionListener(e -> model.addRow(new Object[]{"", "", ""}));
        btnSupprimer.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) model.removeRow(row);
        });

        btnCalculer.addActionListener(e -> this.creerGraphe());

        btnVider.addActionListener(e -> {
            model.setRowCount(0);
            
        });

        model.addTableModelListener(e -> this.remplirPointDepart());

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

	private void remplirPointDepart()
	{
		Set<String> points = getNoeud();

		cbPointDepart.removeAllItems();

		for (String p : points)
		{
			cbPointDepart.addItem(p);
		}

		repaint();
	}

	public void creerGraphe()
	{
		Set<String> noeud = getNoeud();
		List<String> lstNoeud = new ArrayList<>();

		for (String string : noeud)
		{
			lstNoeud.add(string);
		}

		controller.creerNoeuds(lstNoeud);

		for(int cpt = 0 ; model.getRowCount() < cpt; cpt++)
		{
			String source = (String)model.getValueAt(cpt, 0);
			String destination = (String) model.getValueAt(cpt, 1);
			int poid = (int) model.getValueAt(cpt, 2);

			controller.ajouterArc(source,destination, poid);
		}

		String pointDepart = (String)cbPointDepart.getSelectedItem();
		System.out.println(pointDepart);

		//controller.lancerCalcul(pointDepart);

	}

	private Set<String> getNoeud()
	{
		Set<String> points = new LinkedHashSet<>();

		for (int cpt = 0; cpt < model.getRowCount(); cpt++)
		{
			String source = valeur(cpt, 0);
			String destination = valeur(cpt, 1);

			if (!source.isEmpty())
				points.add(source);

			if (!destination.isEmpty())
				points.add(destination);
		}

		return points;
	}
	

}

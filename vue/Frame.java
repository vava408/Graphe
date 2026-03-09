package vue;

import controller.*;

import javax.swing.*;

public class Frame extends JFrame
{
	private PanelTableau panelTableau;
	private PanelGraphe panelGraphe;

	public Frame(Controller controller)
	{
		this.setTitle("Algorithme de Graphes");
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		panelTableau = new PanelTableau(controller);
		panelGraphe  = new PanelGraphe();
		
		this.setLayout(new java.awt.GridLayout(1, 2));
		this.add(panelTableau);
		this.add(panelGraphe);
		
		this.setVisible(true);
	}

}
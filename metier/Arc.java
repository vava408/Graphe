package metier;

import java.util.HashMap;

public class Arc
{
	private Noeud sommetEntant, sommetSortant;
	private int poid;

	public Arc(Noeud sommetEntant, Noeud sommetSortant, int poid)
	{
		this.sommetEntant = sommetEntant;
		this.sommetSortant = sommetSortant;
		this.poid    = poid;
		
	}

	public Noeud getSommetEntrant()
	{
		return this.sommetEntant;
	}
}

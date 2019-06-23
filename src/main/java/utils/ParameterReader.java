/**
 * Auteur : Louis Reymondin
 * Date   : Mars 2007
 * But 	  : Lecture g�n�rique d'un fichier utilis� dans l'application
 * 			Il est possible d'utiliser cette classe comme un iterateur sur le fichier
 * 			Et elle est it�rable sur les donn�es d�j� lue.
 *
 */

package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ParameterReader implements Iterator<String>, Iterable<String>
{

	private ArrayList<String> lignes = new ArrayList<String>();
	private ArrayList<String> labels = new ArrayList<String>();
	private ArrayList<String> entetes = new ArrayList<String>();

	protected String     chemin;
	private BufferedReader lecteur  = null;
	private int currentLigne = 0;
	public final int MAX_LIGNE;

	String ligne;

	/**
	 * Constructeur.
	 *
	 * @param chemin Chemin jusqu'au fichier � lire
	 */

	public ParameterReader(String chemin) throws Exception
	{
		this.chemin = chemin;
		this.MAX_LIGNE = -1;
		int entete = 0;

		//Ouverture du flux
		lecteur = new BufferedReader(new FileReader(new File(chemin)));
		//Lecture des ent�tes
		for(int i = 0; i<entete; i++)
			entetes.add(lecteur.readLine());
	}

	/**
	 * Lire tout un fichier
	 *
	 */
	public void loadFile()
	{
		try
		{
			//parcours tout le fichier
			while((ligne = readLine()) != null)
			{
				//Ajouter la ligne et son label
				addLine(ligne,"Parametre");

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public String next()
	{
		return readLine();
	}

	/**
	 * Lire une ligne du fichier et le prendre en compte dans l'avancement
	 * @return La ligne lue
	 */
	protected String readLine()
	{
		currentLigne++;
		try
		{
			return lecteur.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getLignes()
	{
		return lignes;
	}

	/**
	 * Ajouter une ligne
	 * @param ligne Ligne � ajouter
	 */
	public void addLine(String ligne, String label)
	{
		lignes.add(ligne);
		addLineLabel(label);
	}

	/**
	 * Ajouter une ligne label
	 * @param label Le label � ajouter
	 */
	private void addLineLabel(String label)
	{
		labels.add(label);
	}

	/**
	 * Il y a t'il encore des donn�es � lire
	 */

	public final boolean hasNext()
	{
		return currentLigne < MAX_LIGNE;
	}

	/**
	 * Pas de suppression de ligne, donc m�thode vide
	 */

	public final void remove(){}


	@Override
	public void finalize()
	{
		try
		{
			lecteur.close();
			lignes.clear();
			entetes.clear();
			labels.clear();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Iterator<String> iterator()
	{
		return lignes.iterator();
	}

}
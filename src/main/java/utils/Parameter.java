/**
 * Auteur   : Louis Reymondin
 * Date     : Mars 2007
 * But 	    : Lecture g�n�rique d'un fichier utilis� dans l'application
 * 			  Il est possible d'utiliser cette classe comme un iterateur sur le fichier
 * 			  Et elle est it�rable sur les donn�es d�j� lue.
 * Edited by: Andres Cubides
 * Date     : April 2019
 */

package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Parameter
{
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
	
	private final String SEPARATOR = "\\p{Space}";
	private final String COMMENT = "#";
	private final String CHAINE = "\"";
	private final String VARIABLE = "%";
	
	private Hashtable<String,String> parameters = new Hashtable<String,String>();
	private Hashtable<String,String> unInterpretedParameters = new Hashtable<String,String>();
	private ArrayList<String> keys = new ArrayList<String>();
	private static Parameter instance;

	public static Parameter getInstance(String chemin)
	{
		if(instance == null)
			instance = new Parameter(chemin);
		
		return instance;
	}
	
	public static Parameter getInstance()
	{
	  if (instance == null)
	    throw new RuntimeException("Parameter hasn't been initialized with a config file");
		return instance;
	}
	
	private Parameter(String chemin)
	{
		long time = System.currentTimeMillis();
		
		System.out.println("Loading configuration... ");
		ParameterReader reader = null;
		
		try
		{
			reader = new ParameterReader(chemin);
			reader.loadFile();
		}
		catch(Exception e)
		{
			System.err.println("Error during the configuration : "+e);
			System.exit(0);
		}
		
		int i = 0;
		
		for(String ligne : reader.getLignes())
		{
			i++;
			ligne = ligne.trim();
			
			/* Si la ligne n'est pas vide et que ce n'est pas une ligne de commentaires */
			if(!ligne.isEmpty() && !ligne.startsWith((COMMENT)))
			{
				/* Suppression des commentaires */
				if(ligne.indexOf(COMMENT) != -1)
					ligne = ligne.substring(0,ligne.indexOf(COMMENT));
				
				
				if(ligne.indexOf(CHAINE) != -1)
					manageString(chemin,ligne,i);
				else
					manageNumber(chemin,ligne,i);
			}
		}
		
		/**
		 * Super hack ;)
		 */
		
		manageString(chemin,"PATH_ROOT \"%PATH_DATA%%DIRECTORY%\"",-1);
		manageString(chemin,"PATH_CONFIG \"%PATH_ROOT%config/\"",-1);
		manageString(chemin,"PATH_MODELS \"%PATH_ROOT%models/\"",-1);
		manageString(chemin,"PATH_INPUTS \"%PATH_ROOT%inputs/\"",-1);
		manageString(chemin,"PATH_OUTPUTS \"%PATH_ROOT%outputs/\"",-1);
		manageString(chemin,"PATH_SST \"%PATH_INPUTS%cleanedSST.tif/\"",-1);
		manageString(chemin,"PATH_DEPT_LIST \"%PATH_INPUTS%departments.csv/\"",-1);
		manageString(chemin,"PATH_DEPT_DATA \"%PATH_INPUTS%DEPTO/\"",-1);
		manageString(chemin,"CLIENT_SECURITY_POLICY \"%PATH_DATA%client.policy\"",-1);
		
		if(getBoolean("PRINT_CONFIG"))
		{
			Set<String> keyset = parameters.keySet();
			Object[] keys = keyset.toArray();
			Arrays.sort(keys);
			
			for(Object key : keys)
				System.out.println("\t"+key+" = "+parameters.get(key));
		}
		System.out.println("Configuration loaded in "+(System.currentTimeMillis()-time)+"[ms]\n");
		
	}

	private void manageNumber(String chemin, String ligne, int numero)
	{
		String data[] = ligne.split(SEPARATOR);
		
		if(data.length >= 2)
		{
			int index = -1;
			for(int i = 1; i<data.length && index == -1; i++)
				if(!data[i].isEmpty())
					index = i;
			try
			{
				unInterpretedParameters.put(data[0].trim(),data[index].trim());
				keys.add(data[0].trim());
				parameters.put(data[0].trim(), data[index].trim());
				return;
			}catch(Exception e){}
		}
		
		errorMessage(chemin,numero);
		
	}

	private void manageString(String chemin, String ligne, int numero)
	{
		String data[] = ligne.split(SEPARATOR);
		if(data[0].indexOf(CHAINE) == -1)
		{
			String chaine = "";
			for(int i = 1; i<data.length-1; i++)
				chaine += data[i]+" ";
			chaine += data[data.length-1];
			
			chaine = chaine.trim();
			
			unInterpretedParameters.put(data[0],chaine);
			keys.add(data[0]);
			
			while(chaine.indexOf(VARIABLE) != -1)
			{
				String variable = chaine.substring(chaine.indexOf(VARIABLE)+1,chaine.length());
				variable = variable.substring(0, variable.indexOf(VARIABLE));
				if (parameters.get(variable) == null)
				  throw new RuntimeException("Missing variable for substitution : " + variable);
				
				chaine = chaine.replaceAll(VARIABLE+variable+VARIABLE, parameters.get(variable));
			}
			if(chaine.startsWith(CHAINE) && chaine.endsWith(CHAINE))
			{
				parameters.put(data[0], chaine.substring(1,chaine.length()-1));//ligne.substring(ligne.indexOf(CHAINE)+1,ligne.lastIndexOf(CHAINE)));
				return;
			}
		}
		errorMessage(chemin,numero);
	}
	
	private void errorMessage(String chemin, int numero)
	{
		System.out.println("Configuration file "+chemin+" has an error on line "+numero);
	}
	
	private String getParam (String name) {
	  String r = parameters.get(name);
	  if (r == null)
	    throw new RuntimeException("Couldn't find parameter : " + name);
	  return r;
	}
	
	public int getInteger(String name)
	{
		return Integer.parseInt(getParam(name));
	}
	
	public double getDouble(String name)
	{
		return Double.parseDouble(getParam(name));
	}
	
	public String getString(String name)
	{
		return getParam(name);
	}
	
	public boolean getBoolean(String name)
	{
		return Boolean.parseBoolean(getParam(name));
	}
	
	public Date getDate(String name)
	{
		try
		{
			return dateFormat.parse(getParam(name));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

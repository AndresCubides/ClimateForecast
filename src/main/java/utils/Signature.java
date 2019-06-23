package utils;

import java.io.Serializable;

public class Signature implements Serializable
{
	/**
	 * Software version for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Number of cycle
	 */
	private final int cycle;
	
	/**
	 * State of the algorithm
	 */
	private final int etat;
	
	/**
	 * Work number
	 */
	private final int number;

	private final String dept;
	
	private Object params;
	
	/**
	 * Constructor 
	 * @param cycle Number of cycle
	 * @param etat State of the algorithm
	 * @param fichier Work data file number
	 */
	public Signature(int cycle, int etat, int number, String dept)
	{
		this.cycle = cycle;
		this.etat = etat;
		this.number = number;
		this.dept = dept;
		this.params = null;
	}
	
	public<T extends Serializable> void setParams (T params)
	{
	  this.params = params;
	}
	
	public Object getParams ()
	{
	  return params;
	}
	
	/**
	 * Get the number of cycle of the signature
	 * @return Number of cycle
	 */
	public int getCycle()
	{
		return cycle;
	}
	
	/**
	 * Get the work data file number
	 * @return Work data file number
	 */
	public int getNumber()
	{
		return number;
	}

	/**
	 * Get the department name
	 * @return Department name
	 */
	public String getDepartment()
	{
		return dept;
	}
	
	/**
	 * Get the stat of the algorithm
	 * @return Stat of the algorithm
	 */
	public int getEtat()
	{
		return etat;
	}
	
	/**
	 * Definition of a signature1 == signature2
	 * 		Signatures are equals if parameters cycle fichier and etat are equals
	 */
	@Override
	public boolean equals(Object s)
	{
		try
		{
			Signature si = (Signature) s;
			return (getCycle() == si.getCycle()) && (getNumber() == si.getNumber()) && (getEtat() == si.getEtat());
		}
		catch(Exception e)
		{
			return false;
		}
	}
}

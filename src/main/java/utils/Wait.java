package utils;

/**
 *
 * @author Louis Reymondin
 *
 */

public abstract class Wait 
{
	/* should conitnue to work ? */
	private volatile boolean continuer = true;
	
	/**
	 * Should conitnue to work ?
	 * @return Should conitnue to work ?
	 */
	public synchronized boolean getContinuer()
	{
		return continuer;
	}
	
	/**
	 * Set the continue state
	 * @param con continue state (continue or not)
	 */
	public synchronized void setContinuer(boolean con)
	{
		continuer = con;
	}
}
package work;

import utils.DataFormatter;
import utils.Constante;
import utils.Signature;

import java.io.Serializable;

/**
 *
 * @author Louis Reymondin
 *
 */

public abstract class Work implements Constante, Serializable
{
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = -7958355091464380315L;
	/**
	 * The work signature
	 */
	private Signature signature;
	
	/**
	 * Constructor 
	 * @param signature The work signature
	 */
	public Work(Signature signature)
	{
		this.signature = signature;
	}
	
	/**
	 * Abstract method which is used to load the data
	 * needed by the work.
	 *
	 */
	protected abstract void loadData();
	
	/**
	 * Execute the work
	 * @return An object Result which represent the result of the work
	 */
	protected abstract Signature doJob();
	
	/**
	 * Complete execution of the work (load data and performe)
	 * @return The work's Result object.
	 */
	public Signature execute()
	{
		long time = System.currentTimeMillis();
		System.out.println("Start loading data");
		loadData();
		System.out.println("Data loaded in : "+(System.currentTimeMillis()-time)+"[ms]");

		return doJob();
	}
	
	/**
	 * Get the work signature
	 * @return The work signature
	 */
	public Signature getSignature()
	{
		return signature;
	}

}

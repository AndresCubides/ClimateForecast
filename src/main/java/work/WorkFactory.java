package work;

import managers.Manager;
import utils.DataFormatter;
import utils.Signature;


public abstract class WorkFactory
{
	/**
	 * The manager which manage the work
	 */
	protected Manager manager;
	
	/**
	 * Constructor 
	 * @param manager The manager which manage the work
	 */
	public WorkFactory(Manager manager)
	{
		this.manager = manager;
	}
	
	/**
	 * Abstract methode which return the work
	 * @return the manager
	 */
	protected Manager getManager(){
		return manager;
	}
	
	/**
	 * 	
	 * Create a work
	 * 
	 * @param signature The work's signature
	 * @return The work created
	 */
	public abstract Work creatWork(Signature signature);
}

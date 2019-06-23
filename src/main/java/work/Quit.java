package work;

import utils.Signature;

/**
 *
 * @author Louis Reymondin
 *
 */

public class Quit extends Work
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5438084236499559113L;

	Quit()
	{
		super(new Signature(-1,QUITTER,-1, null));
	}
	
	@Override
	protected Signature doJob()
	{
		return getSignature();
	}

	@Override
	protected void loadData(){}

}

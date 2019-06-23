package work;

import managers.Manager;
import utils.Signature;

public class QuitFactory extends WorkFactory
{
	public QuitFactory(Manager manager)
	{
		super(manager);
	}
	
	@Override
	public Work creatWork(Signature signature)
	{
		return new Quit();
	}

}

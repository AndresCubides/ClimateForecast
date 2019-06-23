package utils;

import managers.Manager;
/**
 *
 * @author Louis Reymondin
 *
 */

public class Checker extends Thread
{
	public final int ATTENTE = 350*1800000;
	public final int STEP = 10000;
	
	private Manager manager;
	
	public Checker(Manager manager)
	{
		this.manager = manager;
	}
	
	@Override
	public void run()
	{
		System.out.println(
				"#                                                                  #\n"+
				"#                         ! Checker booted !                       #\n"+
				"#------------------------------------------------------------------#\n\n");
		int sum = 0;
		
		while(!manager.isFinish())
		{
			try
			{
				sleep(STEP);
				sum += STEP;
				if(sum >= ATTENTE) {
					manager.check(ATTENTE);
					sum = 0;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
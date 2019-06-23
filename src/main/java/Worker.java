import managers.RMIManager;
import work.Work;
import utils.Constante;
import utils.UserWait;
import utils.Wait;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * @author Louis Reymondin
 *
 */
public class Worker extends Thread implements Constante
{
	private static Worker instance = null;

	public static Worker getInstance() {
		if (instance == null)
			instance = new Worker();
		return instance;
	}
	
	/**
	 * Listeners which wants to be aware if the manager stop or is init
	 */
	protected ArrayList<WorkerListener> listeners = new ArrayList<WorkerListener>();
  
	/**
	 * The manager which gives the work to the worker
	 */
	private RMIManager manager;
	
	/**
	 * Waiting for asking stop thread
	 */
	private Wait wait;
	
	private boolean problem = false;
	
	/**
	 * We provide a generic cache system to share objects among works of 
	 * a same worker.
	 */
	private HashMap<String, Object> cache = new HashMap<String, Object>();
	
	/**
	 * Constructor of the worker
	 * 
	 * @param nomCoordinateur IP address of the manager
	 */
	private Worker()
	{
		System.out.println("Client is booting...");
		
		/* Install the security manager */
		System.setProperty("java.security.policy", CLIENT_SECURITY_POLICY);
		System.setSecurityManager(new RMISecurityManager());
		
		connection();
		
		/* ! Start to work ! */
		wait = new UserWait();
	}
	
	private void connection()
	{	
		/* Looking for the manager sourcecode */
		String coordinateurNom = "rmi://" + CLIENT_SERVER_ADDRESS + "/PARASID";
		
		/* Trying to download the manager specification */
		try
		{
			manager = (RMIManager) Naming.lookup(coordinateurNom);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Big crash during the boot, software dead...");
			System.exit(0);
		}
		System.out.println("Client booted with server : "+CLIENT_SERVER_ADDRESS);
	}
	
	/**
	 * Working thread function
	 */
	@Override
	public void run()
	{
		Work job;
		int cycle = 0;
		long time;
		do
		{
			problem = false;
			
			try
			{
				do
				{
					job = null;
					System.gc();
					
					System.out.println(
						"\n\n#------------------------------------------------------------------#\n"+
						"#                 ! Press any key to stop the work !               #\n"+
						"# The current calculation will finish and the software will quit   #\n"+
						"#------------------------------------------------------------------#\n\n");
					/* Try to download a work */
					System.out.println("Ask for work...");
					time = System.currentTimeMillis();
					job = manager.getWork();
					System.out.println("Work recieved after : "+(System.currentTimeMillis()-time)+"[ms]");
					
					/* Performs the work and upload it */
					time = System.currentTimeMillis();
					manager.giveResult(job.execute());
					System.out.println("Work done in "+(System.currentTimeMillis()-time)+"[ms]");
					cycle++;
					System.out.println("Already "+cycle+" work done !!!");
				} /* Continue until the manager tells to quit or the user ask for quitting */
				while(job.getSignature().getEtat() != QUITTER && wait.getContinuer());
			}
			catch(Exception e)
			{
				System.err.println("An unexcepted error between the client and the manager occured.\n"+e+
						"\nThe client will try to reconnect in a while.");
				e.printStackTrace();
				problem = true;
			}
			if(problem)
			{
				try
				{
					Thread.sleep(CLIENT_WAITING_TIME);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				System.out.println("The client will now try to reconnect...");
				connection();
			}
		}/* continue if there were a crash*/
		while(problem);
		
		for(WorkerListener listener : listeners)
			listener.workerFinished();
		
		System.out.println("Worker terminate... Thanks you for your collaboration");
		System.out.println("Press enter to quit.");
	}
	
}
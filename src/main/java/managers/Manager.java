package managers;

import utils.DataFormatter;
import work.*;
import utils.Checker;
import utils.Constante;
import utils.Signature;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;


public class Manager extends UnicastRemoteObject implements
		RMIManager, Constante
{
	private DataFormatter formatter;
	private ArrayList<String> deptList;
	private int type = -1;
	private int forecast = -1;
	private int action;

	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The manager is a singleton, there is only one RMI Server.
	 */
	private static Manager instance;

	/**
	 * Is the work finish ?
	 */
	private volatile boolean finish = false;

	/**
	 * managers.Manager of the message "Quit"
	 */
	private QuitFactory quitFactory;

	/**
	 * List of work to do
	 */
	private volatile ArrayList<Work> works = new ArrayList<Work>();

	/**
	 * List of work actually in process
	 */
	private volatile ArrayList<Signature> currents = new ArrayList<Signature>();

	/**
	 * Date of the last result (used to know if the manager is blocked by a
	 * worker)
	 */
	private volatile long time;

	/**
	 * Reboot the manager if it's blocked
	 */
	private Checker checker;

	/**
	 * Number of work done in a cycle
	 */
	private volatile int revenu = 0;

	/**
	 * State of the algorithm
	 */
	private int state = -1;

	/**
	 * Number of cycle
	 */
	private int cycle = 0;

	/**
	 * Work factories
	 */
	protected WorkFactory workFactory;
	
	/**
	 * Listeners which wants to be aware if the manager stop or is init
	 */
	protected ArrayList<ManagerListener> listeners = new ArrayList<ManagerListener>();
	
	protected DecimalFormat decimal = new DecimalFormat("#.##");
	
	/**
	 * managers.Manager's constructor
	 * 
	 * @param connect
     *
	 * @throws RemoteException
	 */
	protected Manager(boolean connect, int action) throws RemoteException
	{
		this.action = action;

		if (connect)
		{
			System.out.println("Processor's number : "
					+ Runtime.getRuntime().availableProcessors());
			System.out.println("Max memory : "
					+ Runtime.getRuntime().maxMemory());
			System.out.println("Start at : " + System.currentTimeMillis()
					+ "[ms]");

			try
			{
				// Create the registrar
				LocateRegistry.createRegistry(1099);
				
				String serveurNom = "PARASID";
				// Registring as server
				Naming.rebind(serveurNom, this);

				System.out
						.println("\n\n#------------------------------------------------------------------#\n"
								+ "#                       ! RMI server online !                      #\n"
								+ "#                                                                  #");
			} catch (Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}
			if (!LOAD_FOR_SAVE_POINT)
				init();
			else
				restore();
			
			fillWorkFactories();
			
			for(ManagerListener listener : listeners)
				listener.managerInitDone();
			
			creatWorks(works);

			quitFactory = new QuitFactory(this);
			checker = new Checker(this);
			checker.start();

			System.out
					.println("#                                                                  #\n"
							+ "#                          ! Server ready !                        #\n"
							+ "#                                                                  #\n"
							+ "#                                                                  #\n"
							+ "#                           ! SST Loaded !                         #\n"
							+ "#                                                                  #");
		}
	}

	/**
	 * Algorithm Initialization
	 *
	 */
	protected void init(){
		Date startDate;
		Date endDate;
		Calendar cal = Calendar.getInstance();

		printHeader();
		formatter = new DataFormatter();

		if(action == 0)
		{//Modeling
			startDate = START_MODELING;
			endDate = END_MODELING;
		}else{//Forecasting
			cal.setTime(START_DETECTION);
			cal.add(Calendar.MONTH, -(LEAD_TIME+TRIMESTER_MODEL_XTRA+MODELING_MONTHS_INPUT)); // Selects data months before to have the input and lead time for the START date
			startDate = cal.getTime();

			if(type == 0){//Evaluation mode
				cal.setTime(END_DETECTION);
				cal.add(Calendar.MONTH, 2); // Selects data 2 months after the beginning of trimester to have enough data to compare
				endDate = cal.getTime();
			}else{//Beyond available data mode
				cal.setTime(END_DETECTION);
				cal.add(Calendar.MONTH, - (LEAD_TIME+MODELING_NB_OUTPUTS)); // Selects data until the months needed to predict END_DETECTION date trimester
				endDate = cal.getTime();
			}
		}

		formatter.loadSST(startDate, endDate);
		deptList = formatter.loadDeptList();
		setState(0);
	}

	/**
	 * Creating manager
	 */
	public static void register(int action) throws Exception
	{
		if (Manager.instance != null)
			throw new RuntimeException("managers.Manager alredy registered");

		Manager.instance = new Manager(true, action);
	}

	/**
	 * Remote access function which give a work to a worker
	 * 
	 * @return The work to do
	 * @throws RemoteException
	 */
	@Override
	public synchronized Work getWork() throws RemoteException
	{
		try
		{
			/* If there is no work to do */
			while (works.size() == 0 && !isFinish())
				wait();

			/*
			 * if the algorithm is finish we send the message "Quit" to the
			 * worker
			 */
			if (isFinish())
				return quitFactory.creatWork(null);

			/* get a work and register it as current work */
			Work work = works.get(0);
			works.remove(0);
			currents.add(work.getSignature());

			return work;
		} catch (Exception e)
		{
			e.printStackTrace();
			return quitFactory.creatWork(null);
		}
	}

	/**
	 * Remote access function which let a worker give his result
	 * 
	 * @throws RemoteException
	 */
	@Override
	public synchronized void giveResult(Signature result) throws RemoteException
	{
		/*
		 * If the worker was too slow or if a client crash we don't pay
		 * attention of the result or if it's a quit action, otherwise we manage
		 * it
		 */
		if (result.getEtat() != QUITTER && result != null && currents.indexOf(result) != -1)
		{
			currents.remove(result);

			revenu++;

			System.out.print("\rWork " + result.getNumber()
					+ " is done, " + decimal.format((100.0 * revenu) / getNbWorkPerStep())
					+ "% of the current step done.       ");

			/* If it's time to go to the next step */
			if (revenu == getNbWorkPerStep())
			{
				System.out.println();
				nextStep();
				if (!isFinish())
				{
					works.clear();
					creatWorks(works);
					revenu = 0;
				}
				notifyAll();
			}

		} else {
			if(result.getEtat() == QUITTER)
				System.out.println("A client quit without any problems");
			else
				System.out.println("A client had a problem and crash, result = "+result);
		}

		time = System.currentTimeMillis();
	}

	public void setState(int state)
	{
		this.state = state;
	}

	/**
	 * Generate all the work for a step
	 * 
	 * @param works
	 *            : The list to fill with the works
	 */
	protected void creatWorks(ArrayList<Work> works)
	{
		Signature signature;
		Work work;
		for (int i = 0; i < getNbWorkPerStep(); i++)
		{
			signature = new Signature(cycle, state, i, deptList.get(i));
			work = workFactory.creatWork(signature);
			if(work != null)
				works.add(work);
		}
	}

	/**
	 * Generate a work represented by a signature
	 * 
	 * @param signature
	 *            The work's signature
	 * @return A work reprented by a signature
	 */
	protected Work creatWork(Signature signature)
	{
		return workFactory.creatWork(signature);
	}

	/**
	 * Restore the algorithm
	 * 
	 */
	protected void restore(){
		init();
	}

	/**
	 * Fill the work's factory list
	 * 
	 */
	protected void fillWorkFactories(){
		if(action == 0){
			workFactory = new ModelingFactory(this, formatter);
		}else{
			workFactory = new ForecastingFactory(this, formatter, type, forecast);
		}

	}

	/**
	 * If the step is finished, prepare the software to the next step
	 * 
	 */
	protected void nextStep(){
		System.out.println("Save done, you can quit the application with ctrl+c");
		setFinish(true);
	}

	/**
	 * Calculate the number of work in a step
	 * 
	 * @return The number of work in a step
	 */
	protected int getNbWorkPerStep(){
		return deptList.size();
	}

	/**
	 * Methode which let the Checker checks the situation
	 * 
	 * @param barriere
	 *            Limit of time before a client's crash is detect
	 */
	public synchronized void check(long barriere)
	{
		if (System.currentTimeMillis() - time >= barriere)
			reboot();
		else
			System.out.println("Check done, last result was given "
					+ (System.currentTimeMillis() - time)
					+ "[ms] ago, everything is ok !");
	}

	/**
	 * Reboot all the work unfinished after a client crash
	 * 
	 */
	private synchronized void reboot()
	{
		System.out.println("One or more clients are dead.\n\tReboot of "
				+ currents.size() + " works");

		for (int i = 0; i < currents.size(); i++)
		{
			works.add(creatWork(currents.get(i)));
			System.out.println("\tWork : " + currents.get(i).getNumber()
					+ " in queue");
		}

		currents.clear();

		System.out.println("Reboot done...");

		notify();
	}

	/**
	 * Is the algorithm finished ?
	 * 
	 * @return Is the algorithm finished ?
	 */
	public synchronized boolean isFinish()
	{
		return finish;
	}

	/**
	 * Set the state of the algorithm
	 * 
	 * @param finish
	 *            State of the algorithm (finish or not)
	 */
	protected synchronized void setFinish(boolean finish)
	{
		this.finish = finish;
		
		if(this.finish){
			for(ManagerListener listener : listeners)
				listener.managerFinished();
		}
	}

	private void printHeader()
	{//Also prints the different forecasting options to select
		if(action == 0){
			System.out.println("#                                                                  #");
			System.out.println("#                       Modeling init process                      #");
			System.out.println("#                                                                  #");
			if(TRIMESTER_MODEL_XTRA != 0){
				System.out.println("#                      Trained with Trimesters                     #");
				System.out.println("#                                                                  #");
			}
		}else{
			System.out.println("#                                                                  #");
			System.out.println("#                     Forecasting init process                     #");
			System.out.println("#                                                                  #");

			Scanner scanner = new Scanner(System.in);
			do{
				System.out.println("#              Type of forecasting: 0 Evaluation mode              #");
				System.out.println("#                                   1 Beyond available data mode   #");
				System.out.print("#              Enter desired data: ");
				type = scanner.nextInt();
				if (type < 0 || type > 1) {
					type = -1;
					System.out.println("#                                                                  #");
					System.out.println("#           Error! Select one of the available options             #");
					System.out.println("#                                                                  #");
				}
			}while(type ==-1);
			System.out.println("#                                                                  #");

			Scanner scanner2 = new Scanner(System.in);
			do{
				System.out.println("#      Data forecasting options: 0 All Consecutive Trimesters      #");
				System.out.println("#                                1-12(Jan-Dec) Per year trimester  #");
				System.out.println("#                                13 Specific single Trimester      #");
				System.out.print("#              Enter desired data: ");
				forecast = scanner2.nextInt();
				if (forecast < 0 || forecast > 13) {
					forecast = -1;
					System.out.println("#                                                                  #");
					System.out.println("#           Error! Select one of the available options             #");
					System.out.println("#                                                                  #");
				}
			}while(forecast ==-1);
			System.out.println("#                                                                  #");
		}
	}

}

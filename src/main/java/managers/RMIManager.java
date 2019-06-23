package managers;

import utils.Signature;
import work.Work;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMIManager extends Remote
{
	/**
	 * Remote access function which give a work to a worker
	 * 
	 * @return The work to do
	 * @throws RemoteException
	 */
	public Work getWork()  throws RemoteException;
	
	/**
	 * Remote access function which let a worker give his result
	 * 
	 * @throws RemoteException
	 */
	public void giveResult(Signature resultat) throws RemoteException;
}

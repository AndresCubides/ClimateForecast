package utils;

/**
 *
 * @author Louis Reymondin
 *
 */

public class UserWait extends Wait implements Runnable {
	/**
	 * Construcor of the Waiting thread
	 *
	 */
	public UserWait() {
		new Thread(this).start();
	}
	
	/**
	 * Working method of the waiting thead
	 */
	@Override
	public void run()
	{
		try
		{
			/* Listening the user keyboard waiting for enter key */
			System.in.read();
			System.out.println(
					"\n\n#----------------------------------------------------------------#\n"+
					"#                     ! A key has been pressed !                 #\n"+
					"# The current calculation will finish and the software will quit #\n"+
					"#   This could take some minutes, please don't kill the process  #\n"+
					"#                without the permission of the admin             #\n"+
					"#----------------------------------------------------------------#\n\n");
			/* The user want to quit */
			setContinuer(false);
		}
		catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
	}
}

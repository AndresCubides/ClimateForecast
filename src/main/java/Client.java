import utils.FileConsoleOutputStream;
import utils.Parameter;

import java.io.FileNotFoundException;
import java.util.Locale;

/**
 *
 * @author Louis Reymondin
 *
 */
public class Client {
	public static void main(String[] args) throws FileNotFoundException {
		Locale.setDefault(new Locale("en", "US"));
		if (args.length < 2) {
			System.err.println("2 arguments required");
			return;
		}
		final String config = args[0];
		// When using Client, a UNIQUE id must be given to each client (for logging)
		final int uniqueID = Integer.parseInt(args[1]);

		FileConsoleOutputStream.redirectOutput("log", "client_" + uniqueID + "_out.txt", "client_" + uniqueID + "_err.txt");
		Parameter.getInstance(config);
		Worker.getInstance().start();
	}
}

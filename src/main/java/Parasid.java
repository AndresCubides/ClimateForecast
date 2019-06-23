import managers.Manager;
import utils.FileConsoleOutputStream;
import utils.Parameter;

import java.io.FileNotFoundException;
import java.util.Locale;

public class Parasid {
	public static void main(String[] args) throws FileNotFoundException {
		Locale.setDefault(new Locale("en", "US"));
		if (args.length < 2) {
			System.err.println("Error: 2 arguments required");
			return;
		}
		final int action = Integer.parseInt(args[1]);
		try {
			FileConsoleOutputStream.redirectOutput("log", "process_"+action + "_out.txt", "process_"+action + "_err.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Parameter.getInstance(args[0]);
		new Parasid(action);
	}

	public Parasid(int action) {
		try {
			Manager.register(action);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

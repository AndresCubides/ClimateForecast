package utils;

import java.io.*;

/**
 * An outputstream that outputs to both Console and a File (used for logging)
 */
public class FileConsoleOutputStream extends OutputStream {
  
  /**
   * Redirect output to the given files while still displaying it on the console
   */
  public static void redirectOutput (String outDir, String outFileName, String errFileName) throws FileNotFoundException {
    new File(outDir).mkdirs();
    final PrintStream origOut = System.out;
    final PrintStream origErr = System.err;
    
    final FileConsoleOutputStream newOut = new FileConsoleOutputStream(outDir+File.separator+outFileName, origOut);
    final FileConsoleOutputStream newErr = new FileConsoleOutputStream(outDir+File.separator+errFileName, origErr);
    
    System.setOut(new PrintStream(newOut, true));
    System.setErr(new PrintStream(newErr, true));
  }
  
  private final FileOutputStream file;
  private final PrintStream console;
  
  public FileConsoleOutputStream(String outFileName, PrintStream console) throws FileNotFoundException {
    this.file = new FileOutputStream(outFileName);
    this.console = console;
  }

  @Override
  public void write(int arg) throws IOException {
    file.write(arg);
    console.write(arg);
  }
  
  @Override
  public void flush () {
    try {
      file.flush();
    } catch (IOException e) {
      console.print(e);
    }
    console.flush();
  }
  
  @Override
  public void close () {
    try {
      file.close();
    } catch (IOException e) {
      console.print(e);
    }
    //don't close console
  }
}

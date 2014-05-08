package image_config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigChecker {

  private static int exitCode = 0;
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub

    /**
     * read in and print out ... the program arguments 
     */
    int cnt = 0;
    for ( String arg : args )
    {
      cnt++;
      System.out.printf("arg[%d]: %s\n", cnt, arg);      
    }
    
    /**
     * the first argument is taken as path to folder 'image_config' ... read in the major/minor config codes list ...
     */
    Path codesFilePath = Paths.get(args[0] + "\\csf_in\\csf_major_codes.txt");
    try {
      List<String> codesFileLines = Files.readAllLines(codesFilePath,StandardCharsets.UTF_8);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    
//    public void listf(String directoryName, ArrayList<File> files) {
//      File directory = new File(directoryName);
//
//      // get all the files from a directory
//      File[] fList = directory.listFiles();
//      for (File file : fList) {
//          if (file.isFile()) {
//              files.add(file);
//          } else if (file.isDirectory()) {
//              listf(file.getAbsolutePath(), files);
//          }
//      }
//    }    
    
    /**
     * good bye cruel world ! ... I'll tell you a last number ... 
     */
    if (exitCode != 0) {
      System.out.printf("Error: %d\n", exitCode);
    }
    System.exit(exitCode);
  }

}

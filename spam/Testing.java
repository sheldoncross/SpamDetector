package spam;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.math.*;

public class Testing{

  public static TreeMap computePrSF(TreeMap prSW, Path path){
    TreeMap<String, Float> fileProb = new TreeMap();

    try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
      //iterate over directory
      for(Path entry : stream){
        float nSum = 0;

        //Get name of file
        String fileName = entry.getFileName().toString();

        //Get path of file
        Path file = FileSystems.getDefault().getPath(path + "/" + fileName);

        Scanner scanner = new Scanner(file);

        List<String> inFile = new ArrayList<String>();
        inFile.removeAll(inFile);

        while(scanner.hasNext()){
          float sw = 0;
          float n = 0;
          //Get next word
          String word = scanner.next();

          if(inFile.contains(word) == false){
            if(prSW.containsKey(word)){
              sw = (float)(Math.log(1.0f-(float)prSW.get(word)) - Math.log((float)prSW.get(word)));
              n = sw;
            }
          }
          nSum = nSum + n;
          inFile.add(word);
        }

        float prSF = 1.0f/(1.0f+(float)Math.exp(nSum));
        //System.out.println(prSF);

        fileProb.put(fileName, prSF);
      }
    }catch(IOException x){
      System.err.println(x);
    }

    return fileProb;
  }
}

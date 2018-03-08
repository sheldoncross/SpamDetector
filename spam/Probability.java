package spam;

import spam.SpamDetector.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Probability{

  public static TreeMap findHSProb(HashMap<String, Integer> wordCount, Path dir1, Path dir2){
      //Map to store probabilities & float to count files
      TreeMap probabilities = new TreeMap();
      float numOfFiles = 0;

      //Open the first directory
      try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir1)){
        //Count the number of files
        for(Path entry : stream){
          ++numOfFiles;
        }
      }catch(IOException x){
        System.err.println(x);
      }

      //Caluclate probability for each word in directory
      for(HashMap.Entry<String, Integer> word : wordCount.entrySet()){
        float prob = 0;
        prob = word.getValue()/numOfFiles;

        //Put probability in map
        probabilities.put(word.getKey(), prob);
      }

      numOfFiles = 0;
      //Open the second directory
      try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir2)){
        //Count the number of files
        for(Path entry : stream){
          ++numOfFiles;
        }
      }catch(IOException x){
        System.err.println(x);
      }

      //Caluclate probability for each word in directory
      for(HashMap.Entry<String, Integer> word : wordCount.entrySet()){
        float prob = 0;
        prob = word.getValue()/numOfFiles;

        //Add to previous probability if word was in last file
        if(probabilities.containsKey(word.getKey())){
          probabilities.put(word.getKey(), (word.getValue() + prob));
        }else{
          probabilities.put(word.getKey(), prob);
        }
      }

      return probabilities;
    }

  /*Loop over every word and store the probability that file is spam if
  it contains the word*/
  public static TreeMap findSWProb(TreeMap<String, Float> spamProb, TreeMap<String, Float> hamProb){
    //Map to store probability and sets of words from each directory
    TreeMap allProbs = new TreeMap();

    //Caluclate probability for each word
    for(Map.Entry<String, Float> entry : spamProb.entrySet()){
      float prob = 0;
      if(hamProb.containsKey(entry.getKey())){
        prob = ((float)spamProb.get(entry.getKey())/((float)spamProb.get(entry.getKey())+(float)hamProb.get(entry.getKey())));
      }else{
        prob = 1.0f;
      }
      allProbs.put(entry.getKey(), prob);
    }
    for(Map.Entry<String, Float> entry : hamProb.entrySet()){
      float prob = 0;

      if(allProbs.containsKey(entry.getKey()) == false){
        prob = 0.5f;
        allProbs.put(entry.getKey(), prob);
      }
    }
    return allProbs;
  }

}

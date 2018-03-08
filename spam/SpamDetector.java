package spam;


import java.util.*;
import java.nio.file.*;
import java.io.*;
import javafx.collections.*;

public class SpamDetector{

  public static ObservableList<TestFile> result;
  private static float accuracy;
  private static float precision;
  private static ArrayList<String> spammiest;

  public static void start(File mainDirectory){
    System.out.println(ProbTable.mainDirectory.getPath());

    /*Create two maps to store the frequency that a word appears with
    a ham and spam file*/
    HashMap trainSpam = new HashMap();
    HashMap trainHam = new HashMap();

    //Count how many times each file appears in ham and spam
    Path spamPath = FileSystems.getDefault().getPath(mainDirectory.getPath() + "/train/spam/");
    trainSpam = readFiles(spamPath);
    Path hamPath = FileSystems.getDefault().getPath(mainDirectory.getPath() + "/train/ham/");
    trainHam = readFiles(hamPath);

    /*Create two maps to store the probability that a word appears
    in ham or spam*/
    TreeMap<String, Float> prWordSpam = Probability.findHSProb(trainSpam, spamPath, hamPath);
    TreeMap<String, Float> prWordHam = Probability.findHSProb(trainHam, hamPath, spamPath);
    ArrayList<Map.Entry<String, Float>> sorted = new ArrayList<Map.Entry<String, Float>>();
    ArrayList<String> iestWords = new ArrayList<String>();
    for(Map.Entry<String, Float> entry : prWordHam.entrySet()){
      sorted.add(entry);
    }
    for(int i = sorted.size()-5; i < sorted.size(); i++){
      iestWords.add((sorted.get(i)).getKey());
    }

    spammiest = iestWords;

    /*Create a map to store the probability that a file is spam if it contains
    a specific word*/
    TreeMap<String, Float> prSW = Probability.findSWProb(prWordSpam, prWordHam);

    TreeMap<String, Float> prSFSpam = new TreeMap();
    TreeMap<String, Float> prSFHam = new TreeMap();

    Path testSpamPath = FileSystems.getDefault().getPath(mainDirectory.getPath() + "/test/spam");
    Path testHamPath = FileSystems.getDefault().getPath(mainDirectory.getPath() + "/test/ham");

    prSFSpam = Testing.computePrSF(prSW, testSpamPath);
    prSFHam = Testing.computePrSF(prSW, testHamPath);

    setResult(prSFSpam, prSFHam);
  }

  //return hashmap of each word with its frequency in folder
  private static HashMap readFiles(Path path){
    HashMap wordCount = new HashMap();

      //Open directory stream return type path for every file in directory
      try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
        //iterate over directory
        for(Path entry : stream){
          //Get name of file
          String fileName = entry.getFileName().toString();

          //Get path of file
          Path file = FileSystems.getDefault().getPath(path + "/" + fileName);

          Scanner scanner = new Scanner(file);

          //Delimiters
          scanner.useDelimiter("[,\\s\\.;:/!=?-@$%&_*<>\\(\\)\\[\\]\\{\\}\\t\\n\\\\\"]");

          List<String> inFile = new ArrayList<String>();
          inFile.removeAll(inFile);

          //Count all words in file
          while(scanner.hasNext()){
            //Get next word
            String word = scanner.next();

            //Check if word matches valid pattern
            if(isWord(word)){
              //Check if word has been found in folder
              if(wordCount.containsKey(word)){
                //Check if word has already been found in file
                if(inFile.contains(word) == false){
                  wordCount.put(word, (int)wordCount.get(word)+1);
                  inFile.add(word);
                }
              }else{
                wordCount.put(word, 1);
                inFile.add(word);
              }
            }
          }
        }
      }catch(IOException x){
        System.err.println(x);
      }
    return wordCount;
  }

  private static boolean isWord(String word){
   String pattern = "^[a-zA-Z]+$";
   if(word.matches(pattern)){
     return true;
   }else{
     return false;
   }
  }

  public static ArrayList<String> getSpammiest(){
    return spammiest;
  }

  public static float getAccuracy(){
    return accuracy;
  }

  public static float getPrecision(){
    return precision;
  }

  public static void setResult(TreeMap<String, Float> prSFSpam, TreeMap<String, Float> prSFHam){
    ObservableList<spam.TestFile> finalList = FXCollections.observableArrayList();

    for(Map.Entry<String, Float> entry : prSFSpam.entrySet()){
      TestFile currentFile = new TestFile(entry.getKey(), entry.getValue(), "spam");
      currentFile.setSpamProbability(Double.parseDouble(currentFile.getSpamProbRounded()));
      finalList.add(currentFile);
    }
    for(Map.Entry<String, Float> entry : prSFHam.entrySet()){
      TestFile currentFile = new TestFile(entry.getKey(), entry.getValue(), "ham");
      currentFile.setSpamProbability(Double.parseDouble(currentFile.getSpamProbRounded()));
      finalList.add(currentFile);
    }

    float numOfGuesses = 0;
    float numOfTrueNeg = 0;
    float numOfTruePos = 0;
    float numOfFalsePos = 0;
    for(TestFile file : finalList){
      numOfGuesses++;

      if(file.getSpamProbability() < .50 && file.getActualClass() == "ham"){
        ++numOfTrueNeg;
      }
      if(file.getSpamProbability() > .50 && file.getActualClass() == "spam"){
        ++numOfTruePos;
      }
      if(file.getSpamProbability() < .50 && file.getActualClass() == "spam"){
        ++numOfFalsePos;
      }
    }

    accuracy = (numOfTruePos+numOfTrueNeg)/numOfGuesses;
    precision = numOfTruePos/(numOfFalsePos+numOfTruePos);
    result = finalList;

  }

  public static ObservableList<spam.TestFile> getResult(){
    return result;
  }
}

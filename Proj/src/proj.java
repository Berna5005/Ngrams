// Importing required classes
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class proj {
    public static void main(String[] args) throws IOException{
        long start = System.currentTimeMillis();

        // Creating a path choosing file from local directory by creating an object of Path class
        Path fileName = Path.of("463_Paper.txt");

        // Now calling Files.readString() method to read the file and saving it as a string
        String str = Files.readString(fileName);
        //Reading only the words and numbers
        str = str.replace("\n", " ").replace("\r", "");
        str = str.replaceAll("[^a-zA-Z0-9]", " ");
        str = str.replaceAll("\\s{2,}", " ").trim();

        //n-gram
        int n=3;
        System.out.println("OCCURRENCES OF N-GRAMS FOR n EQUALS: "+n);
        System.out.println();
        printNGrams(calculateNGrams(str, n));
        System.out.println("---------------------------------------------------------");
        System.out.println("RELATIVE FREQUENCIES OF THE N-GRAMS");
        System.out.println();
        printNGramsD(RelativeFrequency(str, n));


        System.out.println();
        System.out.println("---------------------------------------------------------");
        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
    }

    //function for calculating the occurrence of n-gram
    public static Map<String, Integer> calculateNGrams(String inputString, int n){
        //saving n-grams in finalOutput
        Map<String, Integer> finalOutput = new HashMap<>();
        //Extracting all the single words from the string
        String[] singleWords = inputString.split(" ");
        int nNumber = singleWords.length-n+1;

        //Building the n-gram and calculating its occurrence
        for (int i=0; i<nNumber; i++){
            StringBuilder tmp = new StringBuilder();
            for (int j=0; j<n; j++){
                if (i>0) tmp.append(" ");
                tmp.append(singleWords[i+j]);
            }
            //if finalOutput contains the n-gram just increase its occurrence
            if (finalOutput.containsKey(tmp.toString()))
                finalOutput.put(tmp.toString(), finalOutput.get(tmp.toString())+1);
            //if not add it as a new n-gram
            else
                finalOutput.put(tmp.toString(), 1);
        }
        //Return the n-grams
        return finalOutput;
    }

    //Print the n-grams with their occurrence
    public static void printNGrams(Map<String, Integer> nGrams){
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue()+"\n");
        }
    }

    //Print the occurrence with their relative frequency
    public static void printNGramsD(Map<String, Double> nGrams){
        for (Map.Entry<String, Double> entry : nGrams.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue()+"\n");
        }
    }

    //Function for calculating the relative frequency of each n-gram
    public static Map<String, Double> RelativeFrequency(String str, int n) {
        Map<String, Double> finalOutput = new HashMap<>();
        //Occurrences for the n-grams the program is calculating
        Map<String, Integer> NGrams = calculateNGrams(str, n);
        //check frequency for every entry
        for (Map.Entry<String, Integer> entry : NGrams.entrySet()) {
            String s = entry.getKey();
            String[] singleWords = s.split("\\s+");
            //divisor
            double sum = 0;
            for (int i = 1; i < n; i++) {
                //Occurrence of all n-grams before n
                Map<String, Integer> NGramI = calculateNGrams(str, i);
                //check for every entry
                for (Map.Entry<String, Integer> entryI : NGramI.entrySet()) {
                    String sI = entryI.getKey();
                    String[] singleWordsI = sI.split("\\s+");
                    //compare the first words of the n-grams
                    if (singleWords[0].equals(singleWordsI[0])) {
                        //if the words are same increase the divisor by the number of occurrence of that n-gram
                        sum += entryI.getValue();
                    }
                }
            }
            //save the n-gram with its relative frequency
            finalOutput.put(entry.getKey(), entry.getValue()/sum);
        }
        return finalOutput;
    }
}
// Importing required classes
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class SequentialPart {
    public static void main(String[] args) throws IOException{
        long start = System.nanoTime();

        // Creating a path choosing file from local directory by creating an object of Path class
        Path fileName = Path.of("463_Paper.txt");

        // Now calling Files.readString() method to read the file and saving it as a string
        String str = Files.readString(fileName);
        //Reading only the words and numbers
        str = str.replace("\n", " ").replace("\r", "");
        str = str.replaceAll("[^a-zA-Z0-9]", " ");
        str = str.replaceAll("\\s{2,}", " ").trim();

        //n-gram
        int n=4;
        int num_of_cycles = 3101;
        // Count cycles
        int cycles = calculateCycles(str, n);
        System.out.println("Total cycles: " + cycles);

        System.out.println("OCCURRENCES OF N-GRAMS FOR n EQUALS: "+n);
        System.out.println();
        printNGrams(calculateNGrams(str, n, num_of_cycles));
        System.out.println("---------------------------------------------------------");
        System.out.println("RELATIVE FREQUENCIES OF THE N-GRAMS");
        System.out.println();
        printNGramsD(RelativeFrequency(str, n, num_of_cycles));


        System.out.println();
        System.out.println("---------------------------------------------------------");
        long end = System.nanoTime();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1e9) + " seconds");
    }

    // Function to calculate cycles
    public static int calculateCycles(String inputString, int n) {
        String[] singleWords = inputString.split(" ");
        int nNumber = singleWords.length - n + 1;
        return nNumber;
    }

    //function for calculating the occurrence of n-gram
    public static Map<String, Integer> calculateNGrams(String inputString, int n, int num_of_cycles){
        //saving n-grams in finalOutput
        Map<String, Integer> finalOutput = new HashMap<>();
        //Extracting all the single words from the string
        String[] singleWords = inputString.split(" ");
        int nNumber = singleWords.length-n+1;

        //Building the n-gram and calculating its occurrence
        for (int i=0; i<nNumber && i<num_of_cycles; i++){
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
    public static void printNGramsD(Map<String, Double> nGrams) {
        for (Map.Entry<String, Double> entry : nGrams.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + "\n");
        }
    }

    //Function for calculating the relative frequency of each n-gram
    public static Map<String, Double> RelativeFrequency(String str, int n, int num_of_cycles) {
        Map<String, Double> finalOutput = new HashMap<>();
        Map<String, Integer> NGrams = calculateNGrams(str, n, num_of_cycles);

        // Calculate the total occurrences of all n-grams that start with a particular prefix
        Map<String, Integer> prefixOccurrences = new HashMap<>();
        for (Map.Entry<String, Integer> entry : NGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.substring(0, n - 1); // Extract prefix
            prefixOccurrences.put(prefix, prefixOccurrences.getOrDefault(prefix, 0) + entry.getValue());
        }

        // Calculate relative frequencies
        for (Map.Entry<String, Integer> entry : NGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.substring(0, n - 1); // Extract prefix
            int prefixOccurrence = prefixOccurrences.getOrDefault(prefix, 1);
            double relativeFrequency = (double) entry.getValue() / prefixOccurrence;
            finalOutput.put(nGram, relativeFrequency);
        }

        return finalOutput;
    }
}
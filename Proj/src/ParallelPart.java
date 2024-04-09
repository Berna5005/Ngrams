import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelPart {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        long start = System.nanoTime();

        Path fileName = Path.of("463_Paper.txt");
        String str = Files.readString(fileName);
        //Reading only the words and numbers
        str = str.replace("\n", " ").replace("\r", "");
        str = str.replaceAll("[^a-zA-Z0-9]", " ");
        str = str.replaceAll("\\s{2,}", " ").trim();

        int n = 4;
        int numThreads = Runtime.getRuntime().availableProcessors(); // Number of available processors

        // Divide the input string into smaller chunks
        List<String> chunks = splitInput(str, numThreads);

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Submit tasks to calculate n-grams for each chunk
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        for (String chunk : chunks) {
            Callable<Map<String, Integer>> task = () -> calculateNGrams(chunk, n);
            futures.add(executorService.submit(task));
        }

        // Merge results from all threads
        Map<String, Integer> mergedNGrams = mergeResults(futures);

        // Calculate relative frequencies
        Map<String, Double> relativeFrequencies = calculateRelativeFrequencies(str, n, mergedNGrams);

        // Print results
        System.out.println("OCCURRENCES OF N-GRAMS FOR n EQUALS: " + n);
        System.out.println();
        printNGrams(mergedNGrams);
        System.out.println("---------------------------------------------------------");
        System.out.println("RELATIVE FREQUENCIES OF THE N-GRAMS");
        System.out.println();
        printNGramsD(relativeFrequencies);

        long end = System.nanoTime();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1e9) + " seconds");

        // Shutdown the executor service
        executorService.shutdown();
    }

    // Split the input string into smaller chunks
    private static List<String> splitInput(String input, int numChunks) {
        List<String> chunks = new ArrayList<>();
        int chunkSize = input.length() / numChunks;
        int startIndex = 0;
        int endIndex = chunkSize;

        for (int i = 0; i < numChunks; i++) {
            if (i == numChunks - 1) {
                endIndex = input.length();
            }
            chunks.add(input.substring(startIndex, endIndex));
            startIndex = endIndex;
            endIndex = Math.min(endIndex + chunkSize, input.length());
        }

        return chunks;
    }

    // Merge results from all threads
    private static Map<String, Integer> mergeResults(List<Future<Map<String, Integer>>> futures) throws InterruptedException, ExecutionException {
        Map<String, Integer> mergedResult = new ConcurrentHashMap<>();
        for (Future<Map<String, Integer>> future : futures) {
            Map<String, Integer> result = future.get();
            // Merge result into mergedResult
            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                mergedResult.put(entry.getKey(), mergedResult.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        return mergedResult;
    }

    // Calculate n-grams for a given chunk of text
    private static Map<String, Integer> calculateNGrams(String inputString, int n) {
        Map<String, Integer> finalOutput = new HashMap<>();
        String[] singleWords = inputString.split(" ");
        int nNumber = singleWords.length - n + 1;

        for (int i = 0; i < nNumber; i++) {
            StringBuilder tmp = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (i > 0) tmp.append(" ");
                tmp.append(singleWords[i + j]);
            }
            finalOutput.put(tmp.toString(), finalOutput.getOrDefault(tmp.toString(), 0) + 1);
        }
        return finalOutput;
    }

    // Calculate relative frequencies
    private static Map<String, Double> calculateRelativeFrequencies(String str, int n, Map<String, Integer> nGrams) throws InterruptedException, ExecutionException {
        Map<String, Double> finalOutput = new HashMap<>();
        Map<String, Integer> prefixOccurrences = new ConcurrentHashMap<>(); // Use ConcurrentHashMap for thread safety

        List<Callable<Void>> tasks = new ArrayList<>();

        // Create tasks to calculate prefix occurrences
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.substring(0, n - 1); // Extract prefix
            tasks.add(() -> {
                prefixOccurrences.put(prefix, prefixOccurrences.getOrDefault(prefix, 0) + entry.getValue());
                return null;
            });
        }

        // Execute tasks in parallel
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.invokeAll(tasks);
        executorService.shutdown();

        // Calculate relative frequencies
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.substring(0, n - 1); // Extract prefix
            int prefixOccurrence = prefixOccurrences.getOrDefault(prefix, 1);
            double relativeFrequency = (double) entry.getValue() / prefixOccurrence;
            finalOutput.put(nGram, relativeFrequency);
        }

        return finalOutput;
    }

    // Print the n-grams with their occurrence
    public static void printNGrams(Map<String, Integer> nGrams) {
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + "\n");
        }
    }

    // Print the occurrence with their relative frequency
    public static void printNGramsD(Map<String, Double> nGrams) {
        for (Map.Entry<String, Double> entry : nGrams.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + "\n");
        }
    }
}

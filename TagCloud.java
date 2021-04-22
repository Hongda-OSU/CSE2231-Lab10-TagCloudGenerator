import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This program counts word occurrences in a given text file and outputs an HTML
 * document with a table of the words and counts listed in alphabetical order.
 *
 * @author Hongda Lin, Net Zhang
 *
 */
public final class TagCloud {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloud() {
    }

    /**
     * Compare {@code String}s in Number of appearance order.
     */
    private static class CountComparator
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int result = o1.getValue().compareTo(o2.getValue());
            if (result == 0) {
                result = o1.getKey().toLowerCase()
                        .compareTo(o1.getKey().toLowerCase());
            }
            return result;
        }
    }

    /**
     * Compare {@code String}s in Alphabetic order.
     */
    private static class AlphabetComparator
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            String s1 = o1.getKey().toLowerCase();
            String s2 = o2.getKey().toLowerCase();
            int result = s1.compareTo(s2);
            if (result == 0) {
                result = o1.getValue().compareTo(o2.getValue());
            }
            return result;
        }
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        for (int i = 0; i < str.length(); ++i) {
            char temp = str.charAt(i);
            if (!strSet.contains(temp)) {
                strSet.add(temp);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        StringBuilder word = new StringBuilder();
        if (separators.contains(text.charAt(position))) {
            int stopPosition = position;
            while (stopPosition < text.length()
                    && separators.contains(text.charAt(stopPosition))) {
                word.append(text.charAt(stopPosition));
                stopPosition++;
            }
        } else {
            int stopPosition = position;
            while (stopPosition < text.length()
                    && !separators.contains(text.charAt(stopPosition))) {
                word.append(text.charAt(stopPosition));
                stopPosition++;
            }
        }
        return word.toString();
    }

    /**
     * Read all of the words from the file, count their appearances and store
     * them correspondingly in a map. Meanwhile store the words in a
     * alphabetical order in a queue.
     *
     * @param file
     *            the input stream
     * @param wordMap
     *            the map that stores the words and their number of appearances
     *            in the file
     * @updates file
     * @replaces words, wordMap
     * @requires file.is_open
     * @ensures file.is_open and file.content is null and wordMap contains all
     *          of the words as keys, and its corresponding number of
     *          appearances as value. The queue words contains the unique words
     *          and store them in a alphabetic order.
     */
    private static void getWords(BufferedReader file,
            Map<String, Integer> wordMap) {

        wordMap.clear();
        /*
         * Define separator characters
         */
        final String separatorStr = " \t\n\r,-.!?[]';:/()0123456789_\"*`";
        Set<Character> separatorSet = new HashSet<>();
        generateElements(separatorStr, separatorSet);
        /*
         * Read the file line by line
         */
        try {
            String tempLine = file.readLine();
            while (tempLine != null) {
                int position = 0;
                while (position < tempLine.length()) {
                    String token = nextWordOrSeparator(tempLine, position,
                            separatorSet);
                    // All to lower case
                    token = token.toLowerCase();
                    // Check whether the token is a word or separator
                    if (!separatorSet.contains(token.charAt(0))) {
                        // If the word map already contains token
                        if (wordMap.containsKey(token)) {
                            Integer tempValue = wordMap.remove(token);
                            wordMap.put(token, tempValue + 1);
                        } else {
                            wordMap.put(token, 1);
                        }
                    }
                    position += token.length();
                }

                tempLine = file.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from file");
        }

    }

    /**
     * Sort the words by their number of appearance in the file.
     *
     * @param wordMap
     *            A map that contains all of the words in a file and stores the
     *            unique words as keys, their number of appearances as values
     * @clears wordMap
     * @return A sorting machine that sort all of the words by their number of
     *         appearance
     * @ensures The returned sorting machine is in Extraction mode
     */
    private static List<Map.Entry<String, Integer>> sortWordsCount(
            Map<String, Integer> wordMap) {

        Comparator<Map.Entry<String, Integer>> cc = new CountComparator();
        List<Map.Entry<String, Integer>> stCount = new LinkedList<>();
        Set<Map.Entry<String, Integer>> wordMapView = wordMap.entrySet();
        Iterator<Map.Entry<String, Integer>> iter = wordMapView.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> tempEntry = iter.next();
            stCount.add(tempEntry);
        }
        // Sort the List based on the number of appearance
        Collections.sort(stCount, cc);

        return stCount;
    }

    /**
     * Calculate the scaled font size basing on the counts of the word.
     *
     * @param fontMax
     *            The font size for the words that has the largest count
     * @param fontMin
     *            The font size for the words that has the minimum count
     * @param count
     *            The number of appearance of the word
     * @param countMin
     *            The minimum number of appearance in the selected words
     * @param countMax
     *            The maximum number of appearance in the selected words
     * @return The scaled font size
     * @ensures scaleSize is the scaled font size of the selected words
     *
     */
    private static int scaleSize(int fontMax, int fontMin, int count,
            int countMin, int countMax) {

        int scaled;
        if (countMin == countMax) {
            scaled = 1;
        } else {
            scaled = (fontMax - fontMin) * (count - countMin)
                    / (countMax - countMin) + fontMin;
        }
        return scaled;
    }

    /**
     * Sort the top n words in a alphabetical order.
     *
     * @param fontSize
     *            A map that used to store the words we want to display and
     *            their scaled font size
     * @param n
     *            The top number of words that we want to display
     * @param fontMax
     *            The font size for the words that has the largest count
     * @param fontMin
     *            The font size for the words that has the minimum count
     * @param stCount
     *            A sorting machine that sorts all of the words in the file by
     *            their number of appearance
     * @replaces fontSize
     * @updates stCount
     * @requires {@code stCount} is in extraction mode {@code n} is in between 1
     *           and the size of {@code stCount}
     * @return A sorting machine that sorts the top {@code n} words through
     *         alphabetical order
     * @ensures {@code sortWordsAlphabet} sorts the top {@code n} words through
     *          alphabetical order. {@code sortWordsAlphabet} is in extraction
     *          mode. Meanwhile {@code fontSize} stores the top {@code n} words
     *          as its keys and their scaled display font size as its
     *          corresponding value
     */
    private static List<Map.Entry<String, Integer>> sortWordsAlphabet(
            Map<String, Integer> fontSize, int n, int fontMax, int fontMin,
            List<Map.Entry<String, Integer>> stCount) {
        // replace fontSize
        // check stCount is in extraction mode
        fontSize.clear();

        Comparator<Map.Entry<String, Integer>> ac = new AlphabetComparator();
        List<Map.Entry<String, Integer>> stAlphabet = new LinkedList<>();

        int countMin = 0;
        int countMax = 0;
        for (int i = 0; i < n; i++) {
            int last = stCount.size() - 1;
            Map.Entry<String, Integer> tempEntry = stCount.remove(last);
            if (i == 0) {
                countMax = tempEntry.getValue();
            } else if (i == n - 1) {
                countMin = tempEntry.getValue();
            }
            stAlphabet.add(tempEntry);
        }
        // Sort the List based on the alphabetical order
        Collections.sort(stAlphabet, ac);

        Iterator<Map.Entry<String, Integer>> iter = stAlphabet.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> tempEntry = iter.next();
            int scaledSize = scaleSize(fontMax, fontMin, tempEntry.getValue(),
                    countMin, countMax);
            fontSize.put(tempEntry.getKey(), scaledSize);
        }

        return stAlphabet;
    }

    /**
     * Output the HTML file that contains a tag cloud.
     *
     * @param out
     *            the output stream
     * @param fileName
     *            the output file name
     * @param stAlphabet
     *            A sorting machine that sorts the top n words through
     *            alphabetical order
     * @param fontSize
     *            A map that used to store the words we want to display and
     *            their scaled font size
     * @updates out.content, stAlphabet
     * @requires out.is_open
     * @ensures out.content has HTML lines that generate a tag cloud where words
     *          are displayed in alphabetical orders, and their font size is
     *          scaled basing on their number of appearances in the text file.
     *          And the size {@code stAlphabet} is zero.
     */
    private static void outputTagCloud(PrintWriter out, String fileName,
            List<Map.Entry<String, Integer>> stAlphabet,
            Map<String, Integer> fontSize) {

        final String cssHref = "http://web.cse.ohio-state.edu/software/"
                + "2231/web-sw2/assignments/projects/"
                + "tag-cloud-generator/data/tagcloud.css";
        int topN = stAlphabet.size();

        out.println("<html>");
        // Header --------------------------------------------------------------
        out.println("<head>");
        out.println(
                "<title>Top " + topN + " words in " + fileName + "</title>");
        out.println("<link href=\"" + cssHref + "\""
                + " rel=\"stylesheet\" type = \"text/css\">");
        out.println("</head>");

        // Body ----------------------------------------------------------------

        out.println("<body>");
        out.println("<h2>Top " + topN + " words in " + fileName + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

        while (stAlphabet.size() > 0) {
            Map.Entry<String, Integer> tempEntry = stAlphabet.remove(0);
            String word = tempEntry.getKey();
            String size = "f" + fontSize.get(tempEntry.getKey());
            int count = tempEntry.getValue();

            // Size
            out.print("<span style=\"cursor:default\" class=\"" + size + "\" ");
            // Count
            out.print("title=\"count: " + count + "\">");
            // Word
            out.println(word + "</span>");

        }

        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) {

        final int fontMax = 48;
        final int fontMin = 11;

        // Read the name of the text file ====================================
        System.out.print("Enter the name of file location: ");
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in));
        String inputLocation;
        try {
            inputLocation = input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading stream from system input " + e);
            return;
        }

        // Read the name of the output tag cloud =============================
        System.out.print("Enter the name of the output html file location: ");
        String outputLocation;
        try {
            outputLocation = input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading stream from system input " + e);
            return;
        }

        // Open the text file =================================================
        BufferedReader fileInput;
        try {
            fileInput = new BufferedReader(new FileReader(inputLocation));
        } catch (IOException e) {
            System.err.println("Error opening file from file location " + e);
            return;
        }

        // Open an output file for the tag cloud ==============================
        PrintWriter fileOutput;
        try {
            fileOutput = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputLocation)));
        } catch (IOException e1) {
            System.err.println("Error creating file in the location " + e1);
            // close the opened text file
            try {
                fileInput.close();
            } catch (IOException e) {
                System.err.println("Error closing file" + e);
                return;
            }
            return;
        }

        // Read the text file to extract all the words =======================
        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        Map<String, Integer> fontSize = new HashMap<String, Integer>();

        getWords(fileInput, wordMap);

        int maxNumber = wordMap.size();

        // Ask the user for the number of words they want on the tag cloud ===
        System.out.print(
                "Please enter the number of words to be included in tag cloud"
                        + "[1, " + maxNumber + "]: ");

        String inputNum;
        try {
            inputNum = input.readLine();
        } catch (IOException e) {
            inputNum = null;
        }
        int userNum;
        try {
            userNum = Integer.parseInt(inputNum);
            while (userNum <= 1 || userNum > maxNumber) {
                System.out.print(
                        "Please enter the number of words to be included in tag cloud"
                                + "[1, " + maxNumber + "]: ");
                try {
                    inputNum = input.readLine();
                } catch (IOException e) {
                    inputNum = null;
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error reading stream from system input " + e);
            fileOutput.close();
            return;
        }

        // Sort the words ====================================================
        List<Entry<String, Integer>> stCount = sortWordsCount(wordMap);
        List<Entry<String, Integer>> stAlphabet = sortWordsAlphabet(fontSize,
                userNum, fontMax, fontMin, stCount);

        // Generate tag cloud HTML ===========================================
        outputTagCloud(fileOutput, inputLocation, stAlphabet, fontSize);

        // Don't forget to close =============================================
        try {
            input.close();
            fileInput.close();
            fileOutput.close();
        } catch (IOException e) {
            System.err.println("Error closing file" + e);
            return;
        }
    }
}

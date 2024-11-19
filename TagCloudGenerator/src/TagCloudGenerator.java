import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;
import components.utilities.FormatChecker;

/**
 * Program to take in an input file of words and outputs an HTML file of the
 * words and the amount of times each word appeared in the input file.
 *
 * @author Shyam Sai Bethina and Yihone Chu
 */
public final class TagCloudGenerator {

    /**
     * String for the list of possible separators (except for ").
     */
    private static final String SEPARATORSTRING = ". ,:;'{][}|/><?!`~1234567890"
            + "@#$%^&*()-_=+\"";

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    /**
     * Sorts Integer values of Map Pairs in decreasing order.
     */
    private static class IntegerSort
            implements Comparator<Map.Pair<String, Integer>> {

        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o2.value().compareTo(o1.value());
        }
    }

    /**
     * Sorts String keys of Map Pairs in alphabetical order.
     */
    private static class StringSort
            implements Comparator<Map.Pair<String, Integer>> {

        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o1.key().compareTo(o2.key());
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
     * @requires <pre>
     * {@code 0 <= position < |text|}
     * </pre>
     * @ensures <pre>
     * {@code nextWordOrSeparator =
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
     *      is not subset of separators)}
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int i = position;
        //if the character at the position is not a separator
        if (!separators.contains(text.charAt(position))) {
            while (i < text.length() && !separators.contains(text.charAt(i))) {
                i++;
            }
        }
        //
        else {
            // if the character at the position is a separator.
            while (i < text.length() && separators.contains(text.charAt(i))) {
                i++;
            }

        }
        //returns the next word or separator.
        return text.substring(position, i);
    }

    /**
     * Gets the lines from the input file stream and returns a queue of the
     * lines.
     *
     * @param in
     *            The input file stream
     * @return A queue of lines from the input file
     * @ensures Returned Queue is filled with lines from the input file stream
     *
     */
    public static Queue<String> getWords(SimpleReader in) {

        /*
         * Set to hold separators. Iterate through the string of separators and
         * add each unique one to the set.
         */
        Set<Character> separators = new Set1L<Character>();
        for (int i = 0; i < SEPARATORSTRING.length(); i++) {
            if (!separators.contains(SEPARATORSTRING.charAt(i))) {
                separators.add(SEPARATORSTRING.charAt(i));
            }
        }

        //Creates an empty queue to add in lines
        Queue<String> words = new Queue1L<>();

        /*
         * While the input file stream is not at the end, it gets the next line
         * within the input, and if the is not empty, it gets added to the Queue
         */
        while (!in.atEOS()) {
            int position = 0;
            String line = in.nextLine();
            line = line.toLowerCase();

            while (position < line.length()) {
                String next = nextWordOrSeparator(line, position, separators);
                //if the line is a word add it into the queue of words
                if (!separators.contains(next.charAt(0))) {
                    words.enqueue(next);
                }
                //increment the position
                position += next.length();
            }
        }

        return words;
    }

    /**
     * Scans through the input file and adds words and their counts to a map.
     * Words are based off of given separators.
     *
     * @param countMap
     *            map that holds words and counts
     * @param words
     *            input file to be counted
     * @replaces countMap
     * @requires input is open
     * @ensures <pre>every key in the map is a word from input and each key
     *               has a value for how many times it appears in input
     *          </pre>
     */
    private static void addToMap(Map<String, Integer> countMap,
            Queue<String> words) {
        /*
         * Clear existing map in case it's not empty.
         */
        countMap.clear();

        for (String word : words) {
            //if the map already has the string in it, increment its value
            if (countMap.hasKey(word)) {
                Map.Pair<String, Integer> temp = countMap.remove(word);
                countMap.add(word, temp.value() + 1);
            }
            //if the map does not have the string within, adds it to the map
            //with a count of 1.
            else {
                countMap.add(word, 1);
            }
        }

    }

    /**
     * Processing through the input textFile ({@code SimpleReader}) and assigns
     * the word and its occurrences in a {@code Map}.
     *
     * @param nWords
     *            the {@code integer} that is the number of words we want to
     *            show
     * @param words
     *            the {@code Map} containing all the words and its occurrences
     * @return sortWords the {@code SortingMachine} that has a Pair that
     *         contains the sorted words' name as key and the number of
     *         occurrences as the value
     * @clears {@code words}
     * @ensures <pre>
     * inFile's words = {@code Map}'s Key(words) and Value(occurrences)
     * </pre>
     */
    public static SortingMachine<Map.Pair<String, Integer>> sort(
            Map<String, Integer> words, int nWords) {

        assert words != null : "Violation of: words is not null";
        //comparators that the sorting machine will use
        Comparator<Map.Pair<String, Integer>> stringComparator = new StringSort();
        Comparator<Map.Pair<String, Integer>> integerComparator = new IntegerSort();
        //the sortin machines.
        SortingMachine<Map.Pair<String, Integer>> sortCount = new SortingMachine1L<>(
                integerComparator);
        SortingMachine<Map.Pair<String, Integer>> sortWords = new SortingMachine1L<>(
                stringComparator);

        //Sort by count
        while (words.size() != 0) {
            Map.Pair<String, Integer> temp = words.removeAny();
            sortCount.add(temp);
        }

        //Take the top n words with the highest counts and sort alphabetically.
        sortCount.changeToExtractionMode();
        int size = sortCount.size();
        for (int i = 0; i < size && i < nWords; i++) {
            Map.Pair<String, Integer> temp = sortCount.removeFirst();
            sortWords.add(temp);
        }

        return sortWords;

    }

    /**
     * Return a font size that corresponds to the given count of a word.
     *
     * @param oldValue
     *            the given count of a word in the text
     * @param largest
     *            the most counts within the sorting machine
     * @param smallest
     *            the least count within the sorting machine
     * @return returns bit of html code that has a font size that corresponds
     *
     *         the count of the word in the text.
     */
    private static String fontSize(int oldValue, int largest, int smallest) {

        int answer = (((oldValue - smallest) * (48 - 11))
                / (largest - smallest)) + 11;

        return "f" + answer;
    }

    /**
     * Outputs the header for the index HTML file.
     *
     * @param out
     *            The output file stream
     * @param nWords
     *            the number of words the user chose to have outputed
     * @param fileName
     *            The name of the file the user desired
     * @requires out.is_open
     * @ensures output file has the header for the index HTML file
     */
    public static void outputHeader(SimpleWriter out, String fileName,
            int nWords) {
        /*
         * Outputs the beginning code of the index HTML file to the output file
         * stream
         */
        out.println("<html>");
        out.println("   <head>");
        out.println("      <title>Top " + nWords + " words in " + fileName
                + "</title>");
        out.println(
                "      <link href=\"http://web.cse.ohio-state.edu/software/2231"
                        + "/web-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("      <link href=\"tagcloud.css\" rel=\"stylesheet\" "
                + "type=\"text/css\">");
        out.println("   </head>");
        out.println("   <body>");
        out.println(
                "      <h2>Top " + nWords + " words in " + fileName + "</h2>");
        out.println("      <hr>");
        out.println("      <div class=\"cdiv\">");
        out.println("         <p class=\"cbox\">");

    }

    /**
     * Outputs the footer for the index HTML file.
     *
     * @param out
     *            The output file stream
     * @requires out.is_open
     * @ensures output file has the closing braces for the index HTML file
     */
    public static void outputFooter(SimpleWriter out) {
        /*
         * Outputs the closing code of the index HTML file to the output file
         * stream
         */
        out.println("         </p>");
        out.println("      </div>");
        out.println("   </body>");
        out.print("</html>");
    }

    /**
     * Outputs the words and corresponding counts to the table in the index HTML
     * file.
     *
     * @param wordsSorted
     *            Sorting Machine of map pairs of the string and integers
     * @param out
     *            The output file stream
     * @requires out.is_open
     * @ensures output file has the code to output the table of words and counts
     *          in the HTML file.
     */
    public static void outputCounts(
            SortingMachine<Map.Pair<String, Integer>> wordsSorted,
            SimpleWriter out) {

        //gets the largest count
        int largest = 0;
        for (Map.Pair<String, Integer> temp : wordsSorted) {
            if (temp.value() > largest) {
                largest = temp.value();
            }
        }
        //gets smallest count
        int smallest = 0;
        for (Map.Pair<String, Integer> temp : wordsSorted) {
            if (temp.value() < largest) {
                smallest = temp.value();
            }
        }

        //change to extraction in order to remove things
        wordsSorted.changeToExtractionMode();
        while (wordsSorted.size() > 0) {

            Map.Pair<String, Integer> removedCount = wordsSorted.removeFirst();

            String fontSize = fontSize(removedCount.value(), largest, smallest);
            out.println("            <span style=\"cursor:default\" class=\""
                    + fontSize + "\" title=\"count: " + removedCount.value()
                    + "\">" + removedCount.key() + "</span>");
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        /*
         * Creates input file stream for user input and output file stream to
         * ask questions
         */
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        /*
         * Gets the input file name from user, and inputName becomes the answer
         */
        out.print("Enter location and name of input file: ");
        String inputName = in.nextLine();
        out.println();
        /*
         * This input file stream reads the input file using the name the user
         * inputed
         */
        SimpleReader inputFile = new SimpleReader1L(inputName);

        /*
         * Asks for the name of the output file name, and fileName becomes the
         * answer
         */
        out.print("Enter name of output file: ");
        String fileName = in.nextLine();

        /*
         * This output file stream creates a new file with the name the user
         * wanted
         */
        SimpleWriter outFile = new SimpleWriter1L(fileName);

        out.println();
        out.print("Number of words: ");

        String intInput = in.nextLine();

        while (!FormatChecker.canParseInt(intInput)) {
            out.println(
                    "That is not a valid input, please input a valid number: ");
            intInput = in.nextLine();
        }

        int n = Integer.parseInt(intInput);

        /*
         * Queue words is filled up with the words from the input file, and
         * inputFile stream is closed because it is not needed anymore
         */
        Queue<String> words = getWords(inputFile);

        /*
         * The counts map has all the words and counts of each word
         */
        Map<String, Integer> counts = new Map1L<String, Integer>();
        addToMap(counts, words);
        SortingMachine wordsSorted = sort(counts, n);

        /*
         * The next three lines outputs the header, the list, and the footer of
         * the HTML file to the desire file
         */
        outputHeader(outFile, inputName, n);
        outputCounts(wordsSorted, outFile);
        outputFooter(outFile);

        out.println("All done");

        /*
         * Closes all the input and output streams
         */
        inputFile.close();
        in.close();
        out.close();
        outFile.close();
    }

}

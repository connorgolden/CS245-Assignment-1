/*
 * @author: Connor Golden
 * @date: November 17, 2019
 * A spell checker based on Search Trie data structures for CS245 Data Structures & Algorithms.
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


public class CS245A1 {
    public static void main(String[] args){

        System.out.println("Welcome to Connor's Spell Checker!\n");

        if(args.length != 2)
        {
            System.out.println("Proper Usage is: java [inputfile] [outputfile]");
            System.out.println("Exiting...");
            System.exit(1);
        }

        try (InputStream input = new FileInputStream("a1properties.txt")) {

            //Load Properties file and extract value.
            Properties prop = new Properties();
            prop.load(input);
            String ds_type = prop.getProperty("storage");
            String dict_url = prop.getProperty("dict.url");

            //Looking at values from config file, this will choose what kind of tree to make.
            if (ds_type.compareTo("tree") == 0){
                //Make single-single-char-per-node Trie
                System.out.println("Selecting Tree...");
                Tree tree = new Tree();
                run(tree,dict_url, args);
            } else if (ds_type.compareTo("trie") == 0){
                //Make Prefix Trie
                System.out.println("Selecting Trie...");
                Trie trie = new Trie();
                run(trie, dict_url, args);
            } else {
                //Default to Trie if config is set to unknown value.
                System.out.println("Unknown Structure ... Defaulting to Trie...");
                Trie trie = new Trie();
                run(trie, dict_url, args);
            }

        } catch (IOException ex) {
            //Default to Trie if there is no config file. Can send null for URL so it will look for local.
            System.out.println("Config File Not Found.. Defaulting to Local english.0 and Trie...");
            Trie trie = new Trie();
            run(trie,null, args);
        }
    }

    /***
     * This function is called from main, it takes the Roots object determined by the config file. Then it opens
     * input.txt, and output.txt. For every line in input.txt, it will take the word, and check that it is in the tree.
     * If it is in the tree, it will write the same line to the output file. If the word is not there it will ask the
     * tree for a suggestion and put the suggested word in output.txt.
     *
     * Runtime: 0(n) where n is the amount of lines in input file. This runtime does not include the runtimes for
     * checkWord() or suggest(). Worst case runtime = 0(n * runtime of suggest()).
     * @param tree pass the Roots object, either Trie or Tree depending on config file.
     * @param englishUrl the URL from the config that points to the online english.0.
     * @param args the launch args are passed here so it can get the user defined names for input.txt and output.txt
     */
    private static void run(Roots tree, String englishUrl, String[] args){

        //Add the dictionary values to the tree before we start asking it questions.
        addDictionary(englishUrl, tree);

        //Todo redo the print lines so it makes sense, comment the extra verbose but useful ones.
        System.out.println("Input File: " + args[0] + " Output File:" + args[1]);
        System.out.println("Checking Words: \n");

        BufferedWriter output = null;
        try {
            //get output file
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[1]))));

            //get input file
            try(BufferedReader input = new BufferedReader(new FileReader(args[0]))) {
                String line;
                while ((line = input.readLine()) != null) {
                    //Loop through lines on input file.
                    line = line.replaceAll("[^a-zA-Z']", "").toLowerCase();
                    if (line.compareTo("") == 0){
                        continue;
                    }

                    if (tree.checkWord(line)){
                        //If the line (word) is a correct spelling, write the input line to the output file.
                        output.write(line);
                        output.newLine();

                        int i = 1;
                        System.out.printf("%-20s %-20s", line, "(Correct)");
                        System.out.println();

                    } else {
                        //If the word does not match, write the suggested string.
                        String suggest = tree.suggest(line);
                        output.write(suggest);
                        output.newLine();
                        System.out.printf("%-20s %-20s", line, "(Incorrect Suggested: " + suggest +")");
                        System.out.println();
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not find Input File. Exiting...");
                System.exit(1);
            }

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not find Output File");
            System.exit(1);
        }

        System.out.println("\nCompleted Spellcheck");
    }

    /***
     * This function will take a tree object and then find an english.0 file, either online from GitHub, or locally.
     * Once it finds this file, it will read it line by line, adding the words to the tree object we gave it.
     *
     * Runtime: 0(n) where n is the size of the input dictionary.
     * @param urlStr takes the URL of the online english.0 file we get from config file
     * @param tree Takes the Tree object we created in the runTree or runTrie function.
     */
    private static void addDictionary(String urlStr, Roots tree){

        System.out.println("Adding Dictionary...");
        int wordLimit = 100000000;


        Reader obj = null;

        if (urlStr != null) {

            URL url = null;
            try {
                url = new URL(urlStr);

                try {
                    obj = new InputStreamReader(url.openStream());
                } catch (IOException e) {
                    System.out.println("Unable to Access Online english.0. Defaulting to Local...");
                    obj = getLocalEnglish();
                }

            } catch (MalformedURLException e) {
                System.out.println("Invalid URL. Defaulting to Local...");
                obj = getLocalEnglish();
            }

        } else {
            System.out.println("URL for english.0 not found in config. Defaulting to Local...");
            obj = getLocalEnglish();
        }

        if (obj != null){
            try (BufferedReader br = new BufferedReader(obj)) {
                String line;
                int counter = 0;
                while ((line = br.readLine()) != null) {

                    if (line.compareTo("") != 0) {
                        //System.out.println(line);
                        // read each word from the file and check if it in the dictionary
                        tree.add(line.toLowerCase());

                        if (counter == wordLimit) {
                            break;
                        }
                        counter++;
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed File Input. Exiting.");
                System.exit(1);
            }

            System.out.println("Dictionary Added.");
        } else {
            System.out.println("Unable to find an english.0. Exiting...");
            System.exit(1);
        }
    }

    /***
     * getLocalEnglish() is called when we need to look for a local copy of engligh.0 of we can't connect to the web and
     * download the online version from GitHub. If we cant find the file locally we need to exit and give up because we
     * cannot build the dictionary.
     *
     * Runtime: 0(1) as it just returns the Reader object if it exists. Exits if it dosen't.
     * @return a FileReader object for the local english.0 file.
     */
    private static Reader getLocalEnglish(){
        Reader obj = null;
        try {
            obj = new FileReader("english.0");
            return obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Neither Online of Offline english.0 found. Exiting...");
            System.exit(1);
        }
        return null;
    }
}
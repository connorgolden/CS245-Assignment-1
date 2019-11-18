/*
REQUIRES JAVAFX LIB INSTALLED
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class CS245A1LiveCheck extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Build and add data to tree.

        Roots tree;

        String dict_url = null;
        try (InputStream input = new FileInputStream("a1properties.txt")) {

            //Load Properties file and extract value.
            Properties prop = new Properties();
            prop.load(input);
            String ds_type = prop.getProperty("storage");
            dict_url = prop.getProperty("dict.url");

            //Looking at values from config file, this will choose what kind of tree to make.
            if (ds_type.compareTo("tree") == 0){
                //Make single-single-char-per-node Trie
                System.out.println("Selecting Tree...");
                tree = new Tree();

            } else if (ds_type.compareTo("trie") == 0){
                //Make Prefix Trie
                System.out.println("Selecting Trie...");
                tree = new Trie();

            } else {
                //Default to Trie if config is set to unknown value.
                System.out.println("Unknown Structure ... Defaulting to Trie...");
                tree = new Trie();
            }

        } catch (IOException ex) {
            //Default to Trie if there is no config file. Can send null for URL so it will look for local.
            System.out.println("Config File Not Found.. Defaulting to Local english.0 and Trie...");
            tree = new Trie();
        }

        addDictionary(dict_url, tree);

        // Here on is the JavaFX =======================================================================================

        //creating label suggestion
        Text text1 = new Text("Suggestion:");

        //creating label enter text
        Text text2 = new Text("Enter Text:");

        //Creating label for what the suggestion is
        Label suggestion = new Label("");

        //Creating field for entering text
        TextField textField = new TextField();

        //Creating Buttons
        Button button2 = new Button("Clear");

        //Creating a Grid Pane
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(500, 300);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(text1, 0, 0);
        gridPane.add(suggestion, 1, 0);
        gridPane.add(text2, 0, 1);
        gridPane.add(textField, 1, 1);
        gridPane.add(button2, 1, 2);

        //Creating a scene object
        Scene scene = new Scene(gridPane);

        //Setting title to the Stage
        stage.setTitle("Connor Golden Live Spell Check");

        //Adding scene to the stage
        stage.setScene(scene);

        Roots finalTree = tree;
        textField.textProperty().addListener((observable, oldValue, newValue) -> {

            //System.out.println("textfield changed from " + oldValue + " to " + newValue);

            String result = newValue.substring(newValue.lastIndexOf(' ') + 1).trim()
                    .replaceAll("[^a-zA-Z]", "").toLowerCase();

            if (result.compareTo("") != 0){
                if (finalTree.checkWord(result)){
                    suggestion.setText(result);
                } else {
                    suggestion.setText(finalTree.suggest(result));
                }
            } else {
                suggestion.setText("");
            }

        });

        //Clear Button Listener
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
               textField.setText("");
               suggestion.setText("");
            }
        };

        button2.setOnAction(event);

        //show everything
        stage.show();
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
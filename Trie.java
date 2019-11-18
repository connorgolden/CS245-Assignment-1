/*
 * Single Char Per Node Trie Implementation.
 */

import java.util.Arrays;

public class Trie implements Roots{

    private TrieNode root;

    Trie(){
        root = new TrieNode();
    }

    private static class TrieNode {
        String prefix; // prefix stored in the node
        TrieNode[] children; // array of children (27 children, lowercase and [26] is hardcoded ')
        boolean isWord;

        TrieNode() {
            isWord = false;
            prefix = "";
            children = new TrieNode[27]; // initialize the array of children
        }

        TrieNode(String pref) {
            isWord = true;
            prefix = pref;
            children = new TrieNode[27]; // initialize the array of children
        }
    }

    /***
     * Starts recursive add function.
     * @param word the word we are adding.
     */
    @Override
    public void add(String word){
        root = add(word,root);
    }

    /***
     * Add word to the Trie. Recursively looks through trie to find where to put a new node.
     * @param s string we want to add
     * @param node current node we are going down from. Starts as root then gets set to children as it recurs.
     */
    private TrieNode add(String s, TrieNode node) {

        TrieNode value;

        if (s.length() == 0){
            value = node;
            //base case when tree is empty
        } else if (node.children[getIndexOfCharacter(s.charAt(0))] == null) {
            //Called when adding "a" as first element
            TrieNode newWord = new TrieNode(node.prefix + s);
            //newWord.prefix = node.prefix + s;
            //newWord.isWord = true;
            node.children[getIndexOfCharacter(s.charAt(0))] = newWord;
            value = node;
        } else {
            // we should be  in the node that has prefix starting from s.charAt(0)
            String temp = node.prefix+s;
            int index = getIndexOfCharacter(s.charAt(0));
            TrieNode baseNode = node.children[index];

            String commonPrefix = findCommonPrefix(baseNode.prefix, node.prefix+s);

            if (commonPrefix.equals(baseNode.prefix)) {
                String suffix = temp.substring(commonPrefix.length());
                value = add(suffix, baseNode);
                node.children[getIndexOfCharacter(s.charAt(0))] = value;
                value = node;
                return value;
            }
            // create new Node called newBaseNode
            TrieNode newBaseNode = new TrieNode();

            String oldSuffix = baseNode.prefix.substring(commonPrefix.length());


            if(oldSuffix.length() > 0) {
                newBaseNode.prefix = commonPrefix;
                newBaseNode.children[getIndexOfCharacter(oldSuffix.charAt(0))] = baseNode;
            } else {
                newBaseNode = baseNode;
            }

            // newSuffix = String s - commonPrefix
            String newSuffix = temp.substring(commonPrefix.length(), temp.length());
            // if newSuffix is "", then set newBaseNode as word
            if (newSuffix.length()== 0){
                newBaseNode.isWord = true;
            }

            //recursively add newSuffix to newBaseNode
            value = add(newSuffix, newBaseNode);

            // make newBaseNode as child of node
            node.children[getIndexOfCharacter(s.charAt(0))] = value;
            value = node;
        }

        return value;
    }

    /***
     * Starts Recursive function checkWord()
     * @param word string - word we want to check
     * @return false if word is not in tree, true if it is.
     */
    @Override
    public boolean checkWord(String word) { return checkWord(root, word, word); }

    /***
     * Recursive function to look through tree and check if word exists in the tree. It will call itself and treat the
     * next node as root and look from there.
     * @param node The current node we are looking at. This will progress down the tree.
     * @param string The string we are cutting up to find it.
     * @param full The original word.
     * @return true if it is found. false if it reaches the end of the tree without finding it.
     */
    private boolean checkWord(TrieNode node, String string, String full){
        boolean result = false;
        TrieNode base;
        // make sure that length of s > 0 and node has child that starts with s.charAt(0)
        if (string.length()>0 && node.children[getIndexOfCharacter(string.charAt(0))]!= null){
            // use node that starts with s.charAt(0), call it base
            base = node.children[getIndexOfCharacter(string.charAt(0))];
            if (full.indexOf(base.prefix) == 0) {
                if (base.prefix.equals(full) && base.isWord) {
                    result = true;
                } else {
                    string = full.substring(base.prefix.length(), full.length());
                    result = checkWord(base, string, full);
                }
            }
        }
        return result;
    }

    /***
     * Suggest function. Takes a word that we know is misspelled and returns a suggested correct spelling of the word.
     *
     * This function takes the input string, iterates over the chars, going to the subsequent value in the child arr
     * until it can go no longer. While it is iterating down the tree, it is keeping track of which nodes are complete,
     * proper words. If it sees a proper spelling on its way to the misspelled word, it will keep track of it and return
     * the closest one to the misspelling.
     *
     * If it cannot find a ancestor node that is a word, it will look at the immediate children of the node it got to.
     * If one of them is proper word, it will return that.
     *
     * If it dosen't find anything after this it will call the recursive modifier which will tweak the string until it
     * finds a proper word.
     *
     * @param word String of the misspelled word.
     * @return String of the suggested correct spelling.
     */
    @Override
    public String suggest(String word){

        // Make a char array so we can step through the characters in the word.
        char[] wordCharArr = word.toCharArray();

        // Make 2 string builders. The first one will only be real, properly spelled words. The second one will be a
        // buffer to add to until we know it is a word. Then we add the temp to suggestion.
        StringBuilder suggestion = new StringBuilder();

        Trie.TrieNode current = root;
        //loop through word, build string while children != null and isEnd == true?

        for (char ch : wordCharArr) {

            if (current.children[getIndexOfCharacter(ch)]!= null){

                current = current.children[getIndexOfCharacter(ch)];

                if (current.isWord){
                    suggestion.setLength(0);
                    suggestion.append(current.prefix);
                }

            } else {
                break;
            }
        }


        if (suggestion.length() != 0){
            //If we found a parent, that is a word, then we can return, else we need to get drastic.
            return suggestion.toString();

        } else {
            //We have already moved a pointer to the farthest node down the tree we can in the last step. That is "current"
            // Go to the farthest node down the tree we can, then look if any of the child nodes are words, if so return it.


            if (current != null && current.children.length > 0) {
                for (Trie.TrieNode child: current.children){
                    if (child != null){
                        if (child.isWord){
                            suggestion.setLength(0);
                            suggestion.append(child.prefix);
                            return suggestion.toString();
                        }
                    }
                }
            }

            return recursiveModCheck(word);
        }
    }

    /***
     * This is a recursive function that is called from suggest when we cannot find a complete word as a parent,
     * ancestor or child of the last node we can get to. If we cannot find one that way, we need to start modifying the
     * string of the word we are looking for to try and counter the spelling mistakes.
     *
     * First we take the string we are looking for and cant find a suggestion for, then we start iterating letters.
     * Making sure we test every possible combination, we go from the end of the word to the front, iterating each
     * letter (as well as the trailing letters) from 'a' to 'z'. For each iteration of a letter, we do every possible
     * combination of the letters that follow it for that word length.
     *
     * tesst -> tesss -> ... -> tessa -> ... -> tessz -> ... -> tesrz -> tesry -> tesra -> ... -> tesaa -> ... -> teszz
     *
     * If we find a word when messing with these letter combinations, it will return the string that made the combo.
     * Theoretically it will return the closest possible correct spelling to the misspelled string that we gave it.
     *
     * @param word String of the word we are looking for. When it calls itself it will call minus the last letter.
     * @return A string of a proper word we found.
     */
    private String recursiveModCheck(String word){

        char [] wordCharArr = word.toCharArray();

        for (int index = wordCharArr.length-1; index >= 0; index--){

            while (wordCharArr[index] > 'a'){
                //decrement up to 'a'
                if (checkWord(charArrToStr(wordCharArr))){
                    return charArrToStr(wordCharArr);
                }
                wordCharArr[index]--;


                //Iterate subsequent chars. Chars following the current one we are looking at.
                for (int i = index+1; i <= wordCharArr.length-1; i++){
                    while (wordCharArr[i] > 'a'){
                        //decrement up to 'a'
                        if (checkWord(charArrToStr(wordCharArr))){
                            return charArrToStr(wordCharArr);
                        }
                        wordCharArr[i]--;
                    }

                    while (wordCharArr[i] < 'z'){
                        //increment down to 'z'
                        if (checkWord(charArrToStr(wordCharArr))){
                            return charArrToStr(wordCharArr);
                        }
                        wordCharArr[i]++;
                    }
                }
            }

            while (wordCharArr[index] < 'z'){
                //increment down to 'z'
                if (checkWord(charArrToStr(wordCharArr))){
                    return charArrToStr(wordCharArr);
                }
                wordCharArr[index]++;

                //Iterate subsequent chars. Chars following the current one we are looking at.
                for (int i = index+1; i <= wordCharArr.length-1; i++){
                    while (wordCharArr[i] > 'a'){
                        //decrement up to 'a'
                        if (checkWord(charArrToStr(wordCharArr))){
                            return charArrToStr(wordCharArr);
                        }
                        wordCharArr[i]--;
                    }

                    while (wordCharArr[i] < 'z'){
                        //increment down to 'z'
                        if (checkWord(charArrToStr(wordCharArr))){
                            return charArrToStr(wordCharArr);
                        }
                        wordCharArr[i]++;
                    }
                }
            }
        }

        // If iterating all possible chars doesn't generate a word, remove the last char so it is shorter and try again.
        return recursiveModCheck(charArrToStr(Arrays.copyOfRange(wordCharArr, 0, wordCharArr.length-1)));
    }

    /***
     * Converts a Char array to a String object.
     * @param arr char array
     * @return String
     */
    private String charArrToStr(char[] arr){
        return new String(arr);
    }

    /***
     * Takes a char and returns an int value based on its ascii value. a == 0, z == 25.
     * ' is hardcoded to 26.
     * @param c char we want to get respective int val of
     * @return integer
     */
    private int getIndexOfCharacter(char c) {
        if (c == '\''){
            return 26;
        }
        return (int)c - (int)'a';
    }

    /***
     * Takes two strings and finds the longest prefix they have in common. For example: s1="test" s2="tell", the result
     * will be "te".
     * @param s1 input string 1
     * @param s2 input string 2
     * @return the common prefix
     */
    private String findCommonPrefix(String s1, String s2) {
        int minLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return s1.substring(0, i);
            }
        }
        return s1.substring(0, minLength);
    }
}
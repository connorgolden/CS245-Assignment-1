/*
 * Single Char Per Node Trie Implementation.
 */

import java.util.Arrays;
import java.util.LinkedList;

public class Tree implements Roots {

    private TreeNode root;

    Tree(){ root = new TreeNode(' '); }

    public static class TreeNode {
        char data;
        boolean isWord;
        int count;
        LinkedList<TreeNode> childList;

        TreeNode(char c) {
            childList = new LinkedList<TreeNode>();
            isWord = false;
            data = c;
            count = 0;
        }

        TreeNode getChild(char c) {
            //Get the child with the value c of a node we are looking at.
            if (childList != null)
                for (TreeNode eachChild : childList)
                    if (eachChild.data == c)
                        return eachChild;
            return null;
        }
    }

    /***
     * Adds a new word to the Tree. Iterates over string checking nodes until it is missing some, then it adds what is
     * needed and marks as a word.
     *
     * Runtime: Worst case O(n) where n is the length of the word. It is really 0(n*27) as each char it has to look
     * through each nodes children to find the right spot to put the new node.
     * @param word word we are adding.
     */
    @Override
    public void add(String word) {
        if (checkWord(word)){
            //If the word is already there...
            return;
        }
        TreeNode current = root;
        for (char ch : word.toCharArray()) {

            TreeNode child = current.getChild(ch);
            if (child != null) {
                current = child;
            } else {
                // If child not present, adding it io the list
                current.childList.add(new TreeNode(ch));
                current = current.getChild(ch);
            }
            current.count++;
        }
        //Mark as word.
        current.isWord = true;
    }

    /***
     * Check word checks if a word is in the tree and marked as a properly spelled word.
     *
     * Runtime: Worst case 0(n) where n is length of word, like add, it has to look through the children at every loop,
     * so O(n*27). Average case will also be O(n).
     * @param word The word we are looking for.
     * @return true if the word exists in tree, false if it dosen't.
     */
    @Override
    public boolean checkWord(String word) {
        TreeNode current = root;
        for (char ch : word.toCharArray()){
            //Iterate over chars of word. Stepping from node to node.
            if (current.getChild(ch) == null) {
                return false;
            } else {
                current = current.getChild(ch);
            }
        }
        //We have gotten as far as we can. Return the last nodes bool if it is a word or not.
        return current.isWord;
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
     * Runtime: This is more complicated. Almost always it will be worst case O(n) or 0(n + 27), where n is length of
     * word but if it gets to the recursive bit because it cannot find what it is looking for, it will be O(n^2).
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
        StringBuilder temp = new StringBuilder();


        //Look if there is a parent, grandparent or great... element that is a word.
        // This is highly efficient way of looking for the last parent to be a full word. Instead of iterating over the
        // characters and calling "checkWord" for each added character which would be O(n^2), it does it in O(n).

        TreeNode current = root;
        //loop through word, build string while children != null and isEnd == true?

        for (char ch : wordCharArr) {
            if (current.childList != null) {
                for (TreeNode eachChild : current.childList) {
                    if (eachChild.data == ch) {
                        temp.append(eachChild.data);
                        if (eachChild.isWord) {
                            suggestion.append(temp.toString());
                            temp.setLength(0);
                        }
                        current = eachChild;
                    }
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
            if (current.childList != null){
                for (TreeNode eachChild : current.childList) {
                    if (eachChild.isWord){
                        temp.append(eachChild.data);
                        suggestion.append(temp);
                        return suggestion.toString();
                    }
                }
            }

            //Now that we have exhausted looking for nearby nodes, we need to start changing letters.
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
     * Runtime: 0(2n^2) so O(n^2)
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
     *
     * Runtime: whatever the runtime of String constructor is. Likely O(n) where n is length of char.
     * @param arr char array
     * @return String
     */
    private String charArrToStr(char[] arr){ return new String(arr); }
}
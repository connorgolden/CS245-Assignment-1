interface Roots {
    /** This is Roots interface, defining the minimum functions for the Tree classes. This also makes it much easier to
     * write the code for Main that is not repetitive. It allows me to pass a "Roots" object as a parameter instead of
     * having to declare multiple functions with the same functionality, the only difference being the parameters.
     **/

    void add(String word);

    boolean checkWord(String word);

    String suggest(String word);
}
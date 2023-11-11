

public class Main {
    public static void main(String[] args) throws Exception {
        Grammar grammar = new Grammar();
        grammar.loadGrammar("code/model/GLC.txt");
        System.out.println(grammar.getKey("T"));
    }
}

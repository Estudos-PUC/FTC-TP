

public class Main {
    public static void main(String[] args) throws Exception {
        ALLGrammars allGrammars = new ALLGrammars();
        allGrammars.loadGrammar("model/GLC.txt");
        allGrammars.grammars.get(0).printGrammar();
        //System.out.println(grammar.getGrammar());
    }
}



public class Main {
    public static void main(String[] args) throws Exception {
        ALLGrammars allGrammars = new ALLGrammars();
        allGrammars.loadGrammar("model/GLC.txt");
        allGrammars.grammars.get(0).printGrammar();
        CNFConverter cnf = new CNFConverter(allGrammars.grammars.get(0));
        System.out.println(cnf.g.productions);
        cnf.start();
        System.out.println("----------------------------------------------------------------");
        System.out.println(cnf.g.productions);
        System.out.println(allGrammars.grammars.get(0).productions);
        //System.out.println(grammar.getGrammar());
    }
}

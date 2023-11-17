import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        ALLGrammars allGrammars = new ALLGrammars();
        allGrammars.loadGrammar("code/model/GLC.txt");
        allGrammars.grammars.get(0).printGrammar();
        CNFConverter cnf = new CNFConverter(allGrammars.grammars.get(0));
        System.out.println(cnf.g.productions);
        cnf.start();
        System.out.println("----------------------------------------------------------------");
        System.out.println(cnf.g.productions);
        System.out.println(allGrammars.grammars.get(0).productions);

        
        List<String> w = Arrays.asList( "(","a", "(");

        CYK cyk = new CYK(cnf.g);
        cyk.cykParse(w);
    }
}

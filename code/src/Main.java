import Gramatica.ALLGrammars;
import CYK_Mod.*;
import CYK_normal.CNFConverter;
import CYK_normal.*;
public class Main {
    public static void main(String[] args) throws Exception {
        boolean flag = false;
        /*
         * Ler gramaticas do arquivo txt e inserir em um Array de gramaticas
         */
        ALLGrammars allGrammars = new ALLGrammars();

        //Alterar o caminho se necessario
        String path = "code/model/GLC.txt";
        allGrammars.loadGrammar(path);

        try {
            allGrammars.loadGrammar(path);
            flag = true;
        } catch (Exception e) {
            flag = false;
            System.out.println("Erro na leitura");
        }

        /*
         * Converter gramatica para a forma CNF
         */
        CNFConverter cnf = new CNFConverter(allGrammars.grammars.get(0).clone());

        /*
         * Chamada do cyk normal
         */
        CYK cyk = new CYK(cnf.g);
        cyk.cykParse("(b0+b)*a0");

        /*
         * Chamada do cyk modificado
         */
        CYKModified cykModified = new CYKModified(allGrammars.grammars.get(0).clone());
        System.out.println(cykModified.runCYKAlgorithm("(b0+b)*a0"));

        // System.out.println(cnf.g.productions);
        // System.out.println("----------------------------------------------------------------");
        // System.out.print(cnf.g.productions);
        // System.out.println(allGrammars.grammars.get(0).productions);
        // System.out.print("----------------------------------------------------------------");
    }
}

import Gramatica.ALLGrammars;
import Gramatica.Grammar;
import CYK_Mod.*;
import CYK_normal.CNFConverter;
import CYK_normal.*;

public class Main {
    public static void main(String[] args) throws Exception {

        /*
         * Ler gramaticas do arquivo txt e inserir em um Array de gramaticas
         */
        ALLGrammars allGrammars = new ALLGrammars();

        // Alterar o caminho se necessario
        String path = "code/model/teste.txt";
        allGrammars.loadGrammar(path);

        try {
            allGrammars.loadGrammar(path);
        } catch (Exception e) {
            System.out.println("Erro na leitura");
        }
        for (Grammar tmp : allGrammars.grammars) {
            	
            System.out.println("Gramatica:");
            System.out.println(tmp.productions);
            System.out.println("-----------------------------");
            /*
             * Converter gramatica para a forma CNF
             */
            CNFConverter cnf = new CNFConverter(tmp.clone());

            /*
             * Chamada do cyk normal e do cyk modificado
             */
            CYK cyk = new CYK(tmp.clone());
            CYKModified cykModified = new CYKModified(tmp.clone());

            /*
             * Testes de gramatica
             */
            for (String tmpWord : tmp.word) {
                System.out.println(tmpWord);
                System.out.print("cyk Normal: ");
                cyk.cykParse(tmpWord + "\n");
                try {
                    System.out.print("cyk Modificado: ");
                    System.out.println(cykModified.runCYKAlgorithm(tmpWord) + "\n");
                } catch (Exception e) {
                    System.out.println(false + "\n");
                }
                System.out.println("------------------------------");
            }

        }

        // System.out.println(cnf.g.productions);
        // System.out.println("----------------------------------------------------------------");
        // System.out.print(cnf.g.productions);
        // System.out.println(allGrammars.grammars.get(0).productions);
        // System.out.print("----------------------------------------------------------------");
    }
}

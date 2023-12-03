import Gramatica.ALLGrammars;
import Gramatica.Grammar;
import CYK_Mod.*;
import CYK_normal.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {

        // Definindo o caminho do arquivo de saída
        String outputPath = "code/model/output.txt";

        System.out.println("Processando...");
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            ALLGrammars allGrammars = new ALLGrammars();
            String path = "code/model/teste2.txt";
            try {
                allGrammars.loadGrammar(path);
            } catch (Exception e) {
                writer.println("Erro na leitura");
            }
            for (Grammar tmp : allGrammars.grammars) {
                writer.println(tmp.toString());
                for (String tmpWord : tmp.word) {
                    try {
                        long startTimeCYK = System.currentTimeMillis();
                        CNFConverter cnf = new CNFConverter(tmp.clone());
                        CYK cyk = new CYK(cnf);
                        writer.println("Word: " + tmpWord + "\n");

                        writer.print("cyk Normal: ");
                        writer.println(cyk.cykParse(tmpWord));
                        long endTimeCYK = System.currentTimeMillis();
                        writer.println("Tempo CYK Normal: " + (endTimeCYK - startTimeCYK) + "ms" + "\n");
                    } catch (Exception e) {
                        writer.println("Erro: existe uma variavel declarada que não tem nenhuma regra associada \n");
                    }

                    long startTimeCYKMod = System.currentTimeMillis();
                    CYKModified cykModified = new CYKModified(tmp.clone());
                    try {
                        writer.print("cyk Modificado: ");
                        writer.println(cykModified.runCYKAlgorithm(tmpWord));
                    } catch (Exception e) {
                        writer.println(false);
                    }
                    long endTimeCYKMod = System.currentTimeMillis();
                    writer.println("Tempo CYK Modificado: " + (endTimeCYKMod - startTimeCYKMod) + "ms");
                    writer.println("------------------------------");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Não foi possível criar o arquivo de saída: " + outputPath);
        }
        System.out.println("Concluido =)");
        System.out.println("Para acessar entre em model/output.txt");
    }
}

package Gramatica;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ALLGrammars {
    public ArrayList<Grammar> grammars = new ArrayList<Grammar>();

    public void loadGrammar(String filePath) {
        try {
            File file = new File(filePath);
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                ArrayList<String> word = new ArrayList<>();
                Map<String, Set<String>> productions = new HashMap<>();
                Set<String> variables = new HashSet<>(
                        Arrays.asList(sc.nextLine().replace("variables: ", "").split(",")));
                Set<String> terminals = new HashSet<>(
                        Arrays.asList(sc.nextLine().replace("terminals: ", "").split(",")));
                String startSymbol = sc.nextLine().replace("start: ", "");
                // Ler producoes
                sc.nextLine();
                String line = sc.nextLine();

                while (!(line.equals("#"))) {
                    String[] parts = line.split(" ");
                    String key = parts[0];

                    productions.put(key, new HashSet<>(Arrays.asList(parts).subList(1, parts.length)));
                    line = sc.nextLine();
                }
                line = sc.nextLine();
                while (!(line.equals("#"))) {
                    word.add(line);
                    line = sc.nextLine();
                }
                Grammar tmp = new Grammar(variables, terminals, productions, startSymbol, word);
                grammars.add(tmp);
            }

            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

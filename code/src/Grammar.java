import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * Classe utilizada para transformar os dados dos txt em uma GLC
 */
public class Grammar {
    private Map<String, List<String>> grammar = new HashMap<>();
    private char Start; 
    public List<String> getKey(String key) {
        return grammar.get(key);
    }

    public Map<String, List<String>> getGrammar() {
        return grammar;
    }

    public void loadGrammar(String filePath){
        try {
            File file = new File(filePath);
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(" ");

                String key = parts[0];
                List<String> value = Arrays.asList(parts).subList(1, parts.length);
                grammar.put(key, value);
            }
            sc.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * 1 Introduzir nova variável de partida
 * 2 Remover λ-produções
 * 3 Remover produções unitárias
 * 4 Converte regras remanescentes
 */
public class Chomsky {
    private Map<String, List<String>> cnf = new HashMap<>();
    
    public Chomsky(Map<String, List<String>> grammar) {
        this.cnf = grammar; 
    }

    public Map<String, List<String>> getCnf() {
        return cnf;
    }
   
    public List<String> getKey(String key) {
        return cnf.get(key);
    }
}

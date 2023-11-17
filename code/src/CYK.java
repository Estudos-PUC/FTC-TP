import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CYK {

    // Non-terminals symbols
    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();
    // Rules of the grammar
    Map<String, List<List<String>>> R = new HashMap<>();

    public CYK(Grammar grammar) {
        this.terminals = grammar.terminals;
        this.non_terminal = grammar.variables;
        this.allSymbols.addAll(terminals);
        this.allSymbols.addAll(non_terminal);
        this.R = splitRules(grammar);
        System.out.println();
    }

    public Map<String, List<List<String>>> splitRules(Grammar grammar) {
        Map<String, List<List<String>>> tmp = new HashMap<>();
        
        for (Map.Entry<String, Set<String>> entry : grammar.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();
            List<List<String>> convertRules = new ArrayList<>();
            List<String> symbols= new ArrayList<>();
            for (String rule : rules) {
                symbols = splitSymbols(allSymbols, rule);
                System.out.println();
                convertRules.add(symbols);
            }
            tmp.put(variable, convertRules);
        }
        return tmp;
    }
    public List<String> splitSymbols(List<String> allSymbols, String input) {
        // Ordenar os símbolos pelo comprimento em ordem decrescente

        allSymbols.sort((a, b) -> b.length() - a.length());

        // Criar uma expressão regular que combina com qualquer um dos símbolos
        StringBuilder regex = new StringBuilder();
        for (String symbol : allSymbols) {
            if (regex.length() > 0) {
                regex.append("|");
            }
            regex.append(Pattern.quote(symbol));
        }

        // Encontrar correspondências na string de entrada
        Matcher matcher = Pattern.compile(regex.toString()).matcher(input);
        List<String> splitSymbols = new ArrayList<>();
        while (matcher.find()) {
            splitSymbols.add(matcher.group());
        }

        return splitSymbols;
    }

    // function to perform the CYK Algorithm
    void cykParse(List<String> w) {
        int n = w.size();

        // Initialize the table
        Map<Integer, Map<Integer, Set<String>>> T = new HashMap<>();

        // Filling in the table
        for (int j = 0; j < n; j++) {

            // Iterate over the rules
            for (Map.Entry<String, List<List<String>>> x : R.entrySet()) {
                String lhs = x.getKey();
                List<List<String>> rule = x.getValue();

                for (List<String> rhs : rule) {

                    // If a terminal is found
                    if (rhs.size() == 1
                            && rhs.get(0).equals(w.get(j))) {
                        if (T.get(j) == null)
                            T.put(j, new HashMap<>());
                        T.get(j)
                                .computeIfAbsent(
                                        j, k -> new HashSet<>())
                                .add(lhs);
                    }
                }
            }
            for (int i = j; i >= 0; i--) {

                // Iterate over the range from i to j
                for (int k = i; k <= j; k++) {

                    // Iterate over the rules
                    for (Map.Entry<String, List<List<String>>> x : R.entrySet()) {
                        String lhs = x.getKey();
                        List<List<String>> rule = x.getValue();

                        for (List<String> rhs : rule) {
                            // If a terminal is found
                            if (rhs.size() == 2
                                    && T.get(i) != null
                                    && T.get(i).get(k) != null
                                    && T.get(i).get(k).contains(
                                            rhs.get(0))
                                    && T.get(k + 1) != null
                                    && T.get(k + 1).get(j) != null
                                    && T.get(k + 1)
                                            .get(j)
                                            .contains(
                                                    rhs.get(1))) {
                                if (T.get(i) == null)
                                    T.put(i,
                                            new HashMap<>());
                                if (T.get(i).get(j) == null)
                                    T.get(i).put(
                                            j, new HashSet<>());
                                T.get(i).get(j).add(lhs);
                            }
                        }
                    }
                }
            }
        }

        // If word can be formed by rules
        // of given grammar
        if (T.get(0) != null && T.get(0).get(n - 1) != null
                && T.get(0).get(n - 1).size() != 0)
            System.out.println("True");
        else
            System.out.println("False");
    }
}
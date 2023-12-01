package CYK_normal;
import java.util.*;

public class CYK {

    // Non-terminals symbols
    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();
    // Rules of the grammar
    Map<String, List<List<String>>> R = new HashMap<>();
    String startSymbol;
    public CYK(CNFConverter grammar) {
        this.terminals = grammar.terminals;
        this.non_terminal = grammar.non_terminal;
        this.allSymbols.addAll(terminals);
        this.allSymbols.addAll(non_terminal);
        this.R = grammar.rules;
        this.startSymbol = grammar.startSymbol;
    }


    // function to perform the CYK Algorithm
    public void cykParse(String word) {
        List<String> w = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            w.add("" + word.charAt(i));
        }
        w.remove(w.size()-1);
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

         // Supondo que CNFConverter tenha esse campo
        if (T.get(0) != null && T.get(0).get(n - 1) != null
                && T.get(0).get(n - 1).contains(startSymbol))
            System.out.println("True");
        else
            System.out.println("False");
    }
}
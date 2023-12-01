package CYK_normal;
import java.util.*;

public class CYK {


    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();

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


    public void cykParse(String word) {
        List<String> w = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            w.add("" + word.charAt(i));
        }
        w.remove(w.size()-1);
        int n = w.size();

        // Inicializar a tabela
        Map<Integer, Map<Integer, Set<String>>> T = new HashMap<>();

        // Preencher a tabela
        for (int j = 0; j < n; j++) {

            // Iterar sobre as regras
            for (Map.Entry<String, List<List<String>>> x : R.entrySet()) {
                String lhs = x.getKey();
                List<List<String>> rule = x.getValue();

                for (List<String> rhs : rule) {

                    // Se encontrar um terminal
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

                // Iterar de i a j
                for (int k = i; k <= j; k++) {

                    // Iterar sobre as regras
                    for (Map.Entry<String, List<List<String>>> x : R.entrySet()) {
                        String lhs = x.getKey();
                        List<List<String>> rule = x.getValue();

                        for (List<String> rhs : rule) {
                            // Se encontrar um terminal
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

        if (T.get(0) != null && T.get(0).get(n - 1) != null
                && T.get(0).get(n - 1).contains(startSymbol))
            System.out.println("true");
        else
            System.out.println("false");
    }
}
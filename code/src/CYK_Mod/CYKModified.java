package CYK_Mod;

import java.util.*;

import Gramatica.Grammar;

public class CYKModified {

    BINConverter binGrammar;
    String startSymbol;
    public CYKModified(Grammar grammar) {
        this.binGrammar = new BINConverter(grammar);
        this.startSymbol = grammar.startSymbol;
        
    }

    public boolean runCYKAlgorithm(String w) {
        int n = w.length();
        // Usamos uma lista de listas de conjuntos para representar a tabela CYK
        List<List<Set<String>>> T = new ArrayList<>();

        // Inicializa a tabela CYK
        for (int i = 0; i <= n; i++) {
            T.add(new ArrayList<Set<String>>(Collections.nCopies(n + 1, null)));
            for (int j = 0; j <= n; j++) {
                T.get(i).set(j, new HashSet<>());
            }
        }

        // Preencha a diagonal principal da tabela CYK com fechamentos transitivos dos
        // caracteres
        for (int i = 1; i <= n; i++) {
            T.get(i).set(i, binGrammar.transitiveClosure.get(String.valueOf(w.charAt(i - 1))));
        }

        // Construa o restante da tabela CYK
        for (int length = 2; length <= n; length++) {
            for (int i = 1; i <= n - length + 1; i++) {
                int j = i + length - 1;
                for (int k = i; k < j; k++) {
                    for (String A : binGrammar.non_terminal) {
                        for (List<String> rule : binGrammar.R.getOrDefault(A, new ArrayList<>())) {
                            if (rule.size() == 2) {
                                String B = rule.get(0);
                                String C = rule.get(1);
                                if (T.get(i).get(k).contains(B) && T.get(k + 1).get(j).contains(C)) {
                                    T.get(i).get(j).add(A);
                                }
                            }
                        }
                    }
                    // Após verificar todas as produções, feche T[i][j] sob o fechamento transitivo
                    Set<String> closure = new HashSet<>();
                    for (String symbol : T.get(i).get(j)) {
                        closure.addAll(binGrammar.transitiveClosure.getOrDefault(symbol, new HashSet<>()));
                    }
                    T.get(i).set(j, closure);
                }
            }
        }

        // Se o símbolo de início da gramática está no conjunto que deriva a palavra
        // inteira, aceite a palavra
        return T.get(1).get(n).contains(startSymbol);

    }
}

import java.util.*;

public class ChainedVariables {

    // Classe para representar uma gramática
    static class Grammar {
        Set<String> variables;
        Set<String> terminals;
        Map<String, Set<String>> productions;

        public Grammar(Set<String> variables, Set<String> terminals, Map<String, Set<String>> productions) {
            this.variables = variables;
            this.terminals = terminals;
            this.productions = productions;
        }
    }

    public static void main(String[] args) {
        // Exemplo de inicialização da gramática
        Set<String> variables = new HashSet<>(Arrays.asList("E", "T", "F"));
        Set<String> terminals = new HashSet<>(Arrays.asList("(", ")", "*", "t"));
        Map<String, Set<String>> productions = new HashMap<>();
        productions.put("E", new HashSet<>(Arrays.asList("E+T", "T")));
        productions.put("T", new HashSet<>(Arrays.asList("T*F", "F")));
        productions.put("F", new HashSet<>(Arrays.asList("(E)", "t")));

        Grammar g = new Grammar(variables, terminals, productions);

        // Encontrar variáveis encadeadas para 'S'
        Set<String> chainedVariables = findChainedVariables(g, "E");
        System.out.println("Variáveis encadeadas com E: " + chainedVariables);
    }

    // Método para encontrar variáveis encadeadas
    public static Set<String> findChainedVariables(Grammar g, String startVariable) {
        Set<String> U = new HashSet<>(); // Conjunto de variáveis encadeadas
        Set<String> N = new HashSet<>(Collections.singletonList(startVariable)); // Variáveis a serem processadas

        // Repetir até que não haja mais variáveis novas
        while (!N.isEmpty()) {
            // Adicionar todas as novas variáveis ao conjunto de encadeadas
            U.addAll(N);

            // Encontrar variáveis que são acessíveis a partir das variáveis em N
            Set<String> newN = new HashSet<>();
            for (String Z : N) {
                for (String Y : g.variables) {
                    if (!U.contains(Y)) {
                        // Verificar se existe uma produção Z -> Y
                        if (g.productions.containsKey(Z) && g.productions.get(Z).contains(Y)) {
                            newN.add(Y);
                        }
                    }
                }
            }

            // Atualizar N para o próximo conjunto de variáveis a serem processadas
            N = newN;
        }

        // Retornar o conjunto de variáveis encadeadas
        return U;
    }
}

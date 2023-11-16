import java.util.*;

public class UnitaryRulesRemover {

    // Classe para representar uma gramática
    static class Grammar {
        Set<String> variables;
        Set<String> terminals;
        Map<String, Set<String>> productions;
        String startSymbol;

        public Grammar(Set<String> variables, Set<String> terminals, Map<String, Set<String>> productions,
                String startSymbol) {
            this.variables = variables;
            this.terminals = terminals;
            this.productions = productions;
            this.startSymbol = startSymbol;
        }
    }

    public static void main(String[] args) {
        // Set<String> variables = new HashSet<>(Arrays.asList("E", "T", "F"));
        // Set<String> terminals = new HashSet<>(Arrays.asList("(", ")", "*", "t"));
        // Map<String, Set<String>> productions = new HashMap<>();
        // productions.put("E", new HashSet<>(Arrays.asList("E+T", "T")));
        // productions.put("T", new HashSet<>(Arrays.asList("T*F", "F")));
        // productions.put("F", new HashSet<>(Arrays.asList("(E)", "t")));

        // Grammar g = new Grammar(variables, terminals, productions, "E");

        Set<String> variables = new HashSet<>(Arrays.asList("L", "S", "E"));
        Set<String> terminals = new HashSet<>(Arrays.asList("(", ")", "a"));
        Map<String, Set<String>> productions = new HashMap<>();
        productions.put("L", new HashSet<>(Arrays.asList("(S)", "()")));
        productions.put("S", new HashSet<>(Arrays.asList("SE", "E")));
        productions.put("E", new HashSet<>(Arrays.asList("a", "L")));

        Grammar g = new Grammar(variables, terminals, productions, "L");

        // Remover regras unitárias
        Grammar gPrime = removeUnitaryRules(g);
        System.out.println("Grammar without unitary rules:");
        gPrime.productions.forEach((key, value) -> System.out.println(key + " -> " + value));
    }

    // Método para eliminar regras unitárias
    public static Grammar removeUnitaryRules(Grammar g) {
        // Novo conjunto de produções sem regras unitárias
        Map<String, Set<String>> newProductions = new HashMap<>();

        // Para cada variável X em V, faça
        for (String X : g.variables) {
            // Inicializa um conjunto temporário para armazenar as novas produções de X
            Set<String> newRulesForX = new HashSet<>();

            // Encontra todas as variáveis encadeadas a X
            Set<String> chainedVariables = findChainedVariables(g, X);

            // Para cada variável Y em enc(X), verifique as produções Y -> w em R
            for (String Y : chainedVariables) {
                // Assegura que Y tem produções associadas a ela antes de prosseguir
                if (g.productions.containsKey(Y)) {
                    for (String w : g.productions.get(Y)) {
                        // Se w não é uma variável, ou seja, é um terminal ou uma cadeia de terminais e
                        // variáveis
                        if (!g.variables.contains(w)) {
                            // Adiciona a produção X -> w em R'
                            newRulesForX.add(w);
                        }
                    }
                }
            }

            // Atualiza o conjunto de produções R' para a variável X
            newProductions.put(X, newRulesForX);
        }

        // Retorna a nova gramática com o conjunto de produções atualizado
        return new Grammar(g.variables, g.terminals, newProductions, g.startSymbol);
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

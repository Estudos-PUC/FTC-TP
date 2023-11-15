import java.util.*;

public class LambdaRulesRemover {

    // Classe para representar uma gramática
    static class Grammar {
        Set<String> variables;
        Set<String> terminals;
        Map<String, Set<String>> productions;
        String startSymbol;

        public Grammar(Set<String> variables, Set<String> terminals, Map<String, Set<String>> productions, String startSymbol) {
            this.variables = variables;
            this.terminals = terminals;
            this.productions = productions;
            this.startSymbol = startSymbol;
        }
    }

    public static void main(String[] args) {
        // Exemplo de inicialização da gramática
        Set<String> variables = new HashSet<>(Arrays.asList("S","S0", "A", "B"));
        Set<String> terminals = new HashSet<>(Arrays.asList("a", "b"));
        Map<String, Set<String>> productions = new HashMap<>();
        productions.put("S0", new HashSet<>(Arrays.asList("S")));
        productions.put("S", new HashSet<>(Arrays.asList("ASA", "aB", "a")));
        productions.put("A", new HashSet<>(Arrays.asList("B", "S", ""))); // "" representa lambda
        productions.put("B", new HashSet<>(Arrays.asList("b")));

        Grammar g = new Grammar(variables, terminals, productions, "S");

        Grammar gPrime = removeLambdaRules(g);
        
        // Imprimindo a nova gramática
        System.out.println("Nova gramática sem regras lambda:");
        gPrime.productions.forEach((key, value) -> System.out.println(key + " -> " + value));
    }

    // Método para remover regras lambda
    public static Grammar removeLambdaRules(Grammar g) {
        // Encontrar variáveis anuláveis
        Set<String> nullableVariables = findNullableVariables(g);

        // Inicializar o novo conjunto de produções sem regras lambda
        Map<String, Set<String>> newProductions = new HashMap<>();

        // Processar cada produção para remover regras lambda
        for (String variable : g.variables) {
            Set<String> newRules = new HashSet<>();
            if (g.productions.containsKey(variable)) {
                for (String production : g.productions.get(variable)) {
                    if (!production.isEmpty()) { // Ignorar regras lambda
                        // Adicionar todas as combinações possíveis sem as variáveis anuláveis
                        Set<String> combinations = getCombinations(production, nullableVariables);
                        newRules.addAll(combinations);
                    }
                }
            }
            if (!variable.equals(g.startSymbol) || nullableVariables.contains(g.startSymbol)) {
                newRules.remove(""); // Remover regras lambda, exceto para o símbolo de início se necessário
            }
            newProductions.put(variable, newRules);
        }

        return new Grammar(g.variables, g.terminals, newProductions, g.startSymbol);
    }

    // Método para encontrar variáveis anuláveis
    public static Set<String> findNullableVariables(Grammar g) {
        Set<String> nullable = new HashSet<>();
        boolean changes;
        do {
            changes = false;
            for (String variable : g.variables) {
                if (!nullable.contains(variable)) {
                    Set<String> rules = g.productions.get(variable);
                    if (rules != null && rules.contains("")) {
                        nullable.add(variable);
                        changes = true;
                    } else {
                        for (String production : rules) {
                            if (production.chars().allMatch(c -> nullable.contains(String.valueOf((char) c)))) {
                                nullable.add(variable);
                                changes = true;
                                break;
                            }
                        }
                    }
                }
            }
        } while (changes);

        return nullable;
    }

// Método para gerar todas as combinações de produção sem variáveis anuláveis
public static Set<String> getCombinations(String production, Set<String> nullableVariables) {
    Set<String> combinations = new HashSet<>();
    combinations.add(production); // Adicionar a produção original

    // Criar combinações removendo as variáveis anuláveis uma por vez
    for (int i = 0; i < production.length(); i++) {
        // Se o caractere atual é uma variável anulável, criamos novas combinações
        if (nullableVariables.contains(String.valueOf(production.charAt(i)))) {
            List<String> newCombinations = new ArrayList<>();
            for (String existingCombination : combinations) {
                // Se o caractere anulável não estiver no final da string
                if (i < existingCombination.length()) {
                    // Criamos uma nova combinação sem o caractere anulável
                    newCombinations.add(existingCombination.substring(0, i) + existingCombination.substring(i + 1));
                }
            }
            combinations.addAll(newCombinations);
        }
    }
    // Remover a produção vazia se ela não for válida para o símbolo de início
    combinations.remove("");

    return combinations;
}
}

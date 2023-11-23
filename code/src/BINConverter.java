import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.regex.Pattern;

public class BINConverter {
    int variableIndex;
    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();
    Map<String, List<List<String>>> R = new HashMap<>();
    Set<String> nullableSymbols;

    Map<String, Set<String>> inverseUnitGraph = new HashMap<>();
    Map<String, Set<String>> transitiveClosure = new  HashMap<>();

    public BINConverter(Grammar grammar) {
        grammar = breakDownProductions(grammar);
        this.terminals = grammar.terminals;
        this.non_terminal = grammar.variables;
        this.allSymbols.addAll(terminals);
        this.allSymbols.addAll(non_terminal);
        this.R = splitRules(grammar);
        variableIndex = grammar.variableIndex;
        this.nullableSymbols = Nullable();

        buildInverseUnitGraph();
        buildTransitiveClosure();
        System.out.println("--------------");
        System.out.println(inverseUnitGraph);
        System.out.println(transitiveClosure);
        System.out.println();
    }

     // Método para construir o grafo unitário inverso
     private void buildInverseUnitGraph() {
        // Adicionar todos os símbolos como vértices do grafo
        for (String symbol : allSymbols) {
            inverseUnitGraph.put(symbol, new HashSet<>());
        }

        // Para cada regra, adicionar as arestas correspondentes ao grafo
        for (Map.Entry<String, List<List<String>>> entry : R.entrySet()) {
            String lhs = entry.getKey(); // Não-terminal da esquerda
            for (List<String> rule : entry.getValue()) {
                if (rule.size() == 1) {
                    // Para regras A → y
                    inverseUnitGraph.get(rule.get(0)).add(lhs);
                } else if (rule.size() == 2) {
                    // Para regras A → By e A → yB onde B é anulável
                    if (nullableSymbols.contains(rule.get(0))) {
                        inverseUnitGraph.get(rule.get(1)).add(lhs);
                    }
                    if (nullableSymbols.contains(rule.get(1))) {
                        inverseUnitGraph.get(rule.get(0)).add(lhs);
                    }
                }
            }
        }
    }

    // Método para calcular o fechamento transitivo usando o grafo unitário inverso
    private void buildTransitiveClosure() {
        for (String symbol : allSymbols) {
            transitiveClosure.put(symbol, new HashSet<>());
        }

        // Para cada símbolo, executar uma busca em profundidade para encontrar o fechamento
        for (String symbol : allSymbols) {
            Set<String> visited = new HashSet<>();
            dfs(symbol, visited);
            transitiveClosure.get(symbol).addAll(visited);
        }
    }

    // Método auxiliar de busca em profundidade para calcular o fechamento transitivo
    private void dfs(String symbol, Set<String> visited) {
        if (!visited.contains(symbol)) {
            visited.add(symbol);
            for (String adjacentSymbol : inverseUnitGraph.get(symbol)) {
                dfs(adjacentSymbol, visited);
            }
        }
    }


    public void printGrammar() {

        System.out.println("Regras");

        for (Map.Entry<String, List<List<String>>> entry : R.entrySet()) {
            String variable = entry.getKey();
            List<List<String>> rules = entry.getValue();

            System.out.print(variable + " -> ");

            for (int i = 0; i < rules.size(); i++) {
                List<String> rule = rules.get(i);
                for (int j = 0; j < rule.size(); j++) {
                    System.out.print(rule.get(j));
                    if (j < rule.size() - 1) {
                        System.out.print(" ");
                    }
                }
                if (i < rules.size() - 1) {
                    System.out.print(" | ");
                }
            }
            System.out.println();
        }
    }

    public Map<String, List<List<String>>> splitRules(Grammar grammar) {
        Map<String, List<List<String>>> tmp = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : grammar.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();
            List<List<String>> convertRules = new ArrayList<>();
            List<String> symbols = new ArrayList<>();
            for (String rule : rules) {
                symbols = splitSymbols(allSymbols, rule);
                convertRules.add(symbols);
            }
            tmp.put(variable, convertRules);
        }
        return tmp;
    }

    private Grammar breakDownProductions(Grammar grammar) {
        Map<String, String> symbolToVariableMap = new HashMap<>();
        Map<String, Set<String>> newProductions = new HashMap<>();
        List<String> allSymbols = new ArrayList<>();
        allSymbols.addAll(grammar.terminals);
        allSymbols.addAll(grammar.variables);
        for (Map.Entry<String, Set<String>> entry : grammar.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();

            for (String rule : rules) {
                List<String> symbols = splitSymbols(allSymbols, rule);

                if (symbols.size() > 2) {
                    String currentVariable = variable;
                    for (int i = 0; i < symbols.size() - 2; i++) {
                        String nextSymbol = symbols.get(i + 1);
                        String newVariable = symbolToVariableMap.getOrDefault(nextSymbol,
                                grammar.getNextVariableName());
                        symbolToVariableMap.putIfAbsent(nextSymbol, newVariable);

                        Set<String> currentRules = newProductions.computeIfAbsent(currentVariable,
                                k -> new HashSet<>());
                        currentRules.add(symbols.get(i) + newVariable);

                        currentVariable = newVariable;
                    }
                    // Add the final rule which will have exactly two symbols
                    newProductions.computeIfAbsent(currentVariable, k -> new HashSet<>())
                            .add(symbols.get(symbols.size() - 2) + symbols.get(symbols.size() - 1));
                } else {
                    // Rule already has two or fewer symbols, so we simply add it to the new
                    // productions
                    newProductions.computeIfAbsent(variable, k -> new HashSet<>()).add(rule);
                }
            }
        }

        // Update the grammar's productions with the new productions, eliminating any
        // duplicates
        for (Map.Entry<String, Set<String>> entry : newProductions.entrySet()) {
            String variable = entry.getKey();
            Set<String> newRules = entry.getValue();
            grammar.productions.put(variable, newRules);
        }

        // Update the grammar's variables with the new variables
        grammar.variables.addAll(symbolToVariableMap.values());
        return grammar;
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

    // NULLABLE -------------------------------------------------------
    public Set<String> Nullable() {
        Set<String> nullable = new HashSet<>();
        Set<String> to_do = new HashSet<>();
        Map<String, Set<SimpleEntry<String, String>>> occurs = new HashMap<>();

        // Inicializa os conjuntos nullable e to_do com valores vazios
        // Inicializa o mapa occurs com conjunto vazio para cada não-terminal
        for (String A : non_terminal) {
            occurs.put(A, new HashSet<>());
        }

        // Percorre as produções da gramática para encontrar produções unitárias A -> B
        for (String A : non_terminal) {
            for (List<String> production : R.getOrDefault(A, new ArrayList<>())) {
                if (production.size() == 1 && non_terminal.contains(production.get(0))) {
                    String B = production.get(0);
                    occurs.get(B).add(new SimpleEntry<>(A, null));
                }
            }
        }

        // Percorre as produções da gramática para encontrar produções A → BC ou A → CB
        for (String A : non_terminal) {
            for (List<String> production : R.getOrDefault(A, new ArrayList<>())) {
                if (production.size() == 2 && non_terminal.contains(production.get(0))
                        && non_terminal.contains(production.get(1))) {
                    String B = production.get(0);
                    String C = production.get(1);

                    occurs.get(B).add(new SimpleEntry<>(A, C));
                    occurs.get(C).add(new SimpleEntry<>(A, B));
                }
            }
        }

        // Identificar símbolos anuláveis iniciais
        for (String A : non_terminal) {
            for (List<String> production : R.getOrDefault(A, new ArrayList<>())) {
                if (production.isEmpty() || (production.size() == 1 && production.get(0).equals(""))) {
                    nullable.add(A);
                    to_do.add(A);
                }
            }
        }

        while (!to_do.isEmpty()) {
            Iterator<String> iterator = to_do.iterator();
            String B = iterator.next();
            iterator.remove(); // Remove B de to_do

            // Verifica todas as entradas em occurs(B)
            for (SimpleEntry<String, String> entry : occurs.getOrDefault(B, new HashSet<>())) {
                String A = entry.getKey();
                String C = entry.getValue();

                // Se C é nulo, significa que é uma produção do tipo A -> B, então A é anulável
                // Se C não é nulo e é anulável, então A também é anulável
                if (C == null || nullable.contains(C)) {
                    if (!nullable.contains(A)) {
                        nullable.add(A);
                        to_do.add(A); // Adiciona A a to_do para processamento posterior
                    }
                }
            }
        }

        return nullable;
    }

    public void printNullable() {
        System.out.println("Símbolos Anuláveis:");
        for (String symbol : nullableSymbols) {
            System.out.println(symbol);
        }
    }
}

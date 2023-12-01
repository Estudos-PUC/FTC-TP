package CYK_normal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import Gramatica.Grammar;

public class CNFConverter {
    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();
    Map<String, List<List<String>>> rules = new HashMap<>();
    String startSymbol;
    public int variableIndex;

    public CNFConverter(Grammar grammar) {
        this.terminals = grammar.terminals;
        this.non_terminal = grammar.variables;
        this.allSymbols.addAll(terminals);
        this.allSymbols.addAll(non_terminal);
        this.rules = splitRules(grammar);
        this.startSymbol = grammar.startSymbol;
        this.variableIndex = 0;

        createNewStartSymbol();
        System.out.println("Nullable: " + findNullableVariables());
        System.out.println(findChainedVariables());
    }

    public String getNextVariableName() {
        if (variableIndex >= 26 * 26) { // Checa se excedeu o limite de ZZ
            throw new IllegalStateException("Excedido o número máximo de variáveis.");
        }
        int quotient = variableIndex / 26; // Calcula a primeira letra (0 para A, 1 para B, etc.)
        int remainder = variableIndex % 26; // Calcula a segunda letra (0 para A, 1 para B, etc.)
        char firstChar = (char) ('A' + quotient);
        char secondChar = (char) ('A' + remainder);
        String nextVariable = "" + firstChar + secondChar;
        variableIndex++; // Incrementa o índice para a próxima variável
        return nextVariable;
    }

    public void printFormattedGrammar() {
        // Imprime o conjunto de variáveis
        System.out.println("Variáveis: " + non_terminal);

        // Imprime o conjunto de símbolos terminais
        System.out.println("Terminais: " + terminals);

        // Imprime as regras de produção
        System.out.println("Regras:");
        for (String variable : non_terminal) {
            // Obtém todas as produções para a variável atual
            List<List<String>> productions = rules.get(variable);

            if (productions != null && !productions.isEmpty()) {
                // Converte as produções em strings, unindo os símbolos de cada produção
                String productionString = productions.stream()
                        .map(production -> production.stream().collect(Collectors.joining(" ")))
                        .collect(Collectors.joining(" | ")); // Separa múltiplas produções com "|"

                // Imprime a variável seguida por "->" e a string formatada das produções
                System.out.println(variable + " -> " + productionString);
            }
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

    // 1. CRIAR NOVO SIMBOLO INICIAL ------------------------------
    private void createNewStartSymbol() {
        // Checar se o símbolo inicial aparece no lado direito de alguma regra
        boolean isStartSymbolOnRightSide = rules.values().stream()
                .flatMap(List::stream)
                .anyMatch(production -> production.contains(startSymbol));

        if (isStartSymbolOnRightSide) {
            String newStartSymbol = getNextVariableName();

            // Adiciona o novo símbolo inicial ao conjunto de não terminais
            non_terminal.add(newStartSymbol);
            allSymbols.add(newStartSymbol);

            // Adiciona a nova regra de produção para o novo símbolo inicial
            List<String> startProduction = new ArrayList<>();
            startProduction.add(startSymbol);
            List<List<String>> newStartProductions = new ArrayList<>();
            newStartProductions.add(startProduction);
            rules.put(newStartSymbol, newStartProductions);

            // Atualiza o símbolo inicial
            startSymbol = newStartSymbol;
        }
        // Se o símbolo inicial não está do lado direito de nenhuma regra, nada precisa
        // ser feito
    }

    // --------------------------------------------------------------------

    // 2. ELIMINAR REGRAS LAMBDA ------------------------------
    // ... restante da sua classe ...

    private Set<String> findNullableVariables() {
        Set<String> nullableVariables = new HashSet<>(); // VAg
        boolean changed;

        do {
            changed = false;
            Set<String> newNullableVariables = new HashSet<>(nullableVariables); // N

            for (String variable : non_terminal) {
                // Se a variável já foi identificada como anulável, pule para a próxima
                if (nullableVariables.contains(variable)) {
                    continue;
                }

                // Verifica todas as regras da variável atual
                for (List<String> production : rules.get(variable)) {
                    // Se a produção é vazia (representando lambda) ou todas as variáveis na
                    // produção são anuláveis
                    if (production.isEmpty() || production.stream().allMatch(nullableVariables::contains)) {
                        newNullableVariables.add(variable);
                        changed = true;
                        break; // Não precisa verificar mais produções se uma já é anulável
                    }
                }
            }

            // Atualiza o conjunto de variáveis anuláveis após verificar todas as variáveis
            nullableVariables = newNullableVariables;
        } while (changed); // Continue até que não haja mais mudanças no conjunto de variáveis anuláveis

        return nullableVariables;
    }

    // --------------------------------------------------------------------

    // 3. ELIMINAR REGRAS UNITARIAS
    private Map<String, Set<String>> findChainedVariables() {
        Map<String, Set<String>> chainedVariables = new HashMap<>();

        // Inicialize o mapa com todos os não terminais apontando para um conjunto vazio
        for (String nonTerminal : non_terminal) {
            chainedVariables.put(nonTerminal, new HashSet<>());
        }

        for (String nonTerminal : non_terminal) {
            Set<String> chained = new HashSet<>(); // U no pseudocódigo
            Set<String> current = new HashSet<>(); // N no pseudocódigo
            current.add(nonTerminal); // Começa com o próprio não terminal

            while (!current.isEmpty()) {
                Set<String> next = new HashSet<>();
                for (String symbol : current) {
                    // Adiciona o símbolo atual ao conjunto de encadeados se ainda não estiver lá
                    if (chained.add(symbol)) {
                        // Para cada produção que contém o símbolo atual no lado direito...
                        for (List<String> production : rules.get(symbol)) {
                            // Verifica se é uma produção unitária
                            if (production.size() == 1 && non_terminal.containsAll(production)) {
                                next.addAll(production); // Adicione ao próximo conjunto a ser processado
                            }
                        }
                    }
                }
                current = next; // Atualiza o conjunto N para a próxima iteração
            }

            // Depois de processar todas as produções encadeadas, atribua o resultado ao não
            // terminal
            chainedVariables.put(nonTerminal, chained);
        }

        return chainedVariables;
    }
    // --------------------------------------------------------------------

    // 3. GERAR NOVAS REGRAS E APLICAR SUBSTITUICOES PARA REGRAS DE TAMANHO MAIOR
    // QUE 2
    // --------------------------------------------------------------------

    // 4. QUEBRA DE REGRAS ------------------------------

    /*
     * Alterar a estrutura do breakDownProductions lembrando que agora R representa
     * g.productions e que tem essa estrutura:
     * (Novo) Map<String, List<List<String>>> que é =! de Map<String, Set<String>>
     * (antigo)
     * 
     * lembrando de tirar o Grammar e o return porque agora voce tem variaveis
     * criadas no construtor
     */
    private Grammar /* remover return */ breakDownProductions(Grammar grammar) /* remover parametro */ {
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
                                k -> new HashSet<>());/* alterar */
                        currentRules.add(symbols.get(i) + newVariable);

                        currentVariable = newVariable;
                    }
                    // Add the final rule which will have exactly two symbols
                    newProductions.computeIfAbsent(currentVariable, k -> new HashSet<>())
                            .add(symbols.get(symbols.size() - 2) + symbols.get(symbols.size() - 1));/* alterar */
                } else {
                    // Rule already has two or fewer symbols, so we simply add it to the new
                    // productions
                    newProductions.computeIfAbsent(variable, k -> new HashSet<>()).add(rule);/* alterar */
                }
            }
        }

        // Update the grammar's productions with the new productions, eliminating any
        // duplicates
        for (Map.Entry<String, Set<String>> entry : newProductions.entrySet()) /* alterar */ {
            String variable = entry.getKey();
            Set<String> newRules = entry.getValue();
            grammar.productions.put(variable, newRules);/* alterar */
        }

        // Update the grammar's variables with the new variables
        grammar.variables.addAll(symbolToVariableMap.values());
        return grammar; /* remover return */
    }

}

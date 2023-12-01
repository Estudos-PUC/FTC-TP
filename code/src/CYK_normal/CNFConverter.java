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
        System.out.println("Encadeadas: " + findChainedVariables());
        removeLambdaRules();
        printFormattedGrammar();
        breakDownProductions();
        printFormattedGrammar();
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

    private void removeLambdaRules() {
        Set<String> nullableVariables = findNullableVariables(); // Usa o método já implementado para encontrar variáveis anuláveis
        Map<String, List<List<String>>> newRules = new HashMap<>(); // R'
    
        // Para cada regra existente, gerar novas regras omitindo as variáveis anuláveis
        for (Map.Entry<String, List<List<String>>> entry : rules.entrySet()) {
            String variable = entry.getKey();
            List<List<String>> productions = entry.getValue();
    
            // Adicionar todas as regras existentes que não são lambda
            List<List<String>> newProductions = new ArrayList<>();
            for (List<String> production : productions) {
                if (!production.isEmpty()) {
                    newProductions.add(new ArrayList<>(production));
                }
            }
    
            // Adicionar novas regras para cada combinação de variáveis anuláveis
            for (List<String> production : productions) {
                // Criar todas as combinações possíveis onde as variáveis anuláveis são omitidas
                List<String> combination = new ArrayList<>(production);
                combination.removeIf(nullableVariables::contains); // Remove variáveis anuláveis
    
                if (!combination.isEmpty() && !newProductions.contains(combination)) {
                    newProductions.add(combination);
                }
            }
    
            newRules.put(variable, newProductions);
        }
    
        // Para lidar com o caso especial do símbolo inicial
        if (nullableVariables.contains(startSymbol)) {
            List<String> lambdaProduction = new ArrayList<>();
            newRules.get(startSymbol).add(lambdaProduction);
        }
    
        // Atualiza o conjunto de regras de produção da classe
        rules = newRules;
    }
    // 4. QUEBRA DE REGRAS ------------------------------
    private void breakDownProductions() {
        Map<String, String> symbolToVariableMap = new HashMap<>();
    
        // Atualiza a estrutura de rules para lidar com a nova estrutura
        Map<String, List<List<String>>> newProductions = new HashMap<>();
    
        for (Map.Entry<String, List<List<String>>> entry : rules.entrySet()) {
            String variable = entry.getKey();
            List<List<String>> ruleList = entry.getValue();
    
            for (List<String> rule : ruleList) {
                if (rule.size() > 2) {
                    String currentVariable = variable;
                    for (int i = 0; i < rule.size() - 2; i++) {
                        String nextSymbol = rule.get(i + 1);
                        String newVariable = symbolToVariableMap.getOrDefault(nextSymbol, getNextVariableName());
                        symbolToVariableMap.putIfAbsent(nextSymbol, newVariable);
    
                        List<String> currentRule = new ArrayList<>();
                        currentRule.add(rule.get(i));
                        currentRule.add(newVariable);
                        newProductions.computeIfAbsent(currentVariable, k -> new ArrayList<>()).add(currentRule);
    
                        currentVariable = newVariable;
                    }
                    // Adiciona a regra final que terá exatamente dois símbolos
                    List<String> finalRule = new ArrayList<>();
                    finalRule.add(rule.get(rule.size() - 2));
                    finalRule.add(rule.get(rule.size() - 1));
                    newProductions.computeIfAbsent(currentVariable, k -> new ArrayList<>()).add(finalRule);
                } else {
                    // A regra já tem dois ou menos símbolos, então simplesmente a adicionamos às novas produções
                    newProductions.computeIfAbsent(variable, k -> new ArrayList<>()).add(new ArrayList<>(rule));
                }
            }
        }
    
        // Atualiza as produções de rules com as novas produções
        rules = newProductions;
    
        // Atualiza os não terminais com as novas variáveis
        non_terminal.addAll(symbolToVariableMap.values());
    }
    

}

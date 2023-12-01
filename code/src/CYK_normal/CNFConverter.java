package CYK_normal;

import java.util.ArrayList;
import java.util.Collections;
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
        removeLambdaRules();
        System.out.println("Encadeadas: " + findChainedVariables());
        //printFormattedGrammar();
        removeUnitaryRules();
        System.out.println("\n\nELIMINACAO DE REGRAS UNITARIAS: ");
        printFormattedGrammar();
        
        System.out.println("\n\nELIMINACAO DE TERMINAIS: ");
        eliminateTerminalsFromProductions();
        printFormattedGrammar();
        
        
        System.out.println("\n\nQUEBRA DE PRODUCOES: ");
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
        System.out.println("Variavel de Partida: " + startSymbol);
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

    private void removeLambdaRules() {
        // Etapa 1: Encontrar variáveis anuláveis
        Set<String> nullableVariables = findNullableVariables();
    
        // Etapa 2: Inicializar um novo conjunto de regras sem regras lambda
        Map<String, List<List<String>>> newRules = new HashMap<>();
    
        // Etapa 3: Iterar sobre as regras existentes e criar novas regras sem as variáveis anuláveis
        for (Map.Entry<String, List<List<String>>> entry : rules.entrySet()) {
            String variable = entry.getKey();
            List<List<String>> productions = entry.getValue();
            List<List<String>> newProductions = new ArrayList<>();
    
            for (List<String> production : productions) {
                // Ignore regras lambda, exceto para o símbolo de partida
                if (!(production.isEmpty() && !variable.equals(startSymbol))) {
                    // Adicione a produção original, pois ela será usada para gerar combinações
                    newProductions.add(new ArrayList<>(production));
    
                    // Gere e adicione todas as combinações possíveis que não incluem as variáveis anuláveis
                    List<List<String>> combinations = generateCombinations(production, nullableVariables);
                    newProductions.addAll(combinations);
                }
            }
    
            // Se a variável for o símbolo inicial e estiver entre as anuláveis, adicione a regra lambda
            if (variable.equals(startSymbol) && nullableVariables.contains(startSymbol)) {
                newProductions.add(Collections.emptyList());
            }
    
            newRules.put(variable, newProductions);
        }
    
        // Etapa 4: Atualizar as regras da gramática para remover as regras lambda
        rules = newRules;
        removeDuplicateRules();
        removeSelfProducingStartRule();
    
        // Etapa 5: Imprimir a gramática formatada sem regras lambda (para verificação)
        printFormattedGrammar();
    }
    
    // Função que gera todas as combinações possíveis de uma produção sem as variáveis anuláveis.
    private List<List<String>> generateCombinations(List<String> production, Set<String> nullableVariables) {
        // Lista para armazenar todas as combinações
        List<List<String>> combinations = new ArrayList<>();
    
        // Adiciona a produção original, a menos que seja vazia, o que indica uma regra lambda
        if (!production.isEmpty()) {
            combinations.add(new ArrayList<>(production));
        }
    
        // Gera todas as combinações removendo variáveis anuláveis
        generateCombinationsRecursive(combinations, new ArrayList<>(), production, 0, nullableVariables);
    
        return combinations;
    }
    
    
    private void generateCombinationsRecursive(List<List<String>> combinations, List<String> currentCombination,
                                               List<String> remainingProduction, int index, Set<String> nullableVariables) {
        if (index == remainingProduction.size()) {
            // Se a combinação atual não é vazia e não é uma duplicata, adiciona à lista de combinações
            if (!currentCombination.isEmpty() && !combinations.contains(currentCombination)) {
                combinations.add(new ArrayList<>(currentCombination));
            }
            return;
        }
    
        String symbol = remainingProduction.get(index);
    
        // Se o símbolo não é anulável ou é um terminal, mantém o símbolo e continua
        if (!nullableVariables.contains(symbol)) {
            currentCombination.add(symbol);
            generateCombinationsRecursive(combinations, currentCombination, remainingProduction, index + 1, nullableVariables);
            currentCombination.remove(currentCombination.size() - 1);
        } else {
            // Se o símbolo é anulável, gera combinações com e sem o símbolo
    
            // Combinação sem o símbolo anulável
            generateCombinationsRecursive(combinations, currentCombination, remainingProduction, index + 1, nullableVariables);
    
            // Combinação com o símbolo anulável
            currentCombination.add(symbol);
            generateCombinationsRecursive(combinations, currentCombination, remainingProduction, index + 1, nullableVariables);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }
    
    private void removeDuplicateRules() {
        // Novo mapa para armazenar as regras sem duplicatas
        Map<String, List<List<String>>> newRules = new HashMap<>();
    
        // Iterar sobre cada entrada no mapa de regras
        for (Map.Entry<String, List<List<String>>> entry : rules.entrySet()) {
            String variable = entry.getKey();
            List<List<String>> productions = entry.getValue();
    
            // Utilizar um conjunto para identificar produções únicas
            Set<List<String>> uniqueProductions = new HashSet<>(productions);
    
            // Converter o conjunto de volta para uma lista e armazenar no novo mapa
            newRules.put(variable, new ArrayList<>(uniqueProductions));
        }
    
        // Atualizar as regras da classe para ser o novo mapa sem duplicatas
        rules = newRules;
    }

    private void removeSelfProducingStartRule() {
        // Lista para armazenar as novas produções que não incluem a produção unitária de partida
        List<List<String>> newProductionsForStartSymbol = new ArrayList<>();
    
        // Obter as produções para a variável de partida
        List<List<String>> startProductions = rules.get(startSymbol);
    
        // Iterar sobre cada produção da variável de partida
        for (List<String> production : startProductions) {
            // Se a produção não é uma produção unitária que referencia a própria variável de partida, adicioná-la à nova lista
            if (!(production.size() == 1 && production.get(0).equals(startSymbol))) {
                newProductionsForStartSymbol.add(production);
            }
        }
    
        // Atualizar as regras para a variável de partida, removendo a produção unitária que referencia a própria variável
        rules.put(startSymbol, newProductionsForStartSymbol);
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

    public void removeUnitaryRules() {
        // Encontra todas as variáveis encadeadas
        Map<String, Set<String>> chainedVariables = findChainedVariables();
    
        // Novo conjunto de regras sem regras unitárias
        Map<String, List<List<String>>> newRules = new HashMap<>();
    
        // Iterar sobre todas as variáveis
        for (String variable : non_terminal) {
            // Inicializa a lista de novas produções para a variável atual
            List<List<String>> newProductions = new ArrayList<>();
    
            // Obter o conjunto de variáveis encadeadas para a variável atual
            Set<String> closure = chainedVariables.getOrDefault(variable, Collections.emptySet());
    
            // Para cada variável na clausura, adicione suas produções não-unitárias
            for (String chainedVar : closure) {
                // Iterar sobre as produções da variável encadeada
                for (List<String> production : rules.get(chainedVar)) {
                    // Verificar se a produção é unitária e se é diferente da variável atual
                    if (production.size() == 1 && non_terminal.containsAll(production) && !production.contains(variable)) {
                        continue; // Ignora regras unitárias
                    }
                    // Adiciona a produção à lista de novas produções se não for unitária
                    newProductions.add(production);
                }
            }
    
            // Adiciona as novas produções ao conjunto de regras se não estiverem vazias
            if (!newProductions.isEmpty()) {
                newRules.put(variable, newProductions);
            }
        }
    
        // Atualiza as regras da gramática com o novo conjunto sem regras unitárias
        rules = newRules;
    
        // Opcional: remover possíveis duplicatas após a remoção das regras unitárias
        removeDuplicateRules();
    }
    // --------------------------------------------------------------------

    // 3. GERAR NOVAS REGRAS E APLICAR SUBSTITUICOES PARA REGRAS DE TAMANHO MAIOR
    // QUE 2
    private void eliminateTerminalsFromProductions() {
        // Inicializa um mapa temporário para armazenar as novas regras
        Map<String, List<List<String>>> newRules = new HashMap<>();
        
        // Inicializa um mapa para controlar as variáveis que foram criadas para substituir terminais
        Map<String, String> terminalToVariable = new HashMap<>();
        
        // Inicializa um conjunto para armazenar novas variáveis não-terminais
        Set<String> newNonTerminals = new HashSet<>();
    
        for (String variable : new ArrayList<>(non_terminal)) { // Use uma cópia do conjunto para iteração
            List<List<String>> updatedProductions = new ArrayList<>();
    
            for (List<String> production : rules.get(variable)) {
                if (production.size() > 1) {
                    List<String> newProduction = new ArrayList<>();
                    for (String symbol : production) {
                        if (terminals.contains(symbol)) {
                            if (!terminalToVariable.containsKey(symbol)) {
                                String newVariable = getNextVariableName();
                                terminalToVariable.put(symbol, newVariable);
                                newNonTerminals.add(newVariable);
                                newRules.put(newVariable, Collections.singletonList(Collections.singletonList(symbol)));
                            }
                            newProduction.add(terminalToVariable.get(symbol));
                        } else {
                            newProduction.add(symbol);
                        }
                    }
                    updatedProductions.add(newProduction);
                } else {
                    updatedProductions.add(production);
                }
            }
    
            newRules.put(variable, updatedProductions);
        }
    
        // Agora atualize o conjunto non_terminal com as novas variáveis não-terminais
        non_terminal.addAll(newNonTerminals);
        
        // Atualiza o mapa de regras da classe com as novas regras
        rules.putAll(newRules);
    }
    
    
    

    // --------------------------------------------------------------------

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
                    // A regra já tem dois ou menos símbolos, então simplesmente a adicionamos às
                    // novas produções
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

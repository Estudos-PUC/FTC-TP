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

import Gramatica.Grammar;

public class CNFConverter {
    public Grammar g;

    public CNFConverter(Grammar grammar){
        System.out.println("GRAMATICA INICIAL: ");
        grammar.printGrammar();

        createNewStartSymbol(grammar);
        System.out.println("GRAMATICA APOS A CRIACAO DE NOVO SIMBOLO DE PARTIDA: ");
        grammar.printGrammar();

        grammar = removeLambdaRules(grammar);
        System.out.println("GRAMATICA APOS A REMOCAO DE LAMBDA: ");
        grammar.printGrammar();
        
        grammar = removeUnitaryRules(grammar);
        System.out.println("GRAMATICA APOS A REMOCAO DE REGRAS UNITARIAS: ");
        grammar.printGrammar();
        
        productionsWithTwoOrMoreSymbols(grammar);
        System.out.println("GRAMATICA APOS A MOD: ");
        grammar.printGrammar();

        breakDownProductions(grammar);
        this.g = grammar ;
        System.out.println("GRAMATICA NA CNF: ");
        grammar.printGrammar();
    }
    private static void addProductions(Map<String, Set<String>> productions, String variable, Set<String> newRules) {
        productions.put(variable, newRules);
    }

    public static void createNewStartSymbol(Grammar g) {
        // Verifique se o símbolo de início ocorre no lado direito de alguma regra
        boolean isStartSymbolOnRightSide = g.productions.values().stream()
                .anyMatch(rules -> rules.stream().anyMatch(rule -> rule.contains(g.startSymbol)));
    
        if (isStartSymbolOnRightSide) {
            // Crie um novo símbolo de início que não está em V
            String newStartSymbol = g.getNextVariableName();
    
            // Adicione o novo símbolo de início a V'
            g.variables.add(newStartSymbol);
    
            // Adicione a regra P' → P em R'
            Set<String> newStartRules = new HashSet<>();
            newStartRules.add(g.startSymbol);
            g.productions.put(newStartSymbol, newStartRules);
    
            // Atualize o símbolo de início da gramática para o novo símbolo
            g.startSymbol = newStartSymbol;
        }
        // Se o símbolo de início não ocorrer do lado direito, nenhuma ação é necessária
    }
    
    

    // ----------------------------- REMOCAO DE REGRAS LAMBDA --------------------------=========
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
                newRules.remove("/"); // Remover regras lambda, exceto para o símbolo de início se necessário
            }
            addProductions(newProductions, variable, newRules);
        }

        return g;
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
                    if (rules != null && rules.contains("/")) {
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
        // Usar um único conjunto para armazenar todas as combinações
        Set<String> combinations = new HashSet<>();
        combinations.add(production); // Adicionar a produção original
    
        // Iterar sobre cada caractere da produção
        for (int i = 0; i < production.length(); i++) {
            // Verificar se o caractere atual é uma variável anulável
            if (nullableVariables.contains(String.valueOf(production.charAt(i)))) {
                // Uma nova lista temporária não é mais necessária.
                // Em vez disso, um novo conjunto para armazenar combinações atualizadas é usado.
                Set<String> updatedCombinations = new HashSet<>();
    
                // Para cada combinação existente, criar uma nova sem o caractere anulável
                for (String existingCombination : combinations) {
                    // Se o caractere anulável não estiver no final da string
                    if (i < existingCombination.length()) {
                        // Adicionando diretamente ao conjunto atualizado
                        updatedCombinations.add(existingCombination.substring(0, i) + existingCombination.substring(i + 1));
                    }
                }
    
                // Adicionar todas as novas combinações ao conjunto principal
                combinations.addAll(updatedCombinations);
            }
        }
    
        // Remover a produção vazia se ela não for válida para o símbolo de início
        combinations.remove("");
    
        return combinations;
    }
    // ------------------------ FIM REMOCAO DE REGRAS LAMBDA----------------------------------- //

    // ------------------------ ELIMINACAO DE REGRAS UNITARIAS -------------------------------- //
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
            addProductions(newProductions, X, newRulesForX);
        }

        // Retorna a nova gramática com o conjunto de produções atualizado
        return g;
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

    // ------------------------ FIM ELIMINACAO DE REGRAS UNITARIAS------------------------------ //

    // ------------------------ TRATAR TERMINAIS ----------------------------------------------------
    // abc

    // substituir variaveis terminais que não são producoes unitarias e criar novas regras  
    public  void productionsWithTwoOrMoreSymbols(Grammar g) {
        Map<String, Set<String>> productionsWithTwoOrMoreSymbols = new HashMap<>();
        Map<String, String> newVariablesForTerminals = new HashMap<>(); // Guarda as novas variáveis para terminais
        Map<String, String> substitutions = new HashMap<>(); // Armazena as substituições a serem realizadas

        // Primeira iteração para identificar as produções e decidir substituições
        for (Map.Entry<String, Set<String>> entry : g.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();
            for (String production : rules) {
                String productionWithoutSpaces = production.replaceAll("\\s+", "");
                if (productionWithoutSpaces.length() >= 2) {
                    productionsWithTwoOrMoreSymbols.computeIfAbsent(variable, k -> new HashSet<>()).add(production);

                    for (int i = 0; i < productionWithoutSpaces.length(); i++) {
                        String symbolStr = String.valueOf(productionWithoutSpaces.charAt(i));
                        if (g.terminals.contains(symbolStr)) {
                            boolean isUniqueProduction = false;
                            String uniqueProductionVariable = null;
                            for (Map.Entry<String, Set<String>> prodEntry : g.productions.entrySet()) {
                                String prodVariable = prodEntry.getKey();
                                Set<String> prodRules = prodEntry.getValue();
                                if (prodRules.contains(symbolStr) && prodRules.size() == 1) {
                                    isUniqueProduction = true;
                                    uniqueProductionVariable = prodVariable;
                                    break;
                                }
                            }
                            if (isUniqueProduction) {
                                substitutions.put(symbolStr, uniqueProductionVariable);
                            } else if (!newVariablesForTerminals.containsKey(symbolStr)) {
                                String newVariable = g.getNextVariableName();
                                newVariablesForTerminals.put(symbolStr, newVariable);
                                substitutions.put(symbolStr, newVariable);
                            }
                        }
                    }
                }
            }
        }

        // Substituições e adição de novas variáveis
        performSubstitutionsAndAddNewVariables(g, newVariablesForTerminals, substitutions);
        System.out.println();
    }

    private  void performSubstitutionsAndAddNewVariables(Grammar g, Map<String, String> newVariables, Map<String, String> substitutions) {
        // Aplica substituições
        for (Map.Entry<String, Set<String>> entry : g.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> newRules = new HashSet<>();
            for (String rule : entry.getValue()) {
                if (rule.length() == 1 && g.terminals.contains(rule)) {
                    // Não substituir em regras unitárias
                    newRules.add(rule);
                } else {
                    String newRule = rule;
                    for (Map.Entry<String, String> sub : substitutions.entrySet()) {
                        newRule = newRule.replace(sub.getKey(), sub.getValue());
                    }
                    newRules.add(newRule);
                }
            }
            addProductions(g.productions, variable, newRules);
            System.out.println(g.productions);
            System.out.println();
        }

        // Adiciona novas variáveis e regras à gramática
        for (Map.Entry<String, String> newVarEntry : newVariables.entrySet()) {
            String terminal = newVarEntry.getKey();
            String newVariable = newVarEntry.getValue();
            Set<String> newRule = new HashSet<>(Collections.singletonList(terminal));
            addProductions(g.productions, newVariable, newRule);
            g.variables.add(newVariable);
            //System.out.println("Criada nova variável " + newVariable + " com a regra " + newVariable + " -> " + terminal);
        }
    }

    // ------------------------ FIM  TRATAR TERMINAIS ------------------------------ //

    private static void breakDownProductions(Grammar grammar) {
        Map<String, String> symbolToVariableMap = new HashMap<>();
        Map<String, Set<String>> newProductions = new HashMap<>();
        List<String> allSymbols = new ArrayList<>();
        allSymbols.addAll(grammar.terminals);
        allSymbols.addAll(grammar.variables);
        for (Map.Entry<String, Set<String>> entry : grammar.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();

            for (String rule : rules) {
                List<String> symbols = splitSymbols(allSymbols,rule);

                if (symbols.size() > 2) {
                    String currentVariable = variable;
                    for (int i = 0; i < symbols.size() - 2; i++) {
                        String nextSymbol = symbols.get(i + 1);
                        String newVariable = grammar.getNextVariableName();
                        symbolToVariableMap.putIfAbsent(nextSymbol, newVariable);

                        Set<String> currentRules = newProductions.computeIfAbsent(currentVariable, k -> new HashSet<>());
                        currentRules.add(symbols.get(i) + newVariable);

                        currentVariable = newVariable;
                    }
                    // Add the final rule which will have exactly two symbols
                    newProductions.computeIfAbsent(currentVariable, k -> new HashSet<>())
                                  .add(symbols.get(symbols.size() - 2) + symbols.get(symbols.size() - 1));
                } else {
                    // Rule already has two or fewer symbols, so we simply add it to the new productions
                    newProductions.computeIfAbsent(variable, k -> new HashSet<>()).add(rule);
                }
            }
        }

        // Update the grammar's productions with the new productions, eliminating any duplicates
        for (Map.Entry<String, Set<String>> entry : newProductions.entrySet()) {
            String variable = entry.getKey();
            Set<String> newRules = entry.getValue();
            grammar.productions.put(variable, newRules);
        }

        // Update the grammar's variables with the new variables
        grammar.variables.addAll(symbolToVariableMap.values());
    }


    public static List<String> splitSymbols(List<String> allSymbols, String input) {
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
}


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CNFConverter {
    // Classe para representar uma gramática
    static class Grammar {
        Set<String> variables;
        Set<String> terminals;
        Map<String, Set<String>> productions;
        String startSymbol;
        int variableIndex;

        public Grammar(Set<String> variables, Set<String> terminals, Map<String, Set<String>> productions,
                String startSymbol) {
            this.variables = variables;
            this.terminals = terminals;
            this.productions = productions;
            this.startSymbol = startSymbol;
            this.variableIndex = 0; 
        }

        public String getNextVariableName() {
            if (variableIndex >= 26 * 101) { // Checa se excedeu o limite de Z100
                throw new IllegalStateException("Excedido o número máximo de variáveis.");
            }
            int quotient = variableIndex / 101; // Calcula qual letra usar (0 para A, 1 para B, etc.)
            int remainder = variableIndex % 101; // Calcula o número (0 a 100)
            char nextChar = (char) ('A' + quotient);
            String nextVariable = nextChar + String.format("%02d", remainder);
            variableIndex++; // Incrementa o índice para a próxima variável
            return nextVariable;
        }

        public void printGrammar() {
            System.out.println("Variáveis: " + this.variables);
            System.out.println("Terminais: " + this.terminals);
            System.out.println("Símbolo inicial: " + this.startSymbol);
            System.out.println("Produções:");
            for (Map.Entry<String, Set<String>> entry : this.productions.entrySet()) {
                String variable = entry.getKey();
                Set<String> rules = entry.getValue();
                System.out.println("  " + variable + " -> " + rules);
            }
        }
    }

    private static void addProductions(Map<String, Set<String>> productions, String variable, Set<String> newRules) {
        productions.put(variable, newRules);
    }

    public static void main(String[] args) {
        Set<String> variables = new HashSet<>(Arrays.asList("E", "T", "F"));
        Set<String> terminals = new HashSet<>(Arrays.asList("(", ")", "*", "t"));
        Map<String, Set<String>> productions = new HashMap<>();
        productions.put("E", new HashSet<>(Arrays.asList("E+T", "T")));
        productions.put("T", new HashSet<>(Arrays.asList("T*F", "F")));
        productions.put("F", new HashSet<>(Arrays.asList("(E)", "t")));

        Grammar g = new Grammar(variables, terminals, productions, "E");
        Grammar noLambda = removeLambdaRules(g);

        // Imprimindo a nova gramatica
        System.out.println("Nova gramatica sem regras lambda:");
        noLambda.printGrammar();

        Grammar noUnitary = removeUnitaryRules(noLambda);
        System.out.println("Nova gramatica sem regras unitarias:");
        noUnitary.printGrammar();

        // System.out.println("FIM");
        System.out.println("Nova gramatica quase final:");
        productionsWithTwoOrMoreSymbols(noUnitary);
        noUnitary.printGrammar();
        breakDownProductions(noUnitary);
        noUnitary.printGrammar();
        
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
                newRules.remove(""); // Remover regras lambda, exceto para o símbolo de início se necessário
            }
            addProductions(newProductions, variable, newRules);
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

    // ------------------------ FIM ELIMINACAO DE REGRAS UNITARIAS------------------------------ //

    // ------------------------ PENULTIMA PARTE ----------------------------------------------------

    // 1. encontrar regras com lado direito maior que 2
    public static void productionsWithTwoOrMoreSymbols(Grammar g) {
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
    }

    private static void performSubstitutionsAndAddNewVariables(Grammar g, Map<String, String> newVariables, Map<String, String> substitutions) {
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
        }

        // Adiciona novas variáveis e regras à gramática
        for (Map.Entry<String, String> newVarEntry : newVariables.entrySet()) {
            String terminal = newVarEntry.getKey();
            String newVariable = newVarEntry.getValue();
            Set<String> newRule = new HashSet<>(Collections.singletonList(terminal));
            addProductions(g.productions, newVariable, newRule);
            g.variables.add(newVariable);
            System.out.println("Criada nova variável " + newVariable + " com a regra " + newVariable + " -> " + terminal);
        }
    }

    // --------------------------------------------------------------------------------------------

    private static void breakDownProductions(Grammar grammar) {
        Map<String, String> symbolToVariableMap = new HashMap<>();
        Map<String, Set<String>> newProductions = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : grammar.productions.entrySet()) {
            String variable = entry.getKey();
            Set<String> rules = entry.getValue();

            for (String rule : rules) {
                List<String> symbols = splitSymbols(rule);

                if (symbols.size() > 2) {
                    String currentVariable = variable;
                    for (int i = 0; i < symbols.size() - 2; i++) {
                        String nextSymbol = symbols.get(i + 1);
                        String newVariable = symbolToVariableMap.getOrDefault(nextSymbol, grammar.getNextVariableName());
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


    private static List<String> splitSymbols(String rule) {
        // Expressão regular para combinar nomes de variáveis como A, A00, A001, B00, etc.
        Matcher matcher = Pattern.compile("[A-Z][0-9]*").matcher(rule);
        List<String> symbols = new ArrayList<>();

        while (matcher.find()) {
            symbols.add(matcher.group());
        }

        return symbols;
    }
}


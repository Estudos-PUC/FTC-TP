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

    public CNFConverter(Grammar grammar) {
        System.out.println("GRAMATICA INICIAL: ");
        grammar.printGrammar();

        createNewStartSymbol(grammar);
        // System.out.println("GRAMATICA APOS A CRIACAO DE NOVO SIMBOLO DE PARTIDA: ");
        // grammar.printGrammar();
    }

    // 1. CRIAR NOVO SIMBOLO INICIAL ------------------------------
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

    // --------------------------------------------------------------------




    // 2. ELIMINAR REGRAS LAMBDA ------------------------------
    // --------------------------------------------------------------------



    
    // 3. ELIMINAR REGRAS UNITARIAS 
    // --------------------------------------------------------------------



    // 3. GERAR NOVAS REGRAS E APLICAR SUBSTITUICOES PARA REGRAS DE TAMANHO MAIOR QUE 2
    // --------------------------------------------------------------------



    // 4. QUEBRA DE REGRAS  ------------------------------

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

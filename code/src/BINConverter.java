import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BINConverter {
    int variableIndex;
    Set<String> terminals;
    Set<String> non_terminal;
    List<String> allSymbols = new ArrayList<>();
    Map<String, List<List<String>>> R = new HashMap<>();

    public BINConverter(Grammar grammar) {
        grammar = breakDownProductions(grammar); 
        this.terminals = grammar.terminals;
        this.non_terminal = grammar.variables;
        this.allSymbols.addAll(terminals);
        this.allSymbols.addAll(non_terminal);
        this.R = splitRules(grammar);
        variableIndex = grammar.variableIndex;
        System.out.println();
    }

    public void printGrammar() {
        System.out.println("----------------------");
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
                System.out.println();
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
                List<String> symbols = splitSymbols(allSymbols,rule);

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
        return grammar;
    }


    public  List<String> splitSymbols(List<String> allSymbols, String input) {
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

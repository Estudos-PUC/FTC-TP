import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Classe utilizada para transformar os dados dos txt em uma GLC
 */
public class Grammar implements Cloneable {
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
    @Override
    public Grammar clone() {
        try {
            Grammar cloned = (Grammar) super.clone();
            cloned.variables = new HashSet<>(this.variables);
            cloned.terminals = new HashSet<>(this.terminals);
            cloned.productions = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : this.productions.entrySet()) {
                cloned.productions.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            // Não é necessário clonar startSymbol e variableIndex, pois são imutáveis ou primitivos
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Não deve acontecer, pois estamos implementando Cloneable
        }
    }
}
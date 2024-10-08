package Gramatica;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Classe utilizada para transformar os dados dos txt em uma GLC
 */
public class Grammar implements Cloneable {
    public Set<String> variables;
    public Set<String> terminals;
    public Map<String, Set<String>> productions;
    public String startSymbol;
    public int variableIndex;
    public ArrayList<String> word;

    public Grammar(Set<String> variables, Set<String> terminals, Map<String, Set<String>> productions,
            String startSymbol, ArrayList<String> word) {
        this.variables = variables;
        this.terminals = terminals;
        this.productions = productions;
        this.startSymbol = startSymbol;
        this.variableIndex = 0;
        this.word = word;
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
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Variables: ").append(variables.toString()).append("\n");
        sb.append("Terminals: ").append(terminals.toString()).append("\n");
        sb.append("Productions:\n");
        for (Map.Entry<String, Set<String>> entry : productions.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue().toString()).append("\n");
        }
        sb.append("Start Symbol: ").append(startSymbol).append("\n");
        return sb.toString();
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

}
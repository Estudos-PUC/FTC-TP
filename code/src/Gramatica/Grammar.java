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
}
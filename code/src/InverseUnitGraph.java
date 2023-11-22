import java.util.*;

public class InverseUnitGraph {
    private Map<String, Set<String>> graph;

    public InverseUnitGraph() {
        this.graph = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public Set<String> getReachableNodes(String start) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        reachable.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> neighbours = graph.getOrDefault(current, Collections.emptySet());
            for (String neighbour : neighbours) {
                if (!reachable.contains(neighbour)) {
                    reachable.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return reachable;
    }
}
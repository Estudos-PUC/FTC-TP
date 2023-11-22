import java.util.*;

public class InverseUnitGraph {
    private Map<String, Set<String>> graph = new HashMap<>();

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
            for (String neighbor : graph.getOrDefault(current, Collections.emptySet())) {
                if (reachable.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return reachable;
    }
}

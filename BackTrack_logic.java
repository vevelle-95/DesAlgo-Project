import java.io.*;
import java.util.*;
import java.util.concurrent.*;



graph class
public class Graph {
    private Map<String, Map<String, Integer>> adjList = new HashMap<>();
    private SimulationEvent lastEvent;

    public void addEdge(String from, String to, int distance) {
        adjList.computeIfAbsent(from, k -> new HashMap<>()).put(to, distance);
        adjList.computeIfAbsent(to, k -> new HashMap<>()).put(from, distance);
    }

    public Map<String, Map<String, Integer>> getAdjList() {
        return adjList;
    }

    public SimulationEvent getLastEvent() {
        return lastEvent;
    }

    public void simulateRandomEvent() {
        List<String> nodes = new ArrayList<>(adjList.keySet());
        if (nodes.size() < 2) return;

        Random rand = new Random();
        String from = nodes.get(rand.nextInt(nodes.size()));
        List<String> neighbors = new ArrayList<>(adjList.get(from).keySet());
        if (neighbors.isEmpty()) return;
        String to = neighbors.get(rand.nextInt(neighbors.size()));

        int eventType = rand.nextInt(5); // 0 to 4
        switch (eventType) {
            case 0: blockEdge(from, to, "Flooding reported"); 
            break;
            case 1: blockEdge(from, to, "Road closed due to construction"); 
            break;
            case 2: increaseEdgeWeight(from, to, "Accident causing delay in traffic"); 
            break;
            case 3: increaseEdgeWeight(from, to, "Sudden increase in traffic congestion"); 
            break;
            case 4: blockEdge(from, to, "Route closed due to protest"); break;
        }
    }

    private void blockEdge(String from, String to, String reason) {
        if (adjList.get(from).containsKey(to)) {
            adjList.get(from).remove(to);
            adjList.get(to).remove(from);
            lastEvent = new SimulationEvent("block", from, to, reason + " on " + from + " ↔ " + to);
        }
    }

    private void increaseEdgeWeight(String from, String to, String reason) {
        int oldWeight = adjList.get(from).get(to);
        int added = new Random().nextInt(10) + 5;
        int newWeight = oldWeight + added;
        adjList.get(from).put(to, newWeight);
        adjList.get(to).put(from, newWeight);
        lastEvent = new SimulationEvent("traffic", from, to,
            reason + ": " + from + " ↔ " + to + " now " + newWeight + " units");
    }
}


class BackTrackAlgo {
    private Graph graph;
    private int bestDistance;
    private List<String> bestPath;

    public BackTrackAlgo(Graph graph) {
        this.graph = graph;
    }

    public void findShortestPath(String startNode) {
        Set<String> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        currentPath.add(startNode);
        visited.add(startNode);
        bestDistance = Integer.MAX_VALUE;
        bestPath = new ArrayList<>();

    }

    private void backtrack(List<String> path, Set<String> visited, int currentDistance) {
        // Complete solution: visited all nodes
        if (visited.size() == graph.getAdjList().size()) {
            // Optionally return to start to make a cycle
            String last = path.get(path.size() - 1);
            String start = path.get(0);
            if (graph.getAdjList().get(last).containsKey(start)) {
                int finalDistance = currentDistance + graph.getAdjList().get(last).get(start);
                if (finalDistance < bestDistance) {
                    bestDistance = finalDistance;
                    bestPath = new ArrayList<>(path);
                    bestPath.add(start); // to complete the cycle
                }
            }
            return;
        }

        String current = path.get(path.size() - 1);
        for (String neighbor : graph.getAdjList().get(current).keySet()) {
            if (!visited.contains(neighbor)) {
                int edgeWeight = graph.getAdjList().get(current).get(neighbor);
                visited.add(neighbor);
                path.add(neighbor);

                backtrack(path, visited, currentDistance + edgeWeight);

                // Undo / Backtrack
                visited.remove(neighbor);
                path.remove(path.size() - 1);
            }
        }
    }
    public List<String> getBestPath() {
        return bestPath;
    }

    public int getBestDistance() {
        return bestDistance;
    }
}
public class SimulationEvent {
    private String type;    // "flooding", "accident", etc.
    private String from;
    private String to;
    private String message;

    public SimulationEvent(String type, String from, String to, String message) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getType() { return type; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getMessage() { return message; }
}


//RANDOM EVENT FOR SIMULATING IF NEED/ NOT REAL TIME IS USED
class RandomEventSimulator {
    public static void start(Runnable eventCallback) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(eventCallback, 10, 10, TimeUnit.SECONDS); 
    }
}

//maan calls for  start up

public class NavigationSimulation {
    public static void main(String[] args) {
        Graph graph = new Graph("sample_destinations.csv"); // change later to your actual file
     BackTrackAlgo solver = new BackTrackAlgo(graph);

        String startNode = "A";
        solver.findShortestPath(startNode);

        RandomEventSimulator.start(() -> {
            graph.simulateRandomEvent();
            solver.findShortestPath(startNode);
        });
    }
}
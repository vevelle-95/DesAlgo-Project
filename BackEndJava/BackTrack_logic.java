import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// create edge weight for each node
class EdgeWeight {
    private int walk;
    private int car;
    private Integer jeepney; // nullable

    public EdgeWeight(int walk, int car, Integer jeepney) {
        this.walk = walk;
        this.car = car;
        this.jeepney = jeepney;
    }

    public int getWalk() { return walk; }
    public int getCar() { return car; }
    public Integer getJeepney() { return jeepney; }

    public int getMode(String mode) {
        return switch (mode) {
            case "walk" -> walk;
            case "car" -> car;
            case "jeep" -> (jeepney != null ? jeepney : Integer.MAX_VALUE);
            default -> throw new IllegalArgumentException("Invalid mode: " + mode);
        };
    }
}

//create simulation 
public class SimulationEvent {
    private String type;
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

//create a class function for creating/simulate a graph
class Graph {
    private Map<String, Map<String, EdgeWeight>> adjList = new HashMap<>();
    private SimulationEvent lastEvent;

    public Graph(List<String> filenames) {
        for (String filename : filenames) {
            loadFromCSV(filename);
        }
    }

    public void addEdge(String from, String to, EdgeWeight weight) {
        adjList.computeIfAbsent(from, k -> new HashMap<>()).put(to, weight);
        adjList.computeIfAbsent(to, k -> new HashMap<>()).put(from, weight);
    }

    public Map<String, Map<String, EdgeWeight>> getAdjList() {
        return adjList;
    }

    public SimulationEvent getLastEvent() {
        return lastEvent;
    }

    public void simulateRandomEvent(String mode) {
        List<String> nodes = new ArrayList<>(adjList.keySet());
        if (nodes.size() < 2) return;

        Random rand = new Random();
        String from = nodes.get(rand.nextInt(nodes.size()));
        List<String> neighbors = new ArrayList<>(adjList.get(from).keySet());
        if (neighbors.isEmpty()) return;
        String to = neighbors.get(rand.nextInt(neighbors.size()));

        int eventType = rand.nextInt(5);
        switch (eventType) {
            case 0 -> blockEdge(from, to, "Flooding reported");
            case 1 -> blockEdge(from, to, "Road closed due to construction");
            case 2 -> increaseEdgeWeight(from, to, "Accident causing delay", mode);
            case 3 -> increaseEdgeWeight(from, to, "Sudden traffic congestion", mode);
            case 4 -> blockEdge(from, to, "Route closed due to protest");
        }
    }

    private void blockEdge(String from, String to, String reason) {
        if (adjList.get(from).containsKey(to)) {
            adjList.get(from).remove(to);
            adjList.get(to).remove(from);
            lastEvent = new SimulationEvent("block", from, to, reason + " on " + from + " ↔ " + to);
        }
    }

    private void increaseEdgeWeight(String from, String to, String reason, String mode) {
        EdgeWeight ew = adjList.get(from).get(to);
        int oldWeight = ew.getMode(mode);
        int added = new Random().nextInt(10) + 5;
        int newWeight = oldWeight + added;

        int walk = ew.getWalk();
        int car = ew.getCar();
        Integer jeep = ew.getJeepney();

        switch (mode) {
            case "walk" -> walk = newWeight;
            case "car" -> car = newWeight;
            case "jeep" -> jeep = newWeight;
        }

        EdgeWeight updated = new EdgeWeight(walk, car, jeep);
        adjList.get(from).put(to, updated);
        adjList.get(to).put(from, updated);

        lastEvent = new SimulationEvent("traffic", from, to, reason + ": " + from + " ↔ " + to + " now " + newWeight + " units");
    }

    private void loadFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String from = parts[0].trim();
                    String to = parts[1].trim();
                    int walk = Integer.parseInt(parts[2].trim());
                    int car = Integer.parseInt(parts[3].trim());
                    Integer jeep = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : null;
                    addEdge(from, to, new EdgeWeight(walk, car, jeep));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Backtrack algorithm logic
class BackTrackAlgo {
    private Graph graph;
    private int bestDistance;
    private List<String> bestPath;
    private int currentDistance;
    private List<String> currentPath;
    private List<Step> bestSteps;

    public BackTrackAlgo(Graph graph) {
        this.graph = graph;
    }

    public void findShortestPath(String startNode) {
        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<Step> steps = new ArrayList<>();

        path.add(startNode);
        visited.add(startNode);
        bestDistance = Integer.MAX_VALUE;
        bestPath = new ArrayList<>();
        bestSteps = new ArrayList<>();

        backtrack(path, visited, 0, steps);
        currentDistance = bestDistance;
        currentPath = new ArrayList<>(bestPath);
    }

    private void backtrack(List<String> path, Set<String> visited, int currentDistance, List<Step> steps) {
        if (visited.size() == graph.getAdjList().size()) {
            String last = path.get(path.size() - 1);
            String start = path.get(0);
            if (graph.getAdjList().get(last).containsKey(start)) {
                EdgeWeight ew = graph.getAdjList().get(last).get(start);
                String mode = getBestMode(ew);
                int returnWeight = ew.getMode(mode);

                if (currentDistance + returnWeight < bestDistance) {
                    bestDistance = currentDistance + returnWeight;
                    bestPath = new ArrayList<>(path);
                    bestPath.add(start);

                    bestSteps = new ArrayList<>(steps);
                    bestSteps.add(new Step(last, start, mode, returnWeight));
                }
            }
            return;
        }

        String current = path.get(path.size() - 1);
        for (String neighbor : graph.getAdjList().get(current).keySet()) {
            if (!visited.contains(neighbor)) {
                EdgeWeight ew = graph.getAdjList().get(current).get(neighbor);
                String mode = getBestMode(ew);
                int weight = ew.getMode(mode);

                visited.add(neighbor);
                path.add(neighbor);
                steps.add(new Step(current, neighbor, mode, weight));

                backtrack(path, visited, currentDistance + weight, steps);

                visited.remove(neighbor);
                path.remove(path.size() - 1);
                steps.remove(steps.size() - 1);
            }
        }
    }

    private String getBestMode(EdgeWeight ew) {
        int walk = ew.getWalk();
        int car = ew.getCar();
        Integer jeep = ew.getJeepney();
        int jeepVal = (jeep != null) ? jeep : Integer.MAX_VALUE;

        int min = Math.min(walk, Math.min(car, jeepVal));
        if (min == walk) return "walk";
        if (min == car) return "car";
        return "jeep";
    }

    public List<String> getBestPath() { return bestPath; }
    public int getBestDistance() { return bestDistance; }
    public List<String> getCurrentPath() { return currentPath; }
    public int getCurrentDistance() { return currentDistance; }
    public List<Step> getBestSteps() { return bestSteps; }
}
class Step {
    String from;
    String to;
    String modeUsed;
    int weightUsed;

    public Step(String from, String to, String modeUsed, int weightUsed) {
        this.from = from;
        this.to = to;
        this.modeUsed = modeUsed;
        this.weightUsed = weightUsed;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getModeUsed() { return modeUsed; }
    public int getWeightUsed() { return weightUsed; }
}


//Random event that adds weight for each
class RandomEventSimulator {
    public static void start(Runnable eventCallback) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(eventCallback, 10, 10, TimeUnit.SECONDS);
    }
}

//App main class
public class NavigationSimulation {
    public static void main(String[] args) {
        List<String> files = Arrays.asList("district1.csv", "district2.csv");
        Graph graph = new Graph(files);
        String mode = "car"; // can be "walk", "car", or "jeep"
        BackTrackAlgo solver = new BackTrackAlgo(graph, mode);

        String startNode = "Quiapo";
        solver.findShortestPath(startNode);

        RandomEventSimulator.start(() -> {
            graph.simulateRandomEvent(mode);
            solver.findShortestPath(startNode);
            // Communicate result to frontend (no console output)
        });
    }
}

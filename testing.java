
// Save this as testing.java
import java.io.*;
import java.util.*;

class EdgeWeight {
    private Integer walk, car, jeepney;

    public EdgeWeight(Integer walk, Integer car, Integer jeepney) {
        this.walk = walk;
        this.car = car;
        this.jeepney = jeepney;
    }

    public Integer getWalk() {
        return walk;
    }

    public Integer getCar() {
        return car;
    }

    public Integer getJeepney() {
        return jeepney;
    }

    public int getMode(String mode) {
        return switch (mode) {
            case "walk" -> walk != null ? walk : Integer.MAX_VALUE;
            case "car" -> car != null ? car : Integer.MAX_VALUE;
            case "jeep" -> jeepney != null ? jeepney : Integer.MAX_VALUE;
            default -> throw new IllegalArgumentException("Invalid mode: " + mode);
        };
    }
}

class Step {
    private final String from, to, mode;
    private final int weight;

    public Step(String from, String to, String mode, int weight) {
        this.from = from;
        this.to = to;
        this.mode = mode;
        this.weight = weight;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getModeUsed() {
        return mode;
    }

    public int getWeightUsed() {
        return weight;
    }
}

class Graph {
    private final Map<String, Map<String, EdgeWeight>> adjList = new HashMap<>();
    private final List<String> eventMessages = new ArrayList<>();

    public Graph(List<String> files) {
        for (String file : files)
            loadFromCSV(file);
    }

    public Map<String, Map<String, EdgeWeight>> getAdjList() {
        return adjList;
    }

    public void addEdge(String from, String to, EdgeWeight weight) {
        adjList.computeIfAbsent(from, k -> new HashMap<>()).put(to, weight);
        adjList.computeIfAbsent(to, k -> new HashMap<>()).put(from, weight);
    }

    public void simulateRandomEventsAffectingPath(List<Step> path, String mode) {
        Set<String> pathEdges = new HashSet<>();
        for (Step s : path) {
            pathEdges.add(s.getFrom() + "<->" + s.getTo());
            pathEdges.add(s.getTo() + "<->" + s.getFrom());
        }

        Random rand = new Random();
        boolean pathAffected = false;
        int count = 0;

        System.out.println("\n======= RANDOM EVENTS OCCURRED =======");

        while (count < 3) {
            List<String> nodes = new ArrayList<>(adjList.keySet());
            String from = nodes.get(rand.nextInt(nodes.size()));
            List<String> neighbors = new ArrayList<>(adjList.get(from).keySet());
            if (neighbors.isEmpty())
                continue;

            String to = neighbors.get(rand.nextInt(neighbors.size()));
            String edgeKey = from + "<->" + to;
            boolean willAffectPath = pathEdges.contains(edgeKey);

            if (!pathAffected && count == 2 && !willAffectPath)
                continue;

            int eventType = rand.nextInt(5);
            boolean success = switch (eventType) {
                case 0 -> increaseWeight(from, to, "Flooding reported", mode);
                case 1 -> blockEdge(from, to, "Route closed due to protest");
                case 2 -> increaseWeight(from, to, "Accident causing delay", mode);
                case 3 -> increaseWeight(from, to, "Traffic congestion", mode);
                case 4 -> blockEdge(from, to, "Road under construction");
                default -> false;
            };

            if (success) {
                pathAffected |= willAffectPath;
                count++;
            }
        }

        for (String msg : eventMessages)
            System.out.println("- " + msg);
    }

    private boolean blockEdge(String from, String to, String reason) {
        if (adjList.containsKey(from) && adjList.get(from).containsKey(to)) {
            adjList.get(from).remove(to);
            adjList.get(to).remove(from);
            eventMessages.add("General - " + reason + " on " + from + " <--> " + to);
            return true;
        }
        return false;
    }

    private boolean increaseWeight(String from, String to, String reason, String mode) {
        if (!adjList.containsKey(from) || !adjList.get(from).containsKey(to))
            return false;

        EdgeWeight ew = adjList.get(from).get(to);
        int current = ew.getMode(mode);
        if (current == Integer.MAX_VALUE) {
            eventMessages.add("No weight for mode '" + mode + "' on " + from + " <--> " + to);
            return false;
        }

        int added = new Random().nextInt(20) + 5;
        int newWeight = current + added;

        Integer walk = ew.getWalk(), car = ew.getCar(), jeep = ew.getJeepney();
        switch (mode) {
            case "walk" -> walk = (walk != null ? walk + added : added);
            case "car" -> car = (car != null ? car + added : added);
            case "jeep" -> jeep = (jeep != null ? jeep + added : added);
        }

        EdgeWeight updated = new EdgeWeight(walk, car, jeep);
        adjList.get(from).put(to, updated);
        adjList.get(to).put(from, updated);

        eventMessages.add("General - " + reason + ": " + from + " <--> " + to + " now " + newWeight + " mins");
        return true;
    }

    private void loadFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2)
                    continue;

                String from = parts[0].trim(), to = parts[1].trim();
                Integer walk = (parts.length > 2 && !parts[2].isEmpty()) ? Integer.parseInt(parts[2].trim()) : null;
                Integer car = (parts.length > 3 && !parts[3].isEmpty()) ? Integer.parseInt(parts[3].trim()) : null;
                Integer jeep = (parts.length > 4 && !parts[4].isEmpty()) ? Integer.parseInt(parts[4].trim()) : null;

                if (walk == null && car == null && jeep == null)
                    continue;
                addEdge(from, to, new EdgeWeight(walk, car, jeep));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class BackTrackAlgo {
    private final Graph graph;
    private int bestDist;
    private List<String> bestPath;
    private List<Step> bestSteps;

    public BackTrackAlgo(Graph graph) {
        this.graph = graph;
    }

    public void findShortestPath(String start, String end) {
        bestDist = Integer.MAX_VALUE;
        bestPath = new ArrayList<>();
        bestSteps = new ArrayList<>();

        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<Step> steps = new ArrayList<>();

        visited.add(start);
        path.add(start);
        dfs(path, visited, 0, steps, end);
    }

    private void dfs(List<String> path, Set<String> visited, int dist, List<Step> steps, String end) {
        String current = path.get(path.size() - 1);
        if (current.equals(end)) {
            if (dist < bestDist) {
                bestDist = dist;
                bestPath = new ArrayList<>(path);
                bestSteps = new ArrayList<>(steps);
            }
            return;
        }

        Map<String, EdgeWeight> neighbors = graph.getAdjList().getOrDefault(current, Collections.emptyMap());
        for (String neighbor : neighbors.keySet()) {
            if (!visited.contains(neighbor)) {
                EdgeWeight ew = neighbors.get(neighbor);
                String mode = getBestMode(ew);
                int weight = ew.getMode(mode);

                visited.add(neighbor);
                path.add(neighbor);
                steps.add(new Step(current, neighbor, mode, weight));

                dfs(path, visited, dist + weight, steps, end);

                visited.remove(neighbor);
                path.remove(path.size() - 1);
                steps.remove(steps.size() - 1);
            }
        }
    }

    private String getBestMode(EdgeWeight ew) {
        Map<String, Integer> modes = new HashMap<>();
        if (ew.getWalk() != null)
            modes.put("walk", ew.getWalk());
        if (ew.getCar() != null)
            modes.put("car", ew.getCar());
        if (ew.getJeepney() != null)
            modes.put("jeep", ew.getJeepney());
        return modes.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow();
    }

    public List<String> getBestPath() {
        return bestPath;
    }

    public int getBestDistance() {
        return bestDist;
    }

    public List<Step> getBestSteps() {
        return bestSteps;
    }
}

public class testing {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Graph graph = new Graph(List.of("C:/Users/johnr/Downloads/Custom_Route.csv"));
        BackTrackAlgo algo = new BackTrackAlgo(graph);

        System.out.print("Enter start node: ");
        String start = scanner.nextLine().trim();
        System.out.print("Enter end node: ");
        String end = scanner.nextLine().trim();

        algo.findShortestPath(start, end);
        List<Step> originalSteps = algo.getBestSteps();

        System.out.println("\n======= BEFORE RANDOM EVENTS =======");
        printResults(start, end, algo);

        graph.simulateRandomEventsAffectingPath(originalSteps, "car");

        System.out.println("\n===== AFTER RANDOM EVENTS =====");
        algo.findShortestPath(start, end);
        printResults(start, end, algo);
    }

    private static void printResults(String start, String end, BackTrackAlgo algo) {
        System.out.println("Best Path from " + start + " to " + end + ": " + algo.getBestPath());
        System.out.println("Total Distance: " + algo.getBestDistance());
        System.out.println("\nSteps:");
        for (Step s : algo.getBestSteps()) {
            System.out.println(
                    s.getFrom() + " -> " + s.getTo() + " [" + s.getModeUsed() + ", " + s.getWeightUsed() + "]");
        }
    }
}
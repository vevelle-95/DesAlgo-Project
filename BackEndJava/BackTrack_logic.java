import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class PathfindingApp {

    private final Graph graph;
    private final BackTrackAlgo solver;
    private final String mode = "mixcommute";

    public PathfindingApp() {
        List<String> files = List.of("C:/Users/johnr/Downloads/big_map.csv"); // Update path as needed
        this.graph = new Graph(files);
        this.solver = new BackTrackAlgo(graph);
    }

    public static void main(String[] args) {
        SpringApplication.run(PathfindingApp.class, args);
    }

    @GetMapping("/shortest-path")
    public Map<String, Object> getShortestPath(@RequestParam String start, @RequestParam String end) {
        graph.simulateRandomEvent(mode);
        solver.findShortestPath(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("event",
                Optional.ofNullable(graph.getLastEvent()).map(SimulationEvent::getMessage).orElse("None"));
        response.put("path", solver.getBestPath());
        response.put("distance", solver.getBestDistance());

        List<Map<String, Object>> stepList = new ArrayList<>();
        for (Step step : solver.getBestSteps()) {
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("from", step.getFrom());
            stepMap.put("to", step.getTo());
            stepMap.put("mode", step.getModeUsed());
            stepMap.put("weight", step.getWeightUsed());
            stepList.add(stepMap);
        }

        response.put("steps", stepList);
        return response;
    }

    // ==== Inner Classes Below ====

    static class EdgeWeight {
        private int walk;
        private int mixcommute;
        private Integer jeepney;

        public EdgeWeight(int walk, int mixcommute, Integer jeepney) {
            this.walk = walk;
            this.mixcommute = mixcommute;
            this.jeepney = jeepney;
        }

        public int getWalk() {
            return walk;
        }

        public int getmixcommute() {
            return mixcommute;
        }

        public Integer getJeepney() {
            return jeepney;
        }

        public int getMode(String mode) {
            return switch (mode) {
                case "walk" -> walk;
                case "mixcommute" -> mixcommute;
                case "jeep" -> (jeepney != null ? jeepney : Integer.MAX_VALUE);
                default -> throw new IllegalArgumentException("Invalid mode: " + mode);
            };
        }
    }

    static class SimulationEvent {
        private String type, from, to, message;

        public SimulationEvent(String type, String from, String to, String message) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static class Graph {
        private Map<String, Map<String, EdgeWeight>> adjList = new HashMap<>();
        private SimulationEvent lastEvent;

        public Graph(List<String> filenames) {
            for (String filename : filenames)
                loadFromCSV(filename);
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
            if (nodes.size() < 2)
                return;

            Random rand = new Random();
            String from = nodes.get(rand.nextInt(nodes.size()));
            List<String> neighbors = new ArrayList<>(adjList.get(from).keySet());
            if (neighbors.isEmpty())
                return;
            String to = neighbors.get(rand.nextInt(neighbors.size()));

            int eventType = rand.nextInt(5);
            switch (eventType) {
                case 0 -> increaseEdgeWeight(from, to, "Flooding reported", mode);
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
                lastEvent = new SimulationEvent("block", from, to, reason + " on " + from + " <-> " + to);
            }
        }

        private void increaseEdgeWeight(String from, String to, String reason, String mode) {
            EdgeWeight ew = adjList.get(from).get(to);
            int oldWeight = ew.getMode(mode);
            int added = new Random().nextInt(20) + 5;
            int newWeight = oldWeight + added;

            int walk = ew.getWalk();
            int mixcommute = ew.getmixcommute();
            Integer jeep = ew.getJeepney();

            switch (mode) {
                case "walk" -> walk = newWeight;
                case "mixcommute" -> mixcommute = newWeight;
                case "jeep" -> jeep = newWeight;
            }

            EdgeWeight updated = new EdgeWeight(walk, mixcommute, jeep);
            adjList.get(from).put(to, updated);
            adjList.get(to).put(from, updated);
            lastEvent = new SimulationEvent("traffic", from, to,
                    reason + ": " + from + " ↔ " + to + " now " + newWeight + " mins");
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
                        int mixcommute = Integer.parseInt(parts[3].trim());
                        Integer jeep = (parts.length > 4) ? Integer.parseInt(parts[4].trim()) : null;
                        addEdge(from, to, new EdgeWeight(walk, mixcommute, jeep));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class BackTrackAlgo {
        private Graph graph;
        private int bestDistance;
        private List<String> bestPath;
        private List<Step> bestSteps;

        public BackTrackAlgo(Graph graph) {
            this.graph = graph;
        }

        public void findShortestPath(String startNode, String endNode) {
            Set<String> visited = new HashSet<>();
            List<String> path = new ArrayList<>();
            List<Step> steps = new ArrayList<>();

            path.add(startNode);
            visited.add(startNode);
            bestDistance = Integer.MAX_VALUE;
            bestPath = new ArrayList<>();
            bestSteps = new ArrayList<>();

            backtrack(path, visited, 0, steps, endNode);
        }

        private void backtrack(List<String> path, Set<String> visited, int currentDistance, List<Step> steps,
                String endNode) {
            String current = path.get(path.size() - 1);
            if (current.equals(endNode)) {
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestPath = new ArrayList<>(path);
                    bestSteps = new ArrayList<>(steps);
                }
                return;
            }

            for (String neighbor : graph.getAdjList().getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (!visited.contains(neighbor)) {
                    EdgeWeight ew = graph.getAdjList().get(current).get(neighbor);
                    String mode = getBestMode(ew);
                    int weight = ew.getMode(mode);

                    visited.add(neighbor);
                    path.add(neighbor);
                    steps.add(new Step(current, neighbor, mode, weight));

                    backtrack(path, visited, currentDistance + weight, steps, endNode);

                    visited.remove(neighbor);
                    path.remove(path.size() - 1);
                    steps.remove(steps.size() - 1);
                }
            }
        }

        private String getBestMode(EdgeWeight ew) {
            int walk = ew.getWalk(), mixcommute = ew.getmixcommute();
            int jeep = (ew.getJeepney() != null) ? ew.getJeepney() : Integer.MAX_VALUE;

            int min = Math.min(walk, Math.min(mixcommute, jeep));
            return (min == walk) ? "walk" : (min == mixcommute) ? "mixcommute" : "jeep";
        }

        public List<String> getBestPath() {
            return bestPath;
        }

        public int getBestDistance() {
            return bestDistance;
        }

        public List<Step> getBestSteps() {
            return bestSteps;
        }
    }

    static class Step {
        private final String from, to, modeUsed;
        private final int weightUsed;

        public Step(String from, String to, String modeUsed, int weightUsed) {
            this.from = from;
            this.to = to;
            this.modeUsed = modeUsed;
            this.weightUsed = weightUsed;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getModeUsed() {
            return modeUsed;
        }

        public int getWeightUsed() {
            return weightUsed;
        }
    }
}

package src.navapp.service;

import src.navapp.model.EdgeWeight;
import src.nvapp.model.SimulationEvent;

import java.io.*;
import java.util.*;

public class Graph {
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

        switch (rand.nextInt(5)) {
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
        int mixcommute = ew.getMixcommute();
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
                    Integer jeep = null;
                    if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                        jeep = Integer.parseInt(parts[4].trim());
                    }
                    addEdge(from, to, new EdgeWeight(walk, mixcommute, jeep));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

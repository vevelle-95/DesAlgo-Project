package navapp.service;

import navapp.model.EdgeWeight;
import navapp.model.Step;

import java.util.*;

public class BackTrackAlgo {
    private final Graph graph;
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

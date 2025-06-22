package src.navapp.controller;

import src.navapp.model.Step;
import src.navapp.service.BackTrackAlgo;
import src.navapp.service.Graph;
import src.navapp.model.SimulationEvent;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class PathfindingController {

    private final Graph graph;
    private final BackTrackAlgo solver;
    private final String mode = "mixsrcmute";

    public PathfindingController() {
        List<String> files = List.of("C:/Users/johnr/Downloads/big_map.csv");
        this.graph = new Graph(files);
        this.solver = new BackTrackAlgo(graph);
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
}

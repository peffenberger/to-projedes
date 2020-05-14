package cz.effy.projedes.dto;

import java.util.List;

public class RoadState {

    private List<String> delays;

    private List<String> colors;

    public RoadState(List<String> delays, List<String> colors) {
        this.delays = delays;
        this.colors = colors;
    }

    public String getTextDelays() {
        StringBuilder sb = new StringBuilder();
        if (delays != null) {
            if (delays.size() > 0) {
                sb.append(delays.get(0));
            }
            if (delays.size() == 2) {
                sb.append("\n");
                sb.append(delays.get(1));
            }

        }
        return sb.toString();
    }

    public List<String> getColors() {
        return colors;
    }
}

package models;

import java.util.ArrayList;
import java.util.List;

public class Cluster<T> {
    private final List<T> points;
    private int clusterId;

    public Cluster(final T center) {
        points = new ArrayList<T>();
    }

    public int getClusterId() {
        return clusterId;
    }

    public void addPoint(final T point) {
        points.add(point);
    }

    public List<T> getPoints() {
        return points;
    }
}
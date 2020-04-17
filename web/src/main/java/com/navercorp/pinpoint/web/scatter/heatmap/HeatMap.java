package com.navercorp.pinpoint.web.scatter.heatmap;

import java.util.List;
import java.util.Objects;

public class HeatMap {
    private final List<Point> data;
    private final long success;
    private final long fail;


    public HeatMap(List<Point> data, long success, long fail) {
        this.data = Objects.requireNonNull(data, "data");
        this.success = success;
        this.fail = fail;
    }

    public List<Point> getData() {
        return data;
    }

    public long getSuccess() {
        return success;
    }

    public long getFail() {
        return fail;
    }

    @Override
    public String toString() {
        return "HeatMap{" +
                "data=" + data.size() +
                ", success=" + success +
                ", fail=" + fail +
                '}';
    }
}

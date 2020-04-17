package com.navercorp.pinpoint.web.scatter.heatmap;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class HeatMapBuilderTest {
    @Test
    public void axix() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 0, 1000);

        Assert.assertEquals(0L, resolver.getIndex(0));
        Assert.assertEquals(10L, resolver.getIndex(100));
    }

    @Test
    public void axix_max_overflow() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 0, 1000);

        Assert.assertEquals(100, resolver.getIndex(1000));
        Assert.assertEquals(100, resolver.getIndex(1100));
    }

    @Test
    public void axix_max_overflow_start() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 100, 1100);

        Assert.assertEquals(0, resolver.getIndex(100));
        Assert.assertEquals(100, resolver.getIndex(1200));
    }

    @Test
    public void addPoint1() {

        HeatMapBuilder builder = HeatMapBuilder.newBuilder(0, 1000, 10, 0, 1000, 10);

        builder.addDataPoint(new Point(1, 1));
        builder.addDataPoint(new Point(1, 2));

        builder.addDataPoint(new Point(101, 101));

        HeatMap heatMap = builder.build();
        List<Point> points = heatMap.getData();

        Assert.assertEquals(points.size(), 2);

        long sum = points.stream().mapToLong(Point::getSuccess).sum();
        Assert.assertEquals(3L, sum);

        Assert.assertEquals(1, points.get(0).getSuccess());
        Assert.assertEquals(2, points.get(1).getSuccess());
    }

    @Test
    public void addPoint2() {

        HeatMapBuilder builder = HeatMapBuilder.newBuilder(0, 1000, 100, 0, 1000, 100);

        builder.addDataPoint(new Point(1, 1));
        builder.addDataPoint(new Point(1, 200));

        HeatMap heatMap = builder.build();
        List<Point> points = heatMap.getData();

        Assert.assertEquals(points.size(), 2);


        Assert.assertEquals(0, points.get(0).getY());
        Assert.assertEquals(20, points.get(1).getY());


    }
}
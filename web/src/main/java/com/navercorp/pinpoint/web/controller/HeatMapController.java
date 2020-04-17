package com.navercorp.pinpoint.web.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.Status;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMap;
import com.navercorp.pinpoint.web.scatter.heatmap.Point;
import com.navercorp.pinpoint.web.service.HeatMapService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.view.TransactionMetaDataViewModel;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequestMapping("/heatmap")
@Controller
public class HeatMapController {


    private static final String COMPLETE = "complete";
    private static final String RESULT_TO = "resultTo";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HeatMapService heatMap;

    @RequestMapping(value = "/drag", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> dragScatterArea(
            @RequestParam("application") String applicationName,
            @RequestParam("x1") long x1,
            @RequestParam("x2") long x2,
            @RequestParam("y1") long y1,
            @RequestParam("y2") long y2,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit ) {

        limit = LimitUtils.checkRange(limit);

        StopWatch watch = new StopWatch();
        watch.start("dragScatterArea");
        DragArea dragArea = DragArea.normalize(x1, x2, y1, y2);

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.newUncheckedRange(x1, x2);
        logger.debug("drag scatter data. RANGE={}, LIMIT={}", range, limit);

        final LimitedScanResult<List<SpanBo>> scatterData = heatMap.dragScatterData(applicationName, dragArea, limit);
        if (logger.isDebugEnabled()) {
            logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scatterData.getScanData().size());
        }
        watch.stop();

        if (logger.isDebugEnabled()) {
            logger.debug("Fetch dragScatterArea time : {}ms", watch.getLastTaskTimeMillis());
        }


        List<SpanBo> scanData = scatterData.getScanData();
        TransactionMetaDataViewModel transaciton = new TransactionMetaDataViewModel(scanData);
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("metadata", transaciton.getMetadata());

        boolean complete = scanData.size() < limit;
        modelMap.put(COMPLETE, complete);
        modelMap.put(RESULT_TO, scatterData.getLimitedTime());

        return modelMap;

    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public HeatMapController.HeatMapViewModel getHeatMapData(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.newUncheckedRange(from, to);
        logger.debug("fetch getHeatMapData. RANGE={}, ", range);

        LimitedScanResult<HeatMap> scanResult = this.heatMap.getHeatMap(applicationName, range, TimeUnit.SECONDS.toMillis(10), LimitUtils.MAX);
        Status status = new Status(System.currentTimeMillis(), range);


        return new HeatMapController.HeatMapViewModel(scanResult.getScanData(), status);
    }


    public static class HeatMapViewModel {
        private final HeatMap heatMap;
        private final Status status;

        public HeatMapViewModel(HeatMap heatMap, Status status) {
            this.heatMap = Objects.requireNonNull(heatMap, "heatMap");
            this.status = Objects.requireNonNull(status, "status");
        }


        @JsonProperty("data")
        public List<long[]> getData() {
            List<Point> pointList = heatMap.getData();
            final List<long[]> list = new ArrayList<>(pointList.size());
            for (Point point : pointList) {
                long[] longs = {point.getX(), point.getY(), point.getSuccess(), point.getFail()};
                list.add(longs);
            }
            return list;
        }

        public long success() {
            return heatMap.getSuccess();
        }

        public long fail() {
            return heatMap.getFail();
        }

        @JsonUnwrapped
        public Status getStatus() {
            return status;
        }

//        public long getResultFrom() {
//            return scatter.getOldestAcceptedTime();
//        }
//
//        public long getResultTo() {
//            return scatter.getLatestAcceptedTime();
//        }

    }
}

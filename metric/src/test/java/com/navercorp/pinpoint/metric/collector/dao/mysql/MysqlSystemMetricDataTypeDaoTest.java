package com.navercorp.pinpoint.metric.collector.dao.mysql;


import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:pinot-collector/applicationContext-collector-test.xml", "classpath:pinot-collector/applicationContext-collector-pinot.xml"})
@TestPropertySource(properties = {"pinpoint.profiles.active=local"})
@WebAppConfiguration
@Transactional("metricTransactionManager")
public class MysqlSystemMetricDataTypeDaoTest {

    @Autowired
    MysqlSystemMetricDataTypeDao systemMetricDataTypeDao;

    @Test
    public void insertSelectTest() {
        MetricData metricData1 = new MetricData("metricName1", "fieldName1", MetricDataType.DOUBLE);
        MetricData metricData2 = new MetricData("metricName2", "fieldName2", MetricDataType.LONG);
        systemMetricDataTypeDao.updateMetricDataType(metricData1);
        systemMetricDataTypeDao.updateMetricDataType(metricData2);

        MetricData metricData1Result = systemMetricDataTypeDao.selectMetricDataType(new MetricDataName(metricData1.getMetricName(), metricData1.getFieldName()));
        assertEquals(metricData1Result.getMetricDataType(), metricData1.getMetricDataType());

        MetricData metricData2Result = systemMetricDataTypeDao.selectMetricDataType(new MetricDataName(metricData2.getMetricName(), metricData2.getFieldName()));
        assertEquals(metricData2Result.getMetricDataType(), metricData2.getMetricDataType());
    }

    @Test
    public void upSertTest() {
        MetricData metricData1 = new MetricData("metricName1", "fieldName1", MetricDataType.DOUBLE);
        MetricData metricData2 = new MetricData("metricName2", "fieldName2", MetricDataType.LONG);
        systemMetricDataTypeDao.updateMetricDataType(metricData1);
        systemMetricDataTypeDao.updateMetricDataType(metricData2);
        systemMetricDataTypeDao.updateMetricDataType(metricData1);
        systemMetricDataTypeDao.updateMetricDataType(metricData2);

        MetricData metricData1Result = systemMetricDataTypeDao.selectMetricDataType(new MetricDataName(metricData1.getMetricName(), metricData1.getFieldName()));
        MetricData metricData2Result = systemMetricDataTypeDao.selectMetricDataType(new MetricDataName(metricData2.getMetricName(), metricData2.getFieldName()));
        assertNotNull(metricData1Result);
        assertNotNull(metricData2Result);
    }

}
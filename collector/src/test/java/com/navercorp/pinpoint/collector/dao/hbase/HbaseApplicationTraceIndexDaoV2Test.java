package com.navercorp.pinpoint.collector.dao.hbase;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;


public class HbaseApplicationTraceIndexDaoV2Test {

    @Test
    public void newRowKey() {
        int index = 9;
        byte[] rowKey  = HbaseApplicationTraceIndexDaoV2.newRowKey(Bytes.toBytes("a"), 24, 2, (byte) index);
        Assert.assertEquals(rowKey[rowKey.length-1], index);


    }
}
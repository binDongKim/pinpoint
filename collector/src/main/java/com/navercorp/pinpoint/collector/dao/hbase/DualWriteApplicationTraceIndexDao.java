package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DualWriteApplicationTraceIndexDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationTraceIndexDao master;
    private final ApplicationTraceIndexDao secondary;

    public DualWriteApplicationTraceIndexDao(ApplicationTraceIndexDao master, ApplicationTraceIndexDao secondary) {
        this.master = Objects.requireNonNull(master, "master");
        this.secondary = Objects.requireNonNull(secondary, "secondary");
    }

//    @Override
    public void insert(SpanBo span) {
        Throwable masterException = null;
        try {
            master.insert(span);
        } catch (Throwable th) {
            masterException = th;
        }
        try {
            secondary.insert(span);
        } catch (Exception e) {
            logger.info("secondary insert fail {}", span);
        }
        if (masterException != null) {
            throwUncheckedException(masterException);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void throwUncheckedException(final Throwable throwable) throws T {
        throw (T) throwable;
    }
}

/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class FileDescriptorDataCollector extends DataCollector implements FileDescriptorDataGetter {
    private final AgentStatDao<FileDescriptorBo> fileDescriptorDao;
    private final List<String> agentIds;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init = new AtomicBoolean(false);

    private final Map<String, Long> fileDescriptorCount = new HashMap<>();

    public FileDescriptorDataCollector(
            DataCollectorCategory dataCollectorCategory,
            AgentStatDao<FileDescriptorBo> fileDescriptorDao,
            List<String> agentIds,
            long timeSlotEndTime,
            long slotInterval
    ) {
        super(dataCollectorCategory);

        this.fileDescriptorDao = fileDescriptorDao;
        this.agentIds = agentIds;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range range = Range.between(timeSlotEndTime - slotInterval, timeSlotEndTime);

        for(String agentId : agentIds) {
            List<FileDescriptorBo> fileDescriptorBoList = fileDescriptorDao.getAgentStatList(agentId, range);

            if (fileDescriptorBoList.isEmpty()) {
                continue;
            }

            long fileDescriptorTotalCount = 0;
            int size = fileDescriptorBoList.size();
            for (FileDescriptorBo fileDescriptorBo : fileDescriptorBoList) {
                fileDescriptorTotalCount += fileDescriptorBo.getOpenFileDescriptorCount();
            }

            final long fileDescriptorAvgCount = fileDescriptorTotalCount / size;

            fileDescriptorCount.put(agentId, fileDescriptorAvgCount);
        }

        init.set(true);
    }

    @Override
    public Map<String, Long> getFileDescriptorCount() {
        return fileDescriptorCount;
    }
}

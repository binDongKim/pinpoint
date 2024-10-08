/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.realtime.collector.sink;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import io.grpc.stub.ServerCallStreamObserver;
import reactor.core.publisher.FluxSink;

import java.util.Objects;

public class ActiveThreadCountPublisher implements Publisher<PCmdActiveThreadCountRes> {

    private final FluxSink<PCmdActiveThreadCountRes> sink;
    private ServerCallStreamObserver<Empty> streamObserver;

    public ActiveThreadCountPublisher(FluxSink<PCmdActiveThreadCountRes> sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    @Override
    public void publish(PCmdActiveThreadCountRes response) {
        this.sink.next(response);
    }

    @Override
    public void error(Throwable throwable) {
        this.sink.error(throwable);
    }

    public void complete() {
        this.sink.complete();
    }

    public void setStreamObserver(ServerCallStreamObserver<Empty> streamObserver) {
        this.streamObserver = streamObserver;
    }

    public void closeStream() {
        if (this.streamObserver != null && this.streamObserver.isReady()) {
            this.streamObserver.onCompleted();
        }
    }
}

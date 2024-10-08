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

package com.navercorp.pinpoint.it.plugin.grpc;

import com.navercorp.pinpoint.it.plugin.utils.ExecutorUtils;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.examples.manualflowcontrol.HelloReply;
import io.grpc.examples.manualflowcontrol.HelloRequest;
import io.grpc.examples.manualflowcontrol.StreamingGreeterGrpc;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * copy grpc framework
 * - https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/manualflowcontrol/ManualFlowControlClient.java
 */
public class HelloWorldStreamClient implements HelloWorldClient {

    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    private final ManagedChannel channel;
    private final StreamingGreeterGrpc.StreamingGreeterStub stub;

    private final ExecutorService workerExecutor;
    private final NioEventLoopGroup eventExecutors;

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    @SuppressWarnings("deprecated")
    public HelloWorldStreamClient(String host, int port) {
        this.workerExecutor = Executors.newCachedThreadPool();
        this.eventExecutors = new NioEventLoopGroup(5, workerExecutor);

        this.channel = newChannel(host, port, eventExecutors);
        this.stub = StreamingGreeterGrpc.newStub(channel);
    }

    private static ManagedChannel newChannel(String host, int port, EventLoopGroup eventExecutors) {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        BuilderUtils.usePlainText(builder);

        builder.eventLoopGroup(eventExecutors);
        builder.channelType(NioSocketChannel.class);

        builder.intercept(MetadataUtils.newCaptureMetadataInterceptor(new AtomicReference<Metadata>(), new AtomicReference<Metadata>()));
        return builder.build();
    }

    @Override
    public void close() {
        ShutdownUtils.shutdownChannel(channel);
        ShutdownUtils.shutdownEventExecutor(eventExecutors);
        ExecutorUtils.shutdownAndAwaitTermination(workerExecutor, 3, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public void greet(final int callCount) throws InterruptedException {

        final CountDownLatch done = new CountDownLatch(1);

        ClientResponseObserver<HelloRequest, HelloReply> clientResponseObserver =
                new ClientResponseObserver<HelloRequest, HelloReply>() {

                    ClientCallStreamObserver<HelloRequest> requestStream;

                    @Override
                    public void beforeStart(final ClientCallStreamObserver<HelloRequest> requestStream) {
                        this.requestStream = requestStream;
                        // Set up manual flow control for the response stream. It feels backwards to configure the response
                        // stream's flow control using the request stream's observer, but this is the way it is.
                        requestStream.disableAutoInboundFlowControl();

                        // Set up a back-pressure-aware producer for the request stream. The onReadyHandler will be invoked
                        // when the consuming side has enough buffer space to receive more messages.
                        //
                        // Messages are serialized into a transport-specific transmit buffer. Depending on the size of this buffer,
                        // MANY messages may be buffered, however, they haven't yet been sent to the server. The server must call
                        // request() to pull a buffered message from the client.
                        //
                        // Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming
                        // StreamObserver'sonNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent
                        // additional messages from being processed by the incoming StreamObserver. The onReadyHandler must return
                        // in a timely manor or else message processing throughput will suffer.
                        requestStream.setOnReadyHandler(new Runnable() {
                            // An iterator is used so we can pause and resume iteration of the request data.
                            final Iterator<String> iterator = names().iterator();

                            @Override
                            public void run() {
                                int count = 0;

                                // Start generating values from where we left off on a non-gRPC thread.
                                while (requestStream.isReady()) {
                                    if (iterator.hasNext() && callCount > count) {
                                        // Send more messages if there are more messages to send.
                                        String name = iterator.next();
                                        logger.info("--> " + name);
                                        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
                                        requestStream.onNext(request);
                                        count++;
                                    } else {
                                        // Signal completion if there is nothing left to send.
                                        requestStream.onCompleted();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onNext(HelloReply value) {
                        logger.info("<-- {}", value.getMessage());
                        // Signal the sender to send one message.
                        requestStream.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Status status = Status.fromThrowable(t);
                        logger.info("onError:{}", status);
                        done.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("All Done");
                        done.countDown();
                    }
                };

        // Note: clientResponseObserver is handling both request and response stream processing.
        stub.sayHelloStreaming(clientResponseObserver);

        done.await();
    }

    private static List<String> names() {
        return Arrays.asList(
                "Sophia",
                "Jackson",
                "Emma",
                "Aiden",
                "Olivia",
                "Lucas",
                "Ava",
                "Liam",
                "Mia",
                "Noah",
                "Isabella",
                "Ethan",
                "Riley",
                "Mason",
                "Aria",
                "Caden",
                "Zoe",
                "Oliver",
                "Charlotte",
                "Elijah",
                "Lily",
                "Grayson",
                "Layla",
                "Jacob",
                "Amelia",
                "Michael",
                "Emily",
                "Benjamin",
                "Madelyn",
                "Carter",
                "Aubrey",
                "James",
                "Adalyn",
                "Jayden",
                "Madison",
                "Logan",
                "Chloe",
                "Alexander",
                "Harper",
                "Caleb",
                "Abigail",
                "Ryan",
                "Aaliyah",
                "Luke",
                "Avery",
                "Daniel",
                "Evelyn",
                "Jack",
                "Kaylee",
                "William",
                "Ella",
                "Owen",
                "Ellie",
                "Gabriel",
                "Scarlett",
                "Matthew",
                "Arianna",
                "Connor",
                "Hailey",
                "Jayce",
                "Nora",
                "Isaac",
                "Addison",
                "Sebastian",
                "Brooklyn",
                "Henry",
                "Hannah",
                "Muhammad",
                "Mila",
                "Cameron",
                "Leah",
                "Wyatt",
                "Elizabeth",
                "Dylan",
                "Sarah",
                "Nathan",
                "Eliana",
                "Nicholas",
                "Mackenzie",
                "Julian",
                "Peyton",
                "Eli",
                "Maria",
                "Levi",
                "Grace",
                "Isaiah",
                "Adeline",
                "Landon",
                "Elena",
                "David",
                "Anna",
                "Christian",
                "Victoria",
                "Andrew",
                "Camilla",
                "Brayden",
                "Lillian",
                "John",
                "Natalie",
                "Lincoln"
        );
    }


}

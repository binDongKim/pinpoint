/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpClientConfig;
import io.vertx.core.http.impl.HttpClientResponseImpl;

/**
 * @author jaehong.kim
 */
public class HttpClientRequestImplDoHandleResponseInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {

    private final boolean statusCode;

    public HttpClientRequestImplDoHandleResponseInterceptor(TraceContext traceContext) {
        super(traceContext);

        final VertxHttpClientConfig config = new VertxHttpClientConfig(traceContext.getProfilerConfig());
        this.statusCode = config.isStatusCode();
    }

    @Override
    public void beforeTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        if (trace.canSampled()) {
            if (!validate(args)) {
                return;
            }

            final HttpClientResponseImpl response = (HttpClientResponseImpl) args[0];
            if (statusCode) {
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, response.statusCode());
            }

            ((AsyncContextAccessor) response)._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
    }

    private boolean validate(final Object[] args) {
        Object httpClientResponseImpl = ArrayUtils.get(args, 0);
        if (!(httpClientResponseImpl instanceof HttpClientResponseImpl)) {
            return false;
        }

        if (!(httpClientResponseImpl instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);
            recorder.recordException(throwable);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}
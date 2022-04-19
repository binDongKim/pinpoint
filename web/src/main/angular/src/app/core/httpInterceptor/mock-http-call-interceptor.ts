import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpEvent, HttpHandler, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

import hostGroup from './mock-data/host-group.json';
import hostList from './mock-data/host-list.json';
import metricDef from './mock-data/metric-definition-id.json';
import metric from './mock-data/metric.json';

const exceptionObjMock = {
    exception: {
        message: 'java.lang.RuntimeException: org.apache.hadoop.hbase.client...',
        request: {
            method: 'GET',
            url: 'http://localhost:4200/xx/yy.pinpoint'
        },
        stacktrace: 'com.navercorp.pinpoint.common.hbase.parallel...'
    }
};
@Injectable()
export class MockHttpCallInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return req.url.includes('.pinpoint') ? this.handleRoutes(req, next) : next.handle(req);
    }

    handleRoutes(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const {url, method, body} = req;
        const apiName = url.match(/.*\/(.*)\.pinpoint/)[1];

        switch (apiName) {
            case 'hostGroup':
                return of(new HttpResponse({status: 200, body: hostGroup})).pipe(delay(500));
            case 'host':
                return of(new HttpResponse({status: 200, body: hostList})).pipe(delay(500));
            case 'collectedMetricInfo':
                return of(new HttpResponse({status: 200, body: metricDef})).pipe(delay(500));
            case 'collectedMetricData':
            // case 'drag':
            // case 'transactionmetadata':
            // case 'getResponseTimeHistogramDataV2':
            // case 'transactionInfo':
                // console.log('여기');
                // return of(new HttpResponse({status: 200, body: exceptionObjMock})).pipe(delay(500));
                return of(new HttpResponse({status: 200, body: metric})).pipe(delay(500));
                // const error = new HttpErrorResponse({status: 501, url: 'https://test.com'});
                // error.message = 'Error Occurred';
                // return throwError();
                // return next.handle(req);
            default:
                return next.handle(req);
        }
    }
}

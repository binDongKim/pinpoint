import React from 'react';
import { GetServerMap } from '@pinpoint-fe/constants';
import { ResponseSummaryChart, ResponseAvgMaxChart, LoadChart } from '../Chart';
import { cn } from '../../lib';
import { Separator } from '..';

export interface ChartsBoardProps {
  className?: string;
  chartsContainerClassName?: string;
  children?: React.ReactNode;
  header: React.ReactNode;
  nodeData?: GetServerMap.NodeData;
  emptyMessage?: string;
}

export const ChartsBoard = ({
  className,
  chartsContainerClassName,
  header,
  children,
  nodeData,
  emptyMessage,
}: ChartsBoardProps) => {
  const wrapperRef = React.useRef<HTMLDivElement>(null);
  const [gridMode, setGridMode] = React.useState(false);

  React.useEffect(() => {
    const wrapperElement = wrapperRef.current;
    if (!wrapperElement) return;
    const resizeObserver = new ResizeObserver(() => {
      wrapperElement.clientWidth > 650 ? setGridMode(true) : setGridMode(false);
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
    };
  }, []);

  return (
    <div className={cn('flex h-full w-full flex-col bg-white', className)}>
      {header}
      <div className={cn('w-full h-full overflow-y-auto', chartsContainerClassName)}>
        {children}
        {nodeData && (
          <div
            ref={wrapperRef}
            className={cn('grid grid-cols-[100%]', {
              'grid-cols-[50%_50%]': gridMode,
            })}
          >
            <div className="px-4 py-2.5">
              <ResponseSummaryChart
                className="h-40"
                title={<div className="flex items-center h-12 font-semibold">Response Summary</div>}
                categories={Object.keys(nodeData?.histogram || {})}
                data={(categories) => {
                  const histogram = nodeData?.histogram;
                  if (histogram) {
                    return categories?.map((category) => {
                      return histogram[category as keyof GetServerMap.Histogram] as number;
                    });
                  }
                  return [];
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <ResponseAvgMaxChart
                className="h-40"
                title={
                  <div className="flex items-center h-12 font-semibold">Response Avg & Max</div>
                }
                data={(categories) => {
                  const responseStatistics = nodeData?.responseStatistics;
                  if (responseStatistics) {
                    return categories!.map((category) => {
                      return responseStatistics?.[
                        category as keyof GetServerMap.ResponseStatistics
                      ];
                    });
                  }
                  return [];
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <LoadChart
                className="h-40"
                title={<div className="flex items-center h-12 font-semibold">Load</div>}
                colors={(() => {
                  const chartColors = ['#34b994', '#51afdf', '#ffba00', '#e67f22', '#e95459'];
                  const excludeKeys = ['Avg', 'Max', 'Sum', 'Tot'];
                  const data = nodeData?.timeSeriesHistogram;
                  const chartData = data?.filter(({ key }) => !excludeKeys.includes(key)) || [];

                  return chartData.reduce((prev, curr, i) => {
                    return { ...prev, [curr.key]: chartColors[i] };
                  }, {});
                })()}
                datas={() => {
                  const excludeKeys = ['Avg', 'Max', 'Sum', 'Tot'];
                  const data = nodeData?.timeSeriesHistogram;
                  const chartData = data?.filter(({ key }) => !excludeKeys.includes(key));

                  return {
                    dates: data?.[0]?.values?.map((v) => v[0]),
                    ...chartData?.reduce((prev, curr) => {
                      return {
                        ...prev,
                        [curr.key]: curr.values?.map?.((v) => v[1]),
                      };
                    }, {}),
                  };
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <LoadChart
                className="h-40"
                title={<div className="flex items-center h-12 font-semibold">Load Avg & Max</div>}
                colors={{
                  Avg: '#97E386',
                  Max: '#13B6E7',
                }}
                datas={{
                  dates: nodeData?.timeSeriesHistogram?.[0]?.values?.map((v) => v[0]),
                  ...['Avg', 'Max'].reduce((prev, curr) => {
                    const matchedHistogram = nodeData?.timeSeriesHistogram?.find(
                      ({ key }: { key: string }) => key === curr,
                    );

                    return {
                      ...prev,
                      [curr]: matchedHistogram?.values?.map?.((v) => v[1]),
                    };
                  }, {}),
                }}
                emptyMessage={emptyMessage}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

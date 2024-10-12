package si.fri.liis.Services;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    public void HandleMetric(MetricsData metricsData) {
        System.out.println(metricsData.toString());
    }
}

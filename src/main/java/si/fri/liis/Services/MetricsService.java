package si.fri.liis.Services;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.fri.liis.Converters.MetricConverter;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final RDFConnectionFuseki conn;

    @Autowired
    public MetricsService(RDFConnectionFuseki conn) {
        this.conn = conn;
    }

    public void HandleMetric(MetricsData metricsData) {

        Model model;

        try {
            MetricConverter mc = new MetricConverter(metricsData, conn);
            model = mc.getConvertedModel();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        try {
            Txn.executeWrite(conn, () -> conn.load(model));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}

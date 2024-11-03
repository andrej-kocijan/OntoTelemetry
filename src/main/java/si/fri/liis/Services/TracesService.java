package si.fri.liis.Services;

import io.opentelemetry.proto.trace.v1.TracesData;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.fri.liis.Converters.TraceConverter;

@Service
public class TracesService {

    private static final Logger logger = LoggerFactory.getLogger(TracesService.class);

    private final RDFConnectionFuseki conn;

    @Autowired
    public TracesService(RDFConnectionFuseki conn) {
        this.conn = conn;
    }

    public void HandleTrace(TracesData tracesData) {

        Model model;

        try {
            TraceConverter tc = new TraceConverter(tracesData, conn);
            model = tc.getConvertedModel();
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

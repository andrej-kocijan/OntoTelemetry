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
import si.fri.liis.Helpers.RDFConnectionFusekiFactory;

@Service
public class TracesService {

    private static final Logger logger = LoggerFactory.getLogger(TracesService.class);

    private final RDFConnectionFusekiFactory connFusekiFactory;

    @Autowired
    public TracesService(RDFConnectionFusekiFactory connFusekiFactory) {
        this.connFusekiFactory = connFusekiFactory;
    }

    public void HandleTrace(TracesData tracesData) {

        Model model;

        try (RDFConnectionFuseki conn = connFusekiFactory.createQueryConnection()) {
            TraceConverter tc = new TraceConverter(tracesData, conn);
            model = tc.getConvertedModel();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        try (RDFConnectionFuseki conn = connFusekiFactory.createLoadConnection()) {
            Txn.executeWrite(conn, () -> conn.load(model));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}

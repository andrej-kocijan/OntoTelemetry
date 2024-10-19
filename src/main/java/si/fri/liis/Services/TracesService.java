package si.fri.liis.Services;

import io.opentelemetry.proto.trace.v1.TracesData;
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

        TraceConverter tc = new TraceConverter(tracesData);
        tc.getConvertedModel().write(System.out, "TURTLE");
        System.out.println("------------------------------------------------------");
    }
}

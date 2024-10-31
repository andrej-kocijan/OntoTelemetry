package si.fri.liis.Helpers;

import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RDFConnectionFusekiFactory {

    @Value("${fuseki.server.url}")
    private String fusekiUrl;

    public RDFConnectionFuseki createQueryConnection() {
        return (RDFConnectionFuseki) RDFConnectionFuseki
                .create()
                .destination(fusekiUrl + "/query")
                .build();
    }

    public RDFConnectionFuseki createUpdateConnection() {
        return (RDFConnectionFuseki) RDFConnectionFuseki
                .create()
                .destination(fusekiUrl + "/update")
                .build();
    }

    public RDFConnectionFuseki createLoadConnection() {
        return (RDFConnectionFuseki) RDFConnectionFuseki
                .create()
                .destination(fusekiUrl + "/load")
                .build();
    }

    public RDFConnectionFuseki createGeneralConnection() {
        return (RDFConnectionFuseki) RDFConnectionFuseki
                .create()
                .updateEndpoint(fusekiUrl + "/update")
                .queryEndpoint(fusekiUrl + "/query")
                .updateEndpoint(fusekiUrl + "/update")
                .build();
    }
}

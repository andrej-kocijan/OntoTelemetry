package si.fri.liis;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import si.fri.liis.Helpers.QueryHelpers;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class FusekiServerConfig {

    @Value("${ontology.url}")
    private String ONTOLOGY_URL;

    private static final Logger logger = LoggerFactory.getLogger(FusekiServerConfig.class);

    @Value("${fuseki.server.url}")
    private String fusekiUrl;

    @Bean
    public RDFConnectionFuseki runFusekiServer() {

        FusekiServer server = FusekiServer
                .create()
                .port(3030)
                .parseConfigFile("src/main/resources/fuseki.config.ttl")
                .build();

        logger.info("Starting Fuseki server...");
        server.start();

        RDFConnectionFuseki conn = (RDFConnectionFuseki) RDFConnectionFuseki
                .create()
                .updateSendMode(UpdateSendMode.asPostForm)
                .updateEndpoint(fusekiUrl + "/update")
                .queryEndpoint(fusekiUrl + "/query")
                .gspEndpoint(fusekiUrl + "/load")
                .build();

        if (ontologyLoaded(conn))
            logger.info("Ontology already loaded...");
        else
            loadOntology(conn);

        return conn;
    }

    private boolean ontologyLoaded(RDFConnectionFuseki conn) {

        try {
            Query q = QueryHelpers.createQuery("""
                    ASK { <http://www.semanticweb.org/andrej/ontologies/2024/9/opentelemetry-ontology> rdf:type owl:Ontology . }
                    """);

            AtomicBoolean ontologyLoaded = new AtomicBoolean(false);
            Txn.executeRead(conn, () -> ontologyLoaded.set(conn.queryAsk(q)));

            return ontologyLoaded.get();

        } catch (Exception e) {
            logger.error("Error while checking for ontology: {}", e.getMessage());
        }

        return false;
    }

    private void loadOntology(RDFConnectionFuseki conn) {

        logger.info("Loading ontology...");
        try {

            Model model = ModelFactory.createDefaultModel();

            try {
                RDFDataMgr.read(model, ONTOLOGY_URL, RDFLanguages.TTL);
            } catch (Exception e) {
                logger.error("Error while reading ontology: {}", e.getMessage());
            }

            Txn.executeWrite(conn, () -> conn.load(model));
            logger.info("Ontology loaded successfully into Fuseki.");
        } catch (Exception e) {
            logger.error("Could not load ontology: {}", e.getMessage());
        }
    }
}

package si.fri.liis;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import si.fri.liis.Helpers.QueryHelpers;

@Component
public class FusekiServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(FusekiServerConfig.class);
    private static final String ONTOLOGY_URL = "https://raw.githubusercontent.com/andrej-kocijan/opentelemetry-ontology/refs/heads/main/OpenTelemetry.ttl";

    @Bean
    public CommandLineRunner runFusekiServer() {
        return args -> {

            FusekiServer server = FusekiServer
                    .create()
                    .port(3030)
                    .parseConfigFile("src/main/resources/fuseki.config.ttl")
                    .build();

            logger.info("Starting Fuseki server...");
            server.start();

            if (ontologyLoaded())
                logger.info("Ontology already loaded...");
            else
                loadOntology();
        };
    }

    private boolean ontologyLoaded() {

        RDFConnectionRemoteBuilder connBuilder = RDFConnectionFuseki
                .service("http://localhost:3030/ds/query");

        try (RDFConnection conn = connBuilder.build()) {
            Query q = QueryHelpers.createQuery("""
                    ASK { <http://www.semanticweb.org/andrej/ontologies/2024/9/opentelemetry-ontology> rdf:type owl:Ontology . }
                    """);

            return conn.queryAsk(q);

        } catch (Exception e) {
            logger.error("Error while checking for ontology: {}", e.getMessage());
        }

        return false;
    }

    private void loadOntology() {

        logger.info("Loading ontology...");
        try (RDFConnection conn = RDFConnectionRemote.service("http://localhost:3030/ds/load").build()) {

            Model model = ModelFactory.createDefaultModel();

            try {
                RDFDataMgr.read(model, ONTOLOGY_URL, RDFLanguages.TTL);
            } catch (Exception e) {
                logger.error("Error while reading ontology: {}", e.getMessage());
            }

            conn.load(model);
            logger.info("Ontology loaded successfully into Fuseki.");
        } catch (Exception e) {
            logger.error("Could not load ontology: {}", e.getMessage());
        }
    }
}

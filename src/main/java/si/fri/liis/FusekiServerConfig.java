package si.fri.liis;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FusekiServerConfig {

    @Bean
    public CommandLineRunner runFusekiServer() {
        return args -> {
            // Create a dataset (e.g., using TDB2 or in-memory for testing)
            Dataset dataset = TDB2Factory.connectDataset("data");

            // Optionally, load an OWL ontology
//            OntModel ontModel = ModelFactory.createOntologyModel();
//            ontModel.read("ontology.owl"); // Load your OWL ontology here

            // Add reasoning capabilities (optional)
//            Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//            Model infModel = ModelFactory.createInfModel(reasoner, ontModel);
//            dataset.getDefaultModel().add(infModel);

            // Start Fuseki server
            FusekiServer server = FusekiServer
                    .create()
                    .port(3030)  // Choose the port for Fuseki
                    .add("/ds", dataset)  // Dataset name and Fuseki dataset
                    .build();

            System.out.println("Starting Fuseki server...");
            server.start();
        };
    }
}

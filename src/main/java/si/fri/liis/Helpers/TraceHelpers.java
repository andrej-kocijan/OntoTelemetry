package si.fri.liis.Helpers;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TraceHelpers {

    public static void CreateMissingTraces(Set<String> traceIds, Model model, RDFConnectionFuseki conn, String ontoUri) {

        Property traceProperty = model.createProperty(ontoUri, "Trace");
        Property traceIdProperty = model.createProperty(ontoUri, "traceId");

        for(String traceId : traceIds) {

            Query q = QueryHelpers.createQuery(String.format("""
                    ASK WHERE {
                        ?t rdf:type :Trace ;
                            :traceId "%s" .
                    }
                    """, traceId));

            AtomicBoolean exists = new AtomicBoolean(false);

            Txn.executeRead(conn, () -> exists.set(conn.queryAsk(q)));

            if(!exists.get()) {
                Resource resource = model.createResource(ontoUri + "trace" + traceId + UUID.randomUUID().toString().split("-")[0]);
                resource.addProperty(RDF.type, traceProperty);
                resource.addLiteral(traceIdProperty, traceId);
            }
        }
    }
}

package si.fri.liis.Helpers;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;

public class QueryHelpers {

    private final static String ONTOLOGY_URL = "http://www.semanticweb.org/andrej/ontologies/2024/9/opentelemetry-ontology#";

    public static PrefixMapping getPrefixes() {

        return PrefixMapping.Factory.create()
                .setNsPrefix("", ONTOLOGY_URL)
                .setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                .setNsPrefix("owl", "http://www.w3.org/2002/07/owl#")
                .setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#")
                .setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    }

    public static Query createQuery(String query) {

        StringBuilder sb = new StringBuilder();
        PrefixMapping prefixes = getPrefixes();

        for(String prefix: prefixes.getNsPrefixMap().keySet()) {
            sb.append("PREFIX ");
            sb.append(prefix);
            sb.append(": <");
            sb.append(prefixes.getNsPrefixURI(prefix));
            sb.append(">\n");
        }

        sb.append(query);

        return QueryFactory.create(sb.toString());
    }
}

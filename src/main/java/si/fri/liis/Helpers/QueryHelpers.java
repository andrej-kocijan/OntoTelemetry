package si.fri.liis.Helpers;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.shared.PrefixMapping;

import java.util.List;

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

        for (String prefix : prefixes.getNsPrefixMap().keySet()) {
            sb.append("PREFIX ");
            sb.append(prefix);
            sb.append(": <");
            sb.append(prefixes.getNsPrefixURI(prefix));
            sb.append(">\n");
        }

        sb.append(query);

        return QueryFactory.create(sb.toString());
    }

    public static String createUpdate(String query) {

        StringBuilder sb = new StringBuilder();
        PrefixMapping prefixes = getPrefixes();

        for (String prefix : prefixes.getNsPrefixMap().keySet()) {
            sb.append("PREFIX ");
            sb.append(prefix);
            sb.append(": <");
            sb.append(prefixes.getNsPrefixURI(prefix));
            sb.append(">\n");
        }

        sb.append(query);

        return sb.toString();
    }

    public static void mergeDuplicates(List<Resource> resources, RDFConnectionFuseki conn) {

        if (resources.size() <= 1)
            return;

        String mainResource = resources.get(0).getLocalName();
        for (int i = 1; i < resources.size(); i++) {

            String toBeRemoved = resources.get(i).getLocalName();

            String q2 = QueryHelpers.createUpdate(String.format("""
                    DELETE { ?s ?p :%s }
                    INSERT { ?s ?p :%s }
                    WHERE {
                        ?s ?p :%s .
                        FILTER (?s != :%s)
                    }
                    """, toBeRemoved, mainResource, toBeRemoved, mainResource));

            String q3 = QueryHelpers.createUpdate(String.format("""
                    DELETE WHERE { :%s ?p ?o }
                    """, toBeRemoved));

            conn.update(q2);
            conn.update(q3);
        }

    }
}

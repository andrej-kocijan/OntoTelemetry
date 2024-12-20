package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Helpers.QueryHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class InstrumentationScopeConverter extends CommonConverter<InstrumentationScope>{

    public static HashMap<String, Resource> existingResources = new HashMap<>();

    public InstrumentationScopeConverter(Model model, InstrumentationScope source, RDFConnectionFuseki conn) {
        super(model, source, conn);
    }

    @Override
    public void convertToModel() {

       Resource existing = checkForExisting(source.getName(), source.getVersion());

       if(existing != null) {
           this.resource = existing;
           return;
       }

       this.resource = this.model.createResource(ontoUri + "InstrumentationScope" + UUID.randomUUID());

        if(!existingResources.containsKey(source.getName() + "@" + source.getVersion()))
            existingResources.put(source.getName() + "@" + source.getVersion(), resource);

       Property instrumentationScopeProperty = model.createProperty(ontoUri, "InstrumentationScope");
       resource.addProperty(RDF.type, instrumentationScopeProperty);

       Property droppedAttributesCount = model.createProperty(ontoUri, "droppedAttributesCount");
       resource.addLiteral(droppedAttributesCount, source.getDroppedAttributesCount());

       Property nameProperty = model.createProperty(ontoUri, "name");
       resource.addLiteral(nameProperty, source.getName());

       Property versionProperty = model.createProperty(ontoUri, "version");
       resource.addLiteral(versionProperty, source.getVersion());

       Property attributeProperty = this.model.createProperty(ontoUri, "attribute");
       for(KeyValue attribute : source.getAttributesList()) {
           Resource attributeResource = (new KeyValueConverter(model, attribute)).getConvertedResource();
           resource.addProperty(attributeProperty, attributeResource);
       }
    }

    private Resource checkForExisting(String name, String version) {

        if(existingResources.containsKey(name + "@" + version))
            return existingResources.get(name + "@" + version);

        AtomicReference<Resource> resource = new AtomicReference<>(null);

        Query q = QueryHelpers.createQuery(String.format("""
                SELECT ?scope
                WHERE {
                    ?scope a :InstrumentationScope ;
                        :name "%s" ;
                        :version "%s" .
                }
                ORDER BY ?scope
                LIMIT 1
                """, name, version));

        try {
            Txn.executeWrite(conn, () -> {

                List<Resource> resources = new ArrayList<>();

                conn.querySelect(q, (result) -> {
                    Resource r = result.getResource("scope");
                    resources.add(r);
                });

                if(!resources.isEmpty()) {
                    resource.set(resources.get(0));
                    existingResources.put(name + "@" + version, resources.get(0));
                }

                // Due to high concurrency, multiple resources could be created, merge them
//                QueryHelpers.mergeDuplicates(resources, conn);

            });
        } catch (Exception e) {
            System.err.println("Error while querying for existing scope: " + e.getMessage());
        }

        return resource.get();
    }
}

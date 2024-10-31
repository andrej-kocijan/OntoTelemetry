package si.fri.liis.Converters.Common;

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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ResourceConverter extends CommonConverter<io.opentelemetry.proto.resource.v1.Resource> {

    public ResourceConverter(Model model, io.opentelemetry.proto.resource.v1.Resource resource, RDFConnectionFuseki conn) {
        super(model, resource, conn);
    }

    @Override
    protected void convertToModel() {

        String serviceName = this.source
                .getAttributesList()
                .stream()
                .filter(a -> a.getKey().equals("service.name") && a.getValue().hasStringValue())
                .map(a -> a.getValue().getStringValue())
                .findFirst()
                .orElse( "");

        Resource existing = checkForExisting(serviceName);

        if (existing != null) {
            this.resource = existing;
            return;
        }

        this.resource = this.model.createResource(ontoUri + "resource" + UUID.randomUUID());

        Property resourceProperty = this.model.createProperty(ontoUri, "Resource");
        Property attributeProperty = this.model.createProperty(ontoUri, "attribute");
        Property droppedAttributesCountProperty = this.model.createProperty(ontoUri, "droppedAttributesCount");
        Property serviceNameProperty = this.model.createProperty(ontoUri, "serviceName");

        resource.addProperty(RDF.type, resourceProperty);
        resource.addLiteral(droppedAttributesCountProperty, this.source.getDroppedAttributesCount());

        resource.addLiteral(serviceNameProperty, serviceName);

        for (KeyValue attribute : this.source.getAttributesList())
        {
            if(attribute.getKey().equals("service.name") && attribute.getValue().hasStringValue())
                continue;

            Resource attributeResource = (new KeyValueConverter(model, attribute)).getConvertedResource();
            resource.addProperty(attributeProperty, attributeResource);
        }
    }

    private Resource checkForExisting(String serviceName) {

        if(serviceName.isEmpty())
            return null;

        AtomicReference<Resource> atomicResource = new AtomicReference<>(null);

        Query q = QueryHelpers.createQuery(String.format("""
                SELECT ?resource
                WHERE {
                    ?resource a :Resource ;
                        :serviceName "%s" .
                }
                ORDER BY ?resource
                """, serviceName));

        try {
            Txn.executeWrite(conn, () -> {

                List<Resource> resources = new ArrayList<>();

                conn.querySelect(q, (result) -> {
                    Resource r = result.getResource("resource");
                    resources.add(r);
                });

                if(!resources.isEmpty())
                    atomicResource.set(resources.get(0));

                // Due to high concurrency, multiple resources could be created, merge them
                QueryHelpers.mergeDuplicates(resources, conn);

            });
        } catch (Exception e) {
            System.err.println("Error while querying for existing resource: " + e.getMessage());
        }

        return atomicResource.get();
    }
}

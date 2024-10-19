package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.UUID;

public class ResourceConverter extends CommonConverter<io.opentelemetry.proto.resource.v1.Resource> {

    public ResourceConverter(Model model, io.opentelemetry.proto.resource.v1.Resource resource) {
        super(model, resource);
    }

    @Override
    protected void convertToModel() {

        this.resource = this.model.createResource(ontoUri + "resource" + UUID.randomUUID());

        Property resourceProperty = this.model.createProperty(ontoUri + "Resource");
        Property attributeProperty = this.model.createProperty(ontoUri + "attribute");
        Property droppedAttributesCountProperty = this.model.createProperty(ontoUri + "droppedAttributesCount");
        Property serviceNameProperty = this.model.createProperty(ontoUri + "serviceName");

        resource.addProperty(RDF.type, resourceProperty);
        resource.addLiteral(droppedAttributesCountProperty, this.source.getDroppedAttributesCount());

        String serviceName = this.source
                .getAttributesList()
                .stream()
                .filter(a -> a.getKey().equals("service.name") && a.getValue().hasStringValue())
                .map(a -> a.getValue().getStringValue())
                .findFirst()
                .orElse( "noServiceName");
        resource.addLiteral(serviceNameProperty, serviceName);

        for (KeyValue attribute : this.source.getAttributesList())
        {
            if(attribute.getKey().equals("service.name") && attribute.getValue().hasStringValue())
                continue;

            Resource attributeResource = (new KeyValueConverter(model, attribute, "resourceAttribute")).getConvertedResource();
            resource.addLiteral(attributeProperty, attributeResource);
        }
    }
}

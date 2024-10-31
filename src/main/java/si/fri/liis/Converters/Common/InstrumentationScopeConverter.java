package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.vocabulary.RDF;

import java.util.UUID;

public class InstrumentationScopeConverter extends CommonConverter<InstrumentationScope>{

    public InstrumentationScopeConverter(Model model, InstrumentationScope source, RDFConnectionFuseki conn) {
        super(model, source, conn);
    }

    @Override
    public void convertToModel() {

       this.resource = this.model.createResource(ontoUri + "InstrumentationScope" + UUID.randomUUID());
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
}

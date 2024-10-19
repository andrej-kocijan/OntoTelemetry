package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.UUID;

public class KeyValueConverter extends CommonConverter<KeyValue> {

    public KeyValueConverter(Model model, KeyValue source) {
        super(model, source);
    }

    @Override
    public void convertToModel() {

        model.createProperty(ontoUri + "KeyValue");
        model.createProperty(ontoUri + "key");
        model.createProperty(ontoUri + "value");

        this.resource = KeyValueHandler(this.source);
    }

    private Resource KeyValueHandler(KeyValue kv) {

        Property keyValueProperty = model.getProperty(ontoUri + "KeyValue");
        Property keyProperty = model.getProperty(ontoUri + "key");
        Property valueProperty = model.getProperty(ontoUri + "value");

        Resource resource = this.model.createResource(ontoUri + "keyValue" + UUID.randomUUID());
        resource.addProperty(RDF.type, keyValueProperty);

        resource.addLiteral(keyProperty, kv.getKey());

        AnyValue value = kv.getValue();

        if (value.hasStringValue() || value.hasDoubleValue() || value.hasBoolValue() || value.hasBytesValue() || value.hasIntValue())
            primitiveValueHandler(value, resource);

        if (value.hasArrayValue())
            for (AnyValue v : value.getArrayValue().getValuesList())
                primitiveValueHandler(v, resource);

        if (value.hasKvlistValue())
            for(KeyValue kvv : value.getKvlistValue().getValuesList()) {
                Resource kvResource = KeyValueHandler(kvv);
                resource.addProperty(valueProperty, kvResource);
            }

        return resource;
    }

    public void primitiveValueHandler(AnyValue value, Resource resource) {

        Property valueProperty = model.getProperty(ontoUri + "value");

        if (value.hasStringValue())
            resource.addLiteral(valueProperty, value.getStringValue());
        if (value.hasDoubleValue())
            resource.addLiteral(valueProperty, value.getDoubleValue());
        if (value.hasBoolValue())
            resource.addLiteral(valueProperty, value.getBoolValue());
        if (value.hasBytesValue())
            resource.addLiteral(valueProperty, value.getBytesValue());
        if (value.hasIntValue())
            resource.addLiteral(valueProperty, value.getIntValue());
    }

}

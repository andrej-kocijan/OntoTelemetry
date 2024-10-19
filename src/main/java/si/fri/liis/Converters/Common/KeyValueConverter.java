package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import org.apache.jena.rdf.model.Model;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.UUID;

public class KeyValueConverter extends CommonConverter<KeyValue> {

    private final String Iri;

    public KeyValueConverter(Model model, KeyValue source) {
        super(model, source);
        this.Iri = "keyValue";
    }

    public KeyValueConverter(Model model, KeyValue source, String iri) {
        super(model, source);
        this.Iri = iri;
    }

    @Override
    public void convertToModel() {

        Property keyValueProperty = model.createProperty(ontoUri + "KeyValue");
        Property keyProperty = model.createProperty(ontoUri + "key");
        Property valueProperty = model.createProperty(ontoUri + "value");

        this.resource = KeyValueHandler(this.source, keyProperty, valueProperty, keyValueProperty);
    }

    private Resource KeyValueHandler(KeyValue kv, Property keyProperty, Property valueProperty, Property keyValueProperty) {

        Resource resource = this.model.createResource(ontoUri + Iri + UUID.randomUUID());
        resource.addProperty(RDF.type, keyValueProperty);

        resource.addLiteral(keyProperty, kv.getKey());

        AnyValue value = kv.getValue();

        if (value.hasStringValue() || value.hasDoubleValue() || value.hasBoolValue() || value.hasBytesValue() || value.hasIntValue())
            primitiveValueHandler(value, resource, valueProperty);

        if (value.hasArrayValue())
            for (AnyValue v : value.getArrayValue().getValuesList())
                primitiveValueHandler(v, resource, valueProperty);

        if (value.hasKvlistValue())
            for(KeyValue kvv : value.getKvlistValue().getValuesList()) {
                Resource kvResource = KeyValueHandler(kvv, keyProperty, valueProperty, keyValueProperty);
                resource.addProperty(valueProperty, kvResource);
            }

        return resource;
    }

    public void primitiveValueHandler(AnyValue value, Resource resource, Property valueProperty) {

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

package si.fri.liis.Converters.Common;

import io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.jena.rdf.model.Model;

public class AttributeConverter extends CommonConverter<KeyValue> {

    public AttributeConverter(Model model, KeyValue source) {
        super(model, source);
    }
}

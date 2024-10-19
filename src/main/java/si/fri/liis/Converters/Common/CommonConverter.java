package si.fri.liis.Converters.Common;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.UUID;

public abstract class CommonConverter<T> implements ICommonConverter {

    protected Resource resource;
    protected final Model model;
    protected final String ontoUri;
    protected final T source;

    public CommonConverter(Model model, T source) {
        this.model = model;
        this.ontoUri = this.model.getNsPrefixURI("");
        this.source = source;
        convertToModel();
    }

    @Override
    public Resource getConvertedResource() {
        return this.resource;
    }

    protected void convertToModel() {
        this.resource = this.model.createResource(ontoUri + UUID.randomUUID());
    }
}

package si.fri.liis.Converters.Common;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;

import java.util.UUID;

public abstract class CommonConverter<T> implements ICommonConverter {

    protected Resource resource;
    protected final Model model;
    protected final String ontoUri;
    protected final T source;
    protected final RDFConnectionFuseki conn;

    public CommonConverter(Model model, T source, RDFConnectionFuseki conn) {
        this.model = model;
        this.ontoUri = this.model.getNsPrefixURI("");
        this.source = source;
        this.conn = conn;
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

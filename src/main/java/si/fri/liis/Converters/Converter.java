package si.fri.liis.Converters;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import si.fri.liis.Helpers.QueryHelpers;

public abstract class Converter<T> implements IConverter {

    protected final Model model;
    protected final String ontoUri;
    protected final T source;

    public Converter(T source) {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(QueryHelpers.getPrefixes());
        this.source = source;
        this.ontoUri = this.model.getNsPrefixURI("");
        convertToModel();
    }

    @Override
    public Model getConvertedModel() {
        return this.model;
    }

    protected void convertToModel() {

    }
}

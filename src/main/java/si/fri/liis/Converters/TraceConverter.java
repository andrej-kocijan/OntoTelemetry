package si.fri.liis.Converters;

import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.TracesData;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Converters.Common.ResourceConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TraceConverter extends Converter<TracesData> {

    public TraceConverter(TracesData tracesData) {
        super(tracesData);
    }

    @Override
    protected void convertToModel() {

        Resource td = model.createResource(ontoUri + "tracesData" + UUID.randomUUID());

        Property tracesDataProperty = model.createProperty(ontoUri + "TracesData");
        td.addProperty(RDF.type, tracesDataProperty);

        Property resourceSpanProperty = model.createProperty(ontoUri + "resourceSpan");

        ArrayList<Resource> resourceSpansResources = convertResourceSpans();

        for(Resource r : resourceSpansResources)
            td.addProperty(resourceSpanProperty, r);

    }

    private ArrayList<Resource> convertResourceSpans() {

        List<ResourceSpans> resourceSpansList = source.getResourceSpansList();
        ArrayList<Resource> resources = new ArrayList<>();

        Property resourceSpanProperty = model.createProperty(ontoUri + "ResourceSpans");
        Property schemaUrlProperty = model.createProperty(ontoUri + "schemaUrl");
        Property resourceProperty = model.createProperty(ontoUri + "resource");
        Property scopeSpanProperty = model.createProperty(ontoUri + "scopeSpan");

        for(ResourceSpans resourceSpans : resourceSpansList) {

            Resource resourceSpansResource = model.createResource(ontoUri + "resourceSpans" + UUID.randomUUID());
            resourceSpansResource.addProperty(RDF.type, resourceSpanProperty);
            resourceSpansResource.addProperty(schemaUrlProperty, resourceSpans.getSchemaUrl());

            Resource resourceResource = (new ResourceConverter(model, resourceSpans.getResource())).getConvertedResource();
            resourceSpansResource.addProperty(resourceProperty, resourceResource);

            resources.add(resourceSpansResource);
        }

        return resources;
    }
}

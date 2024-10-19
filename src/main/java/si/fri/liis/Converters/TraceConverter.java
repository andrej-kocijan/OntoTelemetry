package si.fri.liis.Converters;

import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.TracesData;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Converters.Common.InstrumentationScopeConverter;
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

        Property tracesDataProperty = model.createProperty(ontoUri, "TracesData");
        td.addProperty(RDF.type, tracesDataProperty);

        Property resourceSpanProperty = model.createProperty(ontoUri, "resourceSpan");

        ArrayList<Resource> resourceSpansResources = convertResourceSpans();

        for(Resource r : resourceSpansResources)
            td.addProperty(resourceSpanProperty, r);

    }

    private ArrayList<Resource> convertResourceSpans() {

        List<ResourceSpans> resourceSpansList = source.getResourceSpansList();
        ArrayList<Resource> resources = new ArrayList<>();

        Property resourceSpanProperty = model.createProperty(ontoUri, "ResourceSpans");
        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        Property resourceProperty = model.createProperty(ontoUri, "resource");
        Property scopeSpanProperty = model.createProperty(ontoUri, "scopeSpan");

        for(ResourceSpans resourceSpans : resourceSpansList) {

            Resource resourceSpansResource = model.createResource(ontoUri + "resourceSpans" + UUID.randomUUID());
            resourceSpansResource.addProperty(RDF.type, resourceSpanProperty);
            resourceSpansResource.addProperty(schemaUrlProperty, resourceSpans.getSchemaUrl());

            Resource resourceResource = (new ResourceConverter(model, resourceSpans.getResource())).getConvertedResource();
            resourceSpansResource.addProperty(resourceProperty, resourceResource);

            for(ScopeSpans scopeSpans : resourceSpans.getScopeSpansList()) {
                Resource scopeSpanResource = convertScopeSpans(scopeSpans);
                resourceSpansResource.addProperty(scopeSpanProperty, scopeSpanResource);
            }

            resources.add(resourceSpansResource);
        }

        return resources;
    }

    private Resource convertScopeSpans(ScopeSpans scopeSpans) {

        Resource resource = model.createResource(ontoUri + "scopeSpans" + UUID.randomUUID());
        Property scopeSpansProperty = model.createProperty(ontoUri, "ScopeSpans");
        resource.addProperty(RDF.type, scopeSpansProperty);

        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        resource.addProperty(schemaUrlProperty, scopeSpans.getSchemaUrl());

        Property scopeProperty = model.createProperty(ontoUri, "scope");
        Resource instrumentationScopeResource = (new InstrumentationScopeConverter(model, scopeSpans.getScope())).getConvertedResource();
        resource.addProperty(scopeProperty, instrumentationScopeResource);

        Property spanProperty = model.createProperty(ontoUri, "span");


        return resource;
    }
}

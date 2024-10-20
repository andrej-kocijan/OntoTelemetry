package si.fri.liis.Converters;

import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
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


            List<Resource> scopeSpanResources = convertScopeSpans(resourceSpans.getScopeSpansList());

            for(Resource ssr : scopeSpanResources)
                resourceSpansResource.addProperty(scopeSpanProperty, ssr);

            resources.add(resourceSpansResource);
        }

        return resources;
    }

    private List<Resource> convertScopeSpans(List<ScopeSpans> scopeSpans) {

        ArrayList<Resource> resources = new ArrayList<>();

        Property scopeSpansProperty = model.createProperty(ontoUri, "ScopeSpans");
        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        Property scopeProperty = model.createProperty(ontoUri, "scope");
        Property spanProperty = model.createProperty(ontoUri, "span");

        for(ScopeSpans ss : scopeSpans) {

            Resource resource = model.createResource(ontoUri + "scopeSpans" + UUID.randomUUID());
            resource.addProperty(RDF.type, scopeSpansProperty);

            resource.addProperty(schemaUrlProperty, ss.getSchemaUrl());

            Resource instrumentationScopeResource = (new InstrumentationScopeConverter(model, ss.getScope())).getConvertedResource();
            resource.addProperty(scopeProperty, instrumentationScopeResource);

            List<Resource> spanResources = convertSpans(ss.getSpansList());
            for(Resource sr : spanResources)
                resource.addProperty(spanProperty, sr);

            resources.add(resource);
        }

        return resources;
    }

    private List<Resource> convertSpans(List<Span> spans) {

        ArrayList<Resource> resources = new ArrayList<>();



        return resources;
    }
}

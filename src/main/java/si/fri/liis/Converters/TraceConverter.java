package si.fri.liis.Converters;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Converters.Common.InstrumentationScopeConverter;
import si.fri.liis.Converters.Common.KeyValueConverter;
import si.fri.liis.Converters.Common.ResourceConverter;

import java.util.*;

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
        Set<String> traceIds = new HashSet<String>();

        Property spanProperty = model.createProperty(ontoUri, "Span");
        Property traceIdProperty = model.createProperty(ontoUri, "traceId");
        Property spanIdProperty = model.createProperty(ontoUri, "spanId");
        Property traceStateProperty = model.createProperty(ontoUri, "traceState");
        Property parentSpanIdProperty = model.createProperty(ontoUri, "parentSpanId");
        Property flagsProperty = model.createProperty(ontoUri, "flags");
        Property nameProperty = model.createProperty(ontoUri, "name");
        Property kindProperty = model.createProperty(ontoUri, "kind");
        Property startTimeUnixNanoProperty = model.createProperty(ontoUri, "startTimeUnixNano");
        Property endTimeUnixNanoProperty = model.createProperty(ontoUri, "endTimeUnixNano");
        Property attributeProperty = model.createProperty(ontoUri, "attribute");
        Property droppedAttributesCountProperty = model.createProperty(ontoUri, "droppedAttributesCount");
        Property eventProperty = model.createProperty(ontoUri, "event");
        Property droppedEventsCountProperty = model.createProperty(ontoUri, "droppedEventsCount");
        Property linkProperty = model.createProperty(ontoUri, "link");
        Property droppedLinksCountProperty = model.createProperty(ontoUri, "droppedLinksCount");
        Property statusProperty = model.createProperty(ontoUri, "status");

        for(Span span : spans) {

            String tracedId = HexFormat.of().formatHex(span.getTraceId().toByteArray());
            String spanId = HexFormat.of().formatHex(span.getSpanId().toByteArray());

            Resource resource = model.createResource(ontoUri + "span" + tracedId + "-" + spanId + "-" + UUID.randomUUID().toString().split("-")[0]);
            resource.addProperty(RDF.type, spanProperty);

            resource.addLiteral(traceIdProperty, tracedId);
            traceIds.add(tracedId);

            resource.addLiteral(spanIdProperty, spanId);
            resource.addLiteral(traceStateProperty, span.getTraceState());
            resource.addLiteral(parentSpanIdProperty, HexFormat.of().formatHex(span.getParentSpanId().toByteArray()));
            resource.addLiteral(flagsProperty, span.getFlags());
            resource.addLiteral(nameProperty, span.getName());

            switch (span.getKind()) {
                case SPAN_KIND_INTERNAL -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_INTERNAL"));
                case SPAN_KIND_SERVER -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_SERVER"));
                case SPAN_KIND_CLIENT -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_CLIENT"));
                case SPAN_KIND_PRODUCER -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_PRODUCER"));
                case SPAN_KIND_CONSUMER -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_CONSUMER"));
                default -> resource.addProperty(kindProperty, model.createResource(ontoUri + "SPAN_KIND_UNSPECIFIED"));
            }

            resource.addLiteral(startTimeUnixNanoProperty, span.getStartTimeUnixNano());
            resource.addLiteral(endTimeUnixNanoProperty, span.getEndTimeUnixNano());

            for(KeyValue attribute : span.getAttributesList()) {
                Resource attributeResource = (new KeyValueConverter(model, attribute)).getConvertedResource();
                resource.addProperty(attributeProperty, attributeResource);
            }
            resource.addLiteral(droppedAttributesCountProperty, span.getDroppedAttributesCount());

            // handle Events
            resource.addLiteral(droppedEventsCountProperty, span.getDroppedEventsCount());

            //handle Links
            resource.addLiteral(droppedLinksCountProperty, span.getDroppedLinksCount());

            //handle Status
            Resource statusResource = statusConverter(span.getStatus());
            resource.addProperty(statusProperty, statusResource);
        }


        // create :Trace-s if needed

        return resources;
    }

    public Resource statusConverter(Status status) {

        Property statusProperty = model.createProperty(ontoUri, "Status");
        Property messageProperty = model.createProperty(ontoUri, "message");
        Property codeProperty = model.createProperty(ontoUri, "code");

        Resource resource = model.createResource(ontoUri + "Status" + UUID.randomUUID());
        resource.addProperty(RDF.type, statusProperty);
        resource.addLiteral(messageProperty, status.getMessage());

        switch (status.getCode()) {
            case STATUS_CODE_OK -> resource.addProperty(codeProperty, model.createResource(ontoUri + "STATUS_CODE_OK"));
            case STATUS_CODE_ERROR -> resource.addProperty(codeProperty, model.createResource(ontoUri + "STATUS_CODE_ERROR"));
            default -> resource.addProperty(codeProperty, model.createResource(ontoUri + "STATUS_CODE_UNSET"));
        }

        return resource;
    }
}

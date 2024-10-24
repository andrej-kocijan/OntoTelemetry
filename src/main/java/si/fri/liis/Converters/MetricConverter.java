package si.fri.liis.Converters;

import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Converters.Common.InstrumentationScopeConverter;
import si.fri.liis.Converters.Common.ResourceConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetricConverter extends Converter<MetricsData> {


    public MetricConverter(MetricsData source, RDFConnectionFuseki conn) {
        super(source, conn);
    }

    @Override
    protected void convertToModel() {

        Resource resource = model.createResource(ontoUri + "metricsData" + UUID.randomUUID());

        Property metricsDataProperty = model.createProperty(ontoUri, "MetricsData");
        resource.addProperty(RDF.type, metricsDataProperty);

        Property resourceMetricProperty = model.createProperty(ontoUri, "resourceMetric");

        List<Resource> resourceMetrics = convertResourceMetrics();
        for(Resource resourceMetric: resourceMetrics)
            resource.addProperty(resourceMetricProperty, resourceMetric);
    }

    private ArrayList<Resource> convertResourceMetrics() {

        List<ResourceMetrics> resourceMetricsList = source.getResourceMetricsList();
        ArrayList<Resource> resources = new ArrayList<>();

        Property resourceMetricsProperty = model.createProperty(ontoUri, "ResourceMetrics");
        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        Property resourceProperty = model.createProperty(ontoUri, "resource");
        Property scopeMetricProperty = model.createProperty(ontoUri, "scopeMetric");

        for(ResourceMetrics resourceMetrics : resourceMetricsList) {

            Resource resourceMetricResource = model.createResource(ontoUri + "resourceMetric" + UUID.randomUUID());
            resourceMetricResource.addProperty(RDF.type, resourceMetricsProperty);
            resourceMetricResource.addProperty(schemaUrlProperty, resourceMetrics.getSchemaUrl());

            Resource resourceResource = (new ResourceConverter(model, resourceMetrics.getResource())).getConvertedResource();
            resourceMetricResource.addProperty(resourceProperty, resourceResource);

            List<Resource> scopeMetricsResources = convertScopeMetrics(resourceMetrics.getScopeMetricsList());

            for(Resource smr : scopeMetricsResources)
                resourceMetricResource.addProperty(scopeMetricProperty, smr);

            resources.add(resourceMetricResource);
        }

        return resources;
    }

    private List<Resource> convertScopeMetrics(List<ScopeMetrics> scopeMetrics) {

        ArrayList<Resource> resources = new ArrayList<>();

        Property scopeMetricsProperty = model.createProperty(ontoUri, "ScopeMetrics");
        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        Property scopeProperty = model.createProperty(ontoUri, "scope");
        Property metricProperty = model.createProperty(ontoUri, "metric");

        for(ScopeMetrics sm : scopeMetrics) {

            Resource resource = model.createResource(ontoUri + "scopeMetrics" + UUID.randomUUID());
            resource.addProperty(RDF.type, scopeMetricsProperty);

            resource.addProperty(schemaUrlProperty, sm.getSchemaUrl());

            Resource instrumentationScopeResource = (new InstrumentationScopeConverter(model, sm.getScope())).getConvertedResource();
            resource.addProperty(scopeProperty, instrumentationScopeResource);

            List<Resource> spanResources = convertMetrics(sm.getMetricsList());
            for(Resource sr : spanResources)
                resource.addProperty(metricProperty, sr);

            resources.add(resource);
        }

        return resources;
    }

    private List<Resource> convertMetrics(List<Metric> metrics) {
        ArrayList<Resource> resources = new ArrayList<>();

        return resources;
    }

}

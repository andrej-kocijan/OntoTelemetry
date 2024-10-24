package si.fri.liis.Converters;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.vocabulary.RDF;
import si.fri.liis.Converters.Common.InstrumentationScopeConverter;
import si.fri.liis.Converters.Common.KeyValueConverter;
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
        for (Resource resourceMetric : resourceMetrics)
            resource.addProperty(resourceMetricProperty, resourceMetric);
    }

    private ArrayList<Resource> convertResourceMetrics() {

        List<ResourceMetrics> resourceMetricsList = source.getResourceMetricsList();
        ArrayList<Resource> resources = new ArrayList<>();

        Property resourceMetricsProperty = model.createProperty(ontoUri, "ResourceMetrics");
        Property schemaUrlProperty = model.createProperty(ontoUri, "schemaUrl");
        Property resourceProperty = model.createProperty(ontoUri, "resource");
        Property scopeMetricProperty = model.createProperty(ontoUri, "scopeMetric");

        for (ResourceMetrics resourceMetrics : resourceMetricsList) {

            Resource resourceMetricResource = model.createResource(ontoUri + "resourceMetric" + UUID.randomUUID());
            resourceMetricResource.addProperty(RDF.type, resourceMetricsProperty);
            resourceMetricResource.addProperty(schemaUrlProperty, resourceMetrics.getSchemaUrl());

            Resource resourceResource = (new ResourceConverter(model, resourceMetrics.getResource())).getConvertedResource();
            resourceMetricResource.addProperty(resourceProperty, resourceResource);

            List<Resource> scopeMetricsResources = convertScopeMetrics(resourceMetrics.getScopeMetricsList());

            for (Resource smr : scopeMetricsResources)
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

        for (ScopeMetrics sm : scopeMetrics) {

            Resource resource = model.createResource(ontoUri + "scopeMetrics" + UUID.randomUUID());
            resource.addProperty(RDF.type, scopeMetricsProperty);

            resource.addProperty(schemaUrlProperty, sm.getSchemaUrl());

            Resource instrumentationScopeResource = (new InstrumentationScopeConverter(model, sm.getScope())).getConvertedResource();
            resource.addProperty(scopeProperty, instrumentationScopeResource);

            List<Resource> spanResources = convertMetrics(sm.getMetricsList());
            for (Resource sr : spanResources)
                resource.addProperty(metricProperty, sr);

            resources.add(resource);
        }

        return resources;
    }

    private List<Resource> convertMetrics(List<Metric> metrics) {
        ArrayList<Resource> resources = new ArrayList<>();

        Property metricProperty = model.createProperty(ontoUri, "Metric");
        Property nameProperty = model.createProperty(ontoUri, "name");
        Property descriptionProperty = model.createProperty(ontoUri, "description");
        Property unitProperty = model.createProperty(ontoUri, "unit");
        Property dataProperty = model.createProperty(ontoUri, "data");
        Property metadataProperty = model.createProperty(ontoUri, "metadata");

        for (Metric metric : metrics) {

            Resource resource = model.createResource(ontoUri + "metric" + UUID.randomUUID());
            resource.addProperty(RDF.type, metricProperty);

            resource.addLiteral(nameProperty, metric.getName());
            resource.addLiteral(descriptionProperty, metric.getDescription());
            resource.addLiteral(unitProperty, metric.getUnit());

            Resource dataResource = convertMetricData(metric);
            resource.addProperty(dataProperty, dataResource);

            for (KeyValue metadata : metric.getMetadataList()) {
                Resource metadataResource = (new KeyValueConverter(model, metadata)).getConvertedResource();
                resource.addProperty(metadataProperty, metadataResource);
            }
        }

        return resources;
    }

    private Resource convertMetricData(Metric metric) {
        Resource resource = model.createResource(ontoUri + "metricData" + UUID.randomUUID());

        Property typeProperty;
        Property dataPointProperty  = model.createProperty(ontoUri, "dataPoint");
        Property aggregationTemporalityProperty  = model.createProperty(ontoUri, "aggregationTemporality");

        List<Resource> dataPoints = new ArrayList<>();
        Resource aggregationTemporalityResource = null;

        if (metric.hasGauge()) {
            typeProperty = model.createProperty(ontoUri, "Gauge");

        } else if (metric.hasSum()) {
            typeProperty = model.createProperty(ontoUri, "Sum");
            Property isMonotonicProperty = model.createProperty(ontoUri, "isMonotonic");
            resource.addLiteral(isMonotonicProperty, metric.getSum().getIsMonotonic());
            aggregationTemporalityResource = convertAggregationTemporality(metric.getSum().getAggregationTemporality());

        } else if (metric.hasHistogram()) {
            typeProperty = model.createProperty(ontoUri, "Histogram");
            aggregationTemporalityResource = convertAggregationTemporality(metric.getHistogram().getAggregationTemporality());

        } else if (metric.hasExponentialHistogram()) {
            typeProperty = model.createProperty(ontoUri, "ExponentialHistogram");
            aggregationTemporalityResource = convertAggregationTemporality(metric.getExponentialHistogram().getAggregationTemporality());

        } else if (metric.hasSummary()) {
            typeProperty = model.createProperty(ontoUri, "Summary");

        } else {
            typeProperty = model.createProperty(ontoUri, "MetricData");
            dataPoints = new ArrayList<>();
        }

        resource.addProperty(RDF.type, typeProperty);

        for(Resource dp : dataPoints)
            resource.addProperty(dataPointProperty, dp);

        if(aggregationTemporalityResource != null)
            resource.addProperty(aggregationTemporalityProperty, aggregationTemporalityResource);

        return resource;
    }

    private Resource convertAggregationTemporality(AggregationTemporality aggregationTemporality) {

        return switch (aggregationTemporality) {
            case AGGREGATION_TEMPORALITY_UNSPECIFIED -> model.createResource(ontoUri + ":AGGREGATION_TEMPORALITY_UNSPECIFIED");
            case AGGREGATION_TEMPORALITY_DELTA -> model.createResource(ontoUri + ":AGGREGATION_TEMPORALITY_DELTA");
            case AGGREGATION_TEMPORALITY_CUMULATIVE -> model.createResource(ontoUri + ":AGGREGATION_TEMPORALITY_CUMULATIVE");
            default -> null;
        };
    }

}

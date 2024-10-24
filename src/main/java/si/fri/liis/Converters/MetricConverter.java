package si.fri.liis.Converters;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.vocabulary.RDF;

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
    }
}

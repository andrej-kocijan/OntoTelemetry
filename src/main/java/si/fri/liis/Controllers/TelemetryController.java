package si.fri.liis.Controllers;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.proto.trace.v1.TracesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import si.fri.liis.Services.MetricsService;
import si.fri.liis.Services.TracesService;

@RestController
@DependsOn("runFusekiServer")
public class TelemetryController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryController.class);
    private final TracesService tracesService;
    private final MetricsService metricsService;

    @Autowired
    public TelemetryController(TracesService tracesService, MetricsService metricsService) {
        this.tracesService = tracesService;
        this.metricsService = metricsService;
    }

    @PostMapping("/telemetry/v1/traces")
    public ResponseEntity<byte[]> trace(@RequestBody byte[] body) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/x-protobuf");

        try {
            TracesData td = TracesData.parseFrom(body);

            tracesService.HandleTrace(td);

            ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder().build();
            return new ResponseEntity<>(response.toByteArray(), responseHeaders, HttpStatus.OK);

        } catch (InvalidProtocolBufferException e) {
            Status status = Status.newBuilder().setCodeValue(400).setMessage("Could not parse TracesData from the received body").build();
            return new ResponseEntity<>(status.toByteArray(), responseHeaders, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Status status = Status.newBuilder().setCodeValue(500).setMessage("Unknown error occurred in processing of received trace data").build();
            return new ResponseEntity<>(status.toByteArray(), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/telemetry/v1/metrics")
    public ResponseEntity<byte[]> metric(@RequestBody byte[] body) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/x-protobuf");

        try {
            MetricsData md = MetricsData.parseFrom(body);

            metricsService.HandleMetric(md);

            ExportMetricsServiceResponse response = ExportMetricsServiceResponse.newBuilder().build();
            return new ResponseEntity<>(response.toByteArray(), responseHeaders, HttpStatus.OK);

        } catch (InvalidProtocolBufferException e) {
            Status status = Status.newBuilder().setCodeValue(400).setMessage("Could not parse MetricsData from the received body").build();
            return new ResponseEntity<>(status.toByteArray(), responseHeaders, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Status status = Status.newBuilder().setCodeValue(500).setMessage("Unknown error occurred in processing of received metric data").build();
            return new ResponseEntity<>(status.toByteArray(), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}

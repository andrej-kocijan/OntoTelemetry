package si.fri.liis.Services;

import io.opentelemetry.proto.trace.v1.TracesData;
import org.springframework.stereotype.Service;

@Service
public class TracesService {

    public void HandleTrace(TracesData tracesData) {
        System.out.println(tracesData.toString());
    }
}

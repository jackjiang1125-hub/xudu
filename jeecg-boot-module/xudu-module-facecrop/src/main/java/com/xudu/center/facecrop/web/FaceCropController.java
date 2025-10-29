package com.xudu.center.facecrop.web;

import com.xudu.center.facecrop.config.FaceProcessingProperties;
import com.xudu.center.facecrop.model.FaceQuality;
import com.xudu.center.facecrop.service.FaceCropService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FaceCropController {

    private final FaceCropService service;
    private final FaceProcessingProperties props;

    public FaceCropController(FaceCropService service, FaceProcessingProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public FaceProcessingProperties config() {
        return props;
    }

    @PostMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> process() throws Exception {
        List<FaceQuality> rows = service.processAll();
        long total = rows.stream().filter(r -> r.faceIndex >= 0).count();
        long accepted = rows.stream().filter(r -> r.accepted).count();
        long imagesNoFace = rows.stream().filter(r -> r.faceIndex == -1).count();
        Map<String, Object> resp = new HashMap<>();
        resp.put("facesTotal", total);
        resp.put("accepted", accepted);
        resp.put("rejected", total - accepted);
        resp.put("imagesNoFace", imagesNoFace);
        resp.put("report", props.getOutputDir().resolve("report.csv").toString());
        resp.put("cropsDir", props.getOutputDir().resolve("crops").toString());
        return resp;
    }
}

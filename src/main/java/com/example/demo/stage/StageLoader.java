package com.example.demo.stage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
public class StageLoader {

    private final List<Stage> stages;

    public StageLoader() throws IOException {
        YAMLMapper mapper = new YAMLMapper();
        InputStream is = getClass().getResourceAsStream("/stages.yaml");
        stages = mapper.readValue(is, new TypeReference<>() {});
    }

    public int totalCount() {
        return stages.size();
    }

    public Optional<Stage> findById(int id) {
        return stages.stream().filter(s -> s.id() == id).findFirst();
    }
}

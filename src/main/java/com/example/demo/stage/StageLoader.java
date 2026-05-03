package com.example.demo.stage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class StageLoader {

    private final List<Stage> stages;
    private final Path stagesFilePath;
    private final YAMLMapper mapper = new YAMLMapper();

    public StageLoader() throws IOException {
        this("src/main/resources/stages.yaml");
    }

    public StageLoader(@Value("${stages.file:src/main/resources/stages.yaml}") String stagesFile) throws IOException {
        this.stagesFilePath = Paths.get(stagesFile);
        if (Files.exists(stagesFilePath)) {
            stages = new ArrayList<>(mapper.readValue(stagesFilePath.toFile(), new TypeReference<>() {}));
        } else {
            InputStream is = getClass().getResourceAsStream("/stages.yaml");
            stages = new ArrayList<>(mapper.readValue(is, new TypeReference<>() {}));
        }
    }

    public int totalCount() {
        return stages.size();
    }

    public Optional<Stage> findById(int id) {
        return stages.stream().filter(s -> s.id() == id).findFirst();
    }

    public int nextId() {
        return stages.stream().mapToInt(Stage::id).max().orElse(0) + 1;
    }

    public void addStage(Stage stage) throws IOException {
        stages.add(stage);
        mapper.writeValue(stagesFilePath.toFile(), stages);
    }
}

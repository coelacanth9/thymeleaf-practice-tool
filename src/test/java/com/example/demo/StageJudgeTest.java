package com.example.demo;

import com.example.demo.judge.JudgeService;
import com.example.demo.stage.Stage;
import com.example.demo.stage.StageLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StageJudgeTest {

    static JudgeService judgeService;
    static StageLoader stageLoader;

    @BeforeAll
    static void setup() throws IOException {
        judgeService = new JudgeService();
        stageLoader = new StageLoader();
    }

    @ParameterizedTest(name = "[stage {0}] {1}")
    @MethodSource("testCases")
    void judge(int stageId, String description, String input, boolean shouldPass) {
        Stage stage = stageLoader.findById(stageId).orElseThrow();
        JudgeService.JudgeResult result = judgeService.judge(input, stage);
        String msg = "stage=" + stageId + " [" + description + "]"
                + (result.error() != null ? " error=" + result.error() : "")
                + (result.diffMessage() != null ? " diff=" + result.diffMessage() : "");
        assertEquals(shouldPass, result.correct(), msg);
    }

    static Stream<Arguments> testCases() throws IOException {
        YAMLMapper mapper = new YAMLMapper();
        InputStream is = StageJudgeTest.class.getResourceAsStream("/test-cases.yaml");
        List<TestSuite> suites = mapper.readValue(is, new TypeReference<>() {});
        return suites.stream()
                .flatMap(suite -> suite.cases.stream()
                        .map(c -> Arguments.of(suite.stageId, c.description, c.input, c.shouldPass)));
    }

    static class TestCase {
        public String description;
        public String input;
        public boolean shouldPass;
    }

    static class TestSuite {
        public int stageId;
        public List<TestCase> cases;
    }
}

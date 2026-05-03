package com.example.demo.controller;

import com.example.demo.stage.Stage;
import com.example.demo.stage.StageLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Profile("dev")
@Controller
public class EditorController {

    private final StageLoader stageLoader;
    private final YAMLMapper yamlMapper = new YAMLMapper();

    public EditorController(StageLoader stageLoader) {
        this.stageLoader = stageLoader;
    }

    @GetMapping("/editor")
    public String showEditor(Model model) {
        model.addAttribute("nextId", stageLoader.nextId());
        return "editor";
    }

    @PostMapping("/editor")
    public String saveStage(
            @RequestParam String task,
            @RequestParam String modelYaml,
            @RequestParam String scaffold,
            @RequestParam String hint,
            @RequestParam(required = false) String hintDetail,
            @RequestParam String expectedHtml,
            @RequestParam String requiredSyntax,
            @RequestParam String judgeMode,
            Model model) {

        try {
            Map<String, Object> modelData = yamlMapper.readValue(modelYaml, new TypeReference<>() {});
            List<String> syntaxList = Arrays.stream(requiredSyntax.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            int id = stageLoader.nextId();
            Stage stage = new Stage(id, task, modelData, scaffold, hint, hintDetail, expectedHtml, syntaxList, judgeMode, null);
            stageLoader.addStage(stage);
            return "redirect:/stage/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "保存に失敗しました: " + e.getMessage());
            model.addAttribute("nextId", stageLoader.nextId());
            return "editor";
        }
    }
}

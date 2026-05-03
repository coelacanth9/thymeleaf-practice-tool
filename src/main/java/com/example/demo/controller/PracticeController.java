package com.example.demo.controller;

import com.example.demo.judge.JudgeService;
import com.example.demo.stage.Stage;
import com.example.demo.stage.StageLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PracticeController {

    private final StageLoader stageLoader;
    private final JudgeService judgeService;

    public PracticeController(StageLoader stageLoader, JudgeService judgeService) {
        this.stageLoader = stageLoader;
        this.judgeService = judgeService;
    }

    @GetMapping("/")
    public String root() {
        return "welcome";
    }

    @GetMapping("/stage/{id}")
    public String show(@PathVariable int id, Model model) {
        Stage stage = stageLoader.findById(id).orElseThrow();
        populateModel(model, stage, stage.scaffold(), false);
        return "practice";
    }

    @PostMapping("/stage/{id}/judge")
    public String judge(
            @PathVariable int id,
            @RequestParam String userHtml,
            @RequestParam(required = false) String showHint,
            Model model) {

        Stage stage = stageLoader.findById(id).orElseThrow();
        populateModel(model, stage, userHtml, "true".equals(showHint));

        JudgeService.JudgeResult result = judgeService.judge(userHtml, stage);
        if (result.preview() != null) {
            model.addAttribute("preview", result.preview());
        }
        if (result.error() != null) {
            model.addAttribute("error", result.error());
        } else {
            model.addAttribute("correct", result.correct());
            if (result.diffMessage() != null) {
                model.addAttribute("diffMessage", result.diffMessage());
            }
        }
        return "practice";
    }

    private void populateModel(Model model, Stage stage, String scaffold, boolean showHint) {
        model.addAttribute("stageId", stage.id());
        model.addAttribute("stageNum", stage.id());
        model.addAttribute("totalStages", stageLoader.totalCount());
        model.addAttribute("task", stage.task());
        model.addAttribute("modelData", stage.model());
        model.addAttribute("expectedHtml", stage.expectedHtml());
        model.addAttribute("scaffold", scaffold);
        model.addAttribute("hint", stage.hint());
        model.addAttribute("showHint", showHint);
        stageLoader.findById(stage.id() + 1)
                .ifPresent(next -> model.addAttribute("nextStageId", next.id()));
        stageLoader.findById(stage.id() - 1)
                .ifPresent(prev -> model.addAttribute("prevStageId", prev.id()));
    }
}

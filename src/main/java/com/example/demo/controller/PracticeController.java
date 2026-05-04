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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // flash属性がなければデフォルト値をセット
        if (!model.containsAttribute("scaffold")) {
            model.addAttribute("scaffold", stage.scaffold());
        }
        if (!model.containsAttribute("hintLevel")) {
            model.addAttribute("hintLevel", 0);
        }
        addStageInfo(model, stage);
        return "practice";
    }

    @PostMapping("/stage/{id}/judge")
    public String judge(
            @PathVariable int id,
            @RequestParam String userHtml,
            @RequestParam(required = false, defaultValue = "0") int hintLevel,
            RedirectAttributes redirectAttrs) {

        Stage stage = stageLoader.findById(id).orElseThrow();

        redirectAttrs.addFlashAttribute("scaffold", userHtml);
        redirectAttrs.addFlashAttribute("hintLevel", hintLevel);

        JudgeService.JudgeResult result = judgeService.judge(userHtml, stage);
        if (result.preview() != null) {
            redirectAttrs.addFlashAttribute("preview", result.preview());
        }
        if (hintLevel == 0) {
            // 判定ボタン押下時のみ結果を表示
            if (result.error() != null) {
                redirectAttrs.addFlashAttribute("error", result.error());
            } else {
                redirectAttrs.addFlashAttribute("correct", result.correct());
                if (result.diffMessage() != null) {
                    redirectAttrs.addFlashAttribute("diffMessage", result.diffMessage());
                }
            }
        }

        return "redirect:/stage/" + id;
    }

    private void addStageInfo(Model model, Stage stage) {
        model.addAttribute("stageId", stage.id());
        model.addAttribute("stageNum", stage.id());
        model.addAttribute("totalStages", stageLoader.totalCount());
        model.addAttribute("task", stage.task());
        model.addAttribute("modelData", stage.model());
        model.addAttribute("expectedHtml", stage.expectedHtml());
        model.addAttribute("hint", stage.hint());
        model.addAttribute("hintDetail", stage.hintDetail());
        model.addAttribute("displayMode", stage.displayMode());
        stageLoader.findById(stage.id() + 1)
                .ifPresent(next -> model.addAttribute("nextStageId", next.id()));
        stageLoader.findById(stage.id() - 1)
                .ifPresent(prev -> model.addAttribute("prevStageId", prev.id()));
    }
}

package com.example.demo.stage;

import java.util.List;
import java.util.Map;

public record Stage(
        int id,
        String task,
        Map<String, Object> model,
        String scaffold,
        String hint,
        String hintDetail,
        String expectedHtml,
        List<String> requiredSyntax,
        String judgeMode,
        String displayMode
) {}

package com.example.demo.judge;

import com.example.demo.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.AbstractLinkBuilder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class JudgeService {

    public record JudgeResult(boolean correct, String diffMessage, String error, String preview) {}

    public JudgeResult judge(String userHtml, Stage stage) {
        String rendered;
        try {
            rendered = render(userHtml, stage);
        } catch (Exception e) {
            return new JudgeResult(false, null, "レンダリングエラー: " + e.getMessage(), null);
        }

        if (stage.requiredSyntax() != null) {
            String missing = stage.requiredSyntax().stream()
                    .filter(s -> !userHtml.contains(s))
                    .findFirst().orElse(null);
            if (missing != null) {
                return new JudgeResult(false, "「" + missing + "」を使ってください", null, rendered);
            }
        }

        Document renderedDoc = Jsoup.parseBodyFragment(rendered);
        Document expectedDoc = Jsoup.parseBodyFragment(stage.expectedHtml());

        if ("dom".equals(stage.judgeMode())) {
            Element expectedRoot = expectedDoc.body().firstElementChild();
            Element renderedRoot = renderedDoc.body().firstElementChild();
            boolean correct = domMatches(expectedRoot, renderedRoot);
            String diff = correct ? null : describeDiff(expectedRoot, renderedRoot);
            return new JudgeResult(correct, diff, null, rendered);
        } else {
            String expectedText = expectedDoc.body().text();
            String renderedText = renderedDoc.body().text();
            boolean correct = expectedText.equals(renderedText);
            String diff = correct ? null :
                    "テキストが違います（期待: \"" + expectedText + "\", 実際: \"" + renderedText + "\"）";
            return new JudgeResult(correct, diff, null, rendered);
        }
    }

    public String render(String template, Stage stage) {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setLinkBuilders(Set.of(new SimpleWeblessLinkBuilder()));
        Context ctx = new Context();
        ctx.setVariables(stage.model());
        return engine.process(template, ctx);
    }

    private static class SimpleWeblessLinkBuilder extends AbstractLinkBuilder {
        @Override
        public String buildLink(IExpressionContext context, String base, Map<String, Object> parameters) {
            String path = base;
            Map<String, Object> remaining = new LinkedHashMap<>(parameters);
            for (Iterator<Map.Entry<String, Object>> it = remaining.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
                String placeholder = "{" + entry.getKey() + "}";
                if (path.contains(placeholder)) {
                    path = path.replace(placeholder, String.valueOf(entry.getValue()));
                    it.remove();
                }
            }
            if (!remaining.isEmpty()) {
                StringBuilder sb = new StringBuilder(path).append("?");
                for (Map.Entry<String, Object> e : remaining.entrySet()) {
                    sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
                }
                sb.setLength(sb.length() - 1);
                return sb.toString();
            }
            return path;
        }
    }

    private boolean domMatches(Element expected, Element actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;
        if (!expected.tagName().equals(actual.tagName())) return false;
        for (var attr : expected.attributes()) {
            if (!attr.getValue().equals(actual.attr(attr.getKey()))) return false;
        }
        if (expected.children().size() != actual.children().size()) return false;
        for (int i = 0; i < expected.children().size(); i++) {
            if (!domMatches(expected.child(i), actual.child(i))) return false;
        }
        return expected.ownText().equals(actual.ownText());
    }

    private String describeDiff(Element expected, Element actual) {
        if (expected == null && actual == null) return null;
        if (actual == null) return "要素が足りません: <" + expected.tagName() + "> が必要です";
        if (expected == null) return "要素が多すぎます";
        if (!expected.tagName().equals(actual.tagName())) {
            return "<" + actual.tagName() + "> ではなく <" + expected.tagName() + "> を使いましょう";
        }
        for (var attr : expected.attributes()) {
            if (!attr.getValue().equals(actual.attr(attr.getKey()))) {
                return "属性 " + attr.getKey() + " の値が違います（期待: \""
                        + attr.getValue() + "\", 実際: \"" + actual.attr(attr.getKey()) + "\"）";
            }
        }
        if (expected.children().size() != actual.children().size()) {
            return "子要素の数が違います（期待: " + expected.children().size()
                    + "個, 実際: " + actual.children().size() + "個）";
        }
        for (int i = 0; i < expected.children().size(); i++) {
            String childDiff = describeDiff(expected.child(i), actual.child(i));
            if (childDiff != null) return childDiff;
        }
        if (!expected.ownText().equals(actual.ownText())) {
            return "テキストが違います（期待: \"" + expected.ownText()
                    + "\", 実際: \"" + actual.ownText() + "\"）";
        }
        return null;
    }
}

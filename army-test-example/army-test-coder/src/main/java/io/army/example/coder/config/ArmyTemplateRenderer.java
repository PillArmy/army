package io.army.example.coder.config;

import io.army.util._Collections;
import io.army.util._TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.st.StTemplateRenderer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

final class ArmyTemplateRenderer implements TemplateRenderer {

    static ArmyTemplateRenderer from(TemplateRenderer templateRenderer) {
        return new ArmyTemplateRenderer(templateRenderer);
    }

    static ArmyTemplateRenderer defaultInstance() {
        return new ArmyTemplateRenderer(StTemplateRenderer.builder().build());
    }

    private static final Logger LOG = LoggerFactory.getLogger(ArmyTemplateRenderer.class);

    static final String OFFSET_NOW = "${OFFSET_NOW}";

    static final String BIRTH_PERIOD = "${BIRTH_PERIOD}";


    private final TemplateRenderer templateRenderer;

    private ArmyTemplateRenderer(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String apply(String template, Map<String, ?> variables) {
        final Map<String, Object> newMap = _Collections.hashMapForSize(variables.size());

        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(8));

        Object value;
        for (Map.Entry<String, ?> e : variables.entrySet()) {
            value = e.getValue();

            if (!(value instanceof String text)) {
                newMap.put(e.getKey(), value);
                continue;
            }

            switch (text) {
                case OFFSET_NOW:
                    value = now.format(_TimeUtils.OFFSET_DATETIME_FORMATTER_6);
                    break;
                case BIRTH_PERIOD:
                    value = String.format(" %s 天", AgentTool.BIRTH_DAY.until(now, ChronoUnit.DAYS));
                    break;
            }

            newMap.put(e.getKey(), value);

        } // loop

        final String finalTemplate;
        finalTemplate = this.templateRenderer.apply(template, newMap);
        //  LOG.debug("finalTemplate :\n{}", finalTemplate);
        return finalTemplate;
    }


}



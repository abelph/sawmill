package io.logz.sawmill.processors;

import io.logz.sawmill.Doc;
import io.logz.sawmill.ProcessResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.logz.sawmill.utils.DocUtils.createDoc;
import static org.assertj.core.api.Assertions.assertThat;

public class UserAgentProcessorTest {
    @Test
    public void testValidUserAgent() {
        String field = "agent";
        String targetField = "user_agent";
        String uaString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36";
        Doc doc = createDoc(field, uaString);

        Map<String,Object> config = new HashMap<>();
        config.put("field", field);
        UserAgentProcessor uaProceesor = new UserAgentProcessor.Factory().create(config);

        ProcessResult processResult = uaProceesor.process(doc);

        assertThat(processResult.isSucceeded()).isTrue();
        assertThat(doc.hasField(targetField)).isTrue();

        Map<String,String> userAgent = doc.getField(targetField);

        assertThat(userAgent.get("name")).isEqualTo("Chrome");
        assertThat(userAgent.get("major")).isEqualTo("54");
        assertThat(userAgent.get("minor")).isEqualTo("0");
        assertThat(userAgent.get("patch")).isEqualTo("2840");

        assertThat(userAgent.get("os")).isEqualTo("Mac OS X 10.10.5");
        assertThat(userAgent.get("os_name")).isEqualTo("Mac OS X");
        assertThat(userAgent.get("os_major")).isEqualTo("10");
        assertThat(userAgent.get("os_minor")).isEqualTo("10");
        assertThat(userAgent.get("os_patch")).isEqualTo("5");

        assertThat(userAgent.get("device")).isEqualTo("Other");
    }

    @Test
    public void testInvalidUserAgent() {
        String field = "agent";
        String targetField = "user_agent";
        String uaString = "invalid user-agent: dsafkjl";
        Doc doc = createDoc(field, uaString);

        Map<String,Object> config = new HashMap<>();
        config.put("field", field);
        UserAgentProcessor uaProceesor = new UserAgentProcessor.Factory().create(config);

        ProcessResult processResult = uaProceesor.process(doc);

        assertThat(processResult.isSucceeded()).isTrue();
        assertThat(doc.hasField(targetField)).isTrue();

        Map<String,String> userAgent = doc.getField(targetField);

        assertThat(userAgent.get("name")).isEqualTo("Other");
        assertThat(userAgent.get("os")).isEqualTo("Other");
        assertThat(userAgent.get("os_name")).isEqualTo("Other");
        assertThat(userAgent.get("device")).isEqualTo("Other");
    }
}
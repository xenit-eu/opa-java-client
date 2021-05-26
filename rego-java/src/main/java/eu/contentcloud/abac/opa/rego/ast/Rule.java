package eu.contentcloud.abac.opa.rego.ast;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Rule {

    private Head head;
    private List<Expression> body;

    @Data
    public static class Head {

        private String name;
        private Term key;

        private Map<String, Object> unknownFields = new LinkedHashMap<>();

        @JsonAnySetter
        public void unknownField(String name, Object value) {
            unknownFields.put(name, value);
        }
    }
}

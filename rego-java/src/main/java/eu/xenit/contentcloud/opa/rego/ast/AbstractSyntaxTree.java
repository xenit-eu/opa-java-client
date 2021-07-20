package eu.xenit.contentcloud.opa.rego.ast;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class AbstractSyntaxTree {

    @JsonProperty(value = "package")
    private Package pkg;

    private List<Rule> rules;

}

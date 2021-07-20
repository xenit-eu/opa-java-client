package eu.xenit.contentcloud.opa.rego.ast;

import java.util.List;
import lombok.Data;

@Data
public class Package {
    private List<Rule> path;
}

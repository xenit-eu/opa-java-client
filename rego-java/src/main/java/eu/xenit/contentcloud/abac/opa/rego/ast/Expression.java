package eu.xenit.contentcloud.abac.opa.rego.ast;

import java.util.List;
import lombok.Data;

@Data
public class Expression implements Node {
    long index;
    List<? extends Term> terms;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

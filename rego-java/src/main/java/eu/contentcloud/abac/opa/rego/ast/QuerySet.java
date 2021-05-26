package eu.contentcloud.abac.opa.rego.ast;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuerySet extends ArrayList<Query> implements Node{


    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


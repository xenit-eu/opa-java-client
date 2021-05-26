package eu.contentcloud.abac.opa.rego.ast;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public
class Query extends ArrayList<Expression> implements Node {


    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


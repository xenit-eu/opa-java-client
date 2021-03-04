package eu.xenit.contentcloud.abac.opa.rego.ast;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public
class Query extends ArrayList<Expression> implements Node {


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}


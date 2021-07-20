package eu.xenit.contentcloud.opa.rego.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Query extends ArrayList<Expression> implements Node {

    public Query(@NonNull Expression expression) {
        this(List.of(expression));
    }

    public Query(Collection<Expression> expression) {
        super(expression);
    }


    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


package eu.xenit.contentcloud.thunx.opa.rego.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuerySet extends ArrayList<Query> implements Node {

    public QuerySet(List<Query> queries) {
        super(queries);
    }

    public QuerySet(Query ... queries) {
        this(List.of(queries));
    }

    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


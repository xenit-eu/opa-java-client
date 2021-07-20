package eu.xenit.contentcloud.opa.rego.ast;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expression implements Node {
    long index;
    List<? extends Term> terms;

    public Expression(long index, Term ... terms) {
        this(index, List.of(terms));
    }

    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

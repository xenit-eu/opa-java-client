package com.contentgrid.opa.rego.ast;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expression implements Node {
    long index;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    List<? extends Term> terms;

    public Expression(long index, Term ... terms) {
        this(index, List.of(terms));
    }

    @Override
    public <T> T accept(RegoVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

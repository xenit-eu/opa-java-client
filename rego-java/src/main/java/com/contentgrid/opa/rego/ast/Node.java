package com.contentgrid.opa.rego.ast;

public interface Node {
    <T> T accept(RegoVisitor<T> visitor);
}

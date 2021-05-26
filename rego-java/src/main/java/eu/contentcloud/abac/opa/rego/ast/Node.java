package eu.contentcloud.abac.opa.rego.ast;

public interface Node {
    <T> T accept(RegoVisitor<T> visitor);
}

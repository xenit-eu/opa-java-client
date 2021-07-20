package eu.xenit.contentcloud.thunx.opa.rego.ast;

public interface Node {
    <T> T accept(RegoVisitor<T> visitor);
}

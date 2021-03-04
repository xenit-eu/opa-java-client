package eu.xenit.contentcloud.abac.opa.rego.ast;

public interface Node {
    void accept(Visitor visitor);
}

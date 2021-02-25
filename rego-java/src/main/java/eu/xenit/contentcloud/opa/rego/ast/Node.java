package eu.xenit.contentcloud.opa.rego.ast;

public interface Node {
    void accept(Visitor visitor);
}

package eu.xenit.contentcloud.abac.opa.rego.ast;

import eu.xenit.contentcloud.abac.opa.rego.ast.Term.BooleanValue;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Call;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.NumberValue;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Ref;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.StringValue;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Var;

public interface Visitor {
    void visit(Query query);
    void visit(Expression expression);
    void visit(Ref ref);

    void visit(Call call);
    void visit(Var var);
    void visit(NumberValue numberValue);
    void visit(StringValue stringValue);
    void visit(BooleanValue booleanValue);
}

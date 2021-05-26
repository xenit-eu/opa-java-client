package eu.contentcloud.abac.opa.rego.ast;

import eu.contentcloud.abac.opa.rego.ast.Term.ArrayTerm;
import eu.contentcloud.abac.opa.rego.ast.Term.Call;
import eu.contentcloud.abac.opa.rego.ast.Term.Ref;
import eu.contentcloud.abac.opa.rego.ast.Term.Bool;
import eu.contentcloud.abac.opa.rego.ast.Term.Null;
import eu.contentcloud.abac.opa.rego.ast.Term.Numeric;
import eu.contentcloud.abac.opa.rego.ast.Term.Text;
import eu.contentcloud.abac.opa.rego.ast.Term.Var;

public interface RegoVisitor<T> {
    T visit(QuerySet queries);
    T visit(Query query);
    T visit(Expression expression);
    T visit(Ref ref);

    T visit(Call call);
    T visit(Var var);
    T visit(Numeric numberValue);
    T visit(Text stringValue);
    T visit(Bool booleanValue);
    T visit(Null nullValue);


    T visit(ArrayTerm arrayTerm);
}

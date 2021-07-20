package eu.xenit.contentcloud.thunx.opa.rego.ast;

import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.ArrayTerm;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Call;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Ref;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Bool;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Null;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Numeric;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Text;
import eu.xenit.contentcloud.thunx.opa.rego.ast.Term.Var;

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

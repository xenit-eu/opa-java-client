package com.contentgrid.opa.rego.ast;

import com.contentgrid.opa.rego.ast.Term.ArrayTerm;
import com.contentgrid.opa.rego.ast.Term.SetTerm;
import com.contentgrid.opa.rego.ast.Term.Call;
import com.contentgrid.opa.rego.ast.Term.Ref;
import com.contentgrid.opa.rego.ast.Term.Bool;
import com.contentgrid.opa.rego.ast.Term.Null;
import com.contentgrid.opa.rego.ast.Term.Numeric;
import com.contentgrid.opa.rego.ast.Term.Text;
import com.contentgrid.opa.rego.ast.Term.Var;

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
    T visit(SetTerm setTerm);
}

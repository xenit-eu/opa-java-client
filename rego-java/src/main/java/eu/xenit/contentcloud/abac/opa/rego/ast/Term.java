package eu.xenit.contentcloud.abac.opa.rego.ast;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.NumberValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @Type(value = Term.Ref.class, name = "ref"),
        @Type(value = Term.Call.class, name = "call"),
        @Type(value = Term.Var.class, name = "var"),
        @Type(value = NumberValue.class, name = "number"),
        @Type(value = Term.StringValue.class, name = "string"),
        @Type(value = Term.BooleanValue.class, name = "boolean")
})
public abstract class Term implements Node {

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ref extends Term {
        List<? extends ScalarValue> value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Call extends Term {
        List<? extends Term> value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public abstract static class ScalarValue extends Term {

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Var extends ScalarValue {
        String value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class NumberValue extends ScalarValue {
        double value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class StringValue extends ScalarValue {
        String value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BooleanValue extends ScalarValue {
        boolean value;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

    }




}

package com.contentgrid.opa.rego.ast;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.contentgrid.opa.rego.ast.Term.ArrayTerm;
import com.contentgrid.opa.rego.ast.Term.SetTerm;
import com.contentgrid.opa.rego.ast.Term.Bool;
import com.contentgrid.opa.rego.ast.Term.Null;
import com.contentgrid.opa.rego.ast.Term.Numeric;
import com.contentgrid.opa.rego.ast.Term.Text;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @Type(value = Term.Ref.class, name = "ref"),
        @Type(value = Term.Call.class, name = "call"),
        @Type(value = Term.Var.class, name = "var"),
        @Type(value = Numeric.class, name = "number"),
        @Type(value = Text.class, name = "string"),
        @Type(value = Bool.class, name = "boolean"),
        @Type(value = Null.class, name = "null"),
        @Type(value = ArrayTerm.class, name = "array"),
        @Type(value = SetTerm.class, name = "set")
})
public abstract class Term implements Node {

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ref extends Term {

        List<Term> value;

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return this.getValue().get(0) + this.getValue().stream()
                    .skip(1)
                    .map(term -> "[" + term.toString() + "]")
                    .collect(Collectors.joining());
        }

    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Call extends Term {

        List<? extends Term> value;

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Var extends Term {

        String value;

        @Override
        public String toString() {
            return this.getValue();
        }

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ArrayTerm extends Term {

        List<Term> value;

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class SetTerm extends Term {

        Set<Term> value;

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static abstract class ScalarTerm<T> extends Term {

        public abstract T getValue();

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text extends ScalarTerm<String> {

        String value;

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return "\"" + this.getValue() + "\"";
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Bool extends ScalarTerm<Boolean> {

        private boolean value;

        public Boolean getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.getValue().toString().toUpperCase(Locale.ROOT);
        }

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Numeric extends ScalarTerm<BigDecimal> {

        @Getter
        BigDecimal value;

        public Numeric(long number) {
            this(BigDecimal.valueOf(number));
        }

        public Numeric(double number) {
            this(BigDecimal.valueOf(number));
        }


        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return this.getValue().toString();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Null extends ScalarTerm<Void> {

        @Override
        public <T> T accept(RegoVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return "null";
        }

        @Override
        public Void getValue() {
            return null;
        }
    }
}

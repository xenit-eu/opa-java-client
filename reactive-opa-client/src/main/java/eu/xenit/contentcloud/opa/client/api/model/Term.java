package eu.xenit.contentcloud.opa.client.api.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.xenit.contentcloud.opa.client.api.model.Term.NumberValue;
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
})
public class Term {

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ref extends Term {
        List<? extends ScalarValue> value;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Call extends Term {
        List<? extends Term> value;
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
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class NumberValue extends ScalarValue {
        long value;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class StringValue extends ScalarValue {
        String value;
    }


}

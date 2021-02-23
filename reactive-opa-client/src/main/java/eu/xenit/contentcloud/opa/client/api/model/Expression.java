package eu.xenit.contentcloud.opa.client.api.model;

import java.util.List;
import lombok.Data;

@Data
public class Expression {
    long index;
    List<? extends Term> terms;
}

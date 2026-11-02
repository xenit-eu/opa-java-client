package com.contentgrid.opa.client.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.contentgrid.opa.client.api.PolicyApi.ListPoliciesResponse;
import com.contentgrid.opa.rego.ast.Term;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

class PolicyApiTest {

    @Test
    void testPoliciesResponseObjectMapping() {
        var jsonMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();

        // read listpoliciesresponse.json into ListPoliciesResponse
        try (var is = getClass().getClassLoader().getResourceAsStream(
                "com/contentgrid/opa/client/api/policyapi/listpoliciesresponse.json")) {
            var response = jsonMapper.readValue(is, ListPoliciesResponse.class);
            assertNotNull(response);
            assertThat(response.getResult().get(2).getAst().getRules().get(1).getBody()).singleElement().satisfies(expression -> {
                assertThat(expression.getTerms()).singleElement().isInstanceOfSatisfying(Term.Bool.class, boolTerm -> {
                    assertThat(boolTerm.getValue()).isTrue();
                });
            });
        } catch (Exception e) {
            fail("Failed to deserialize ListPoliciesResponse", e);
        }
    }

}
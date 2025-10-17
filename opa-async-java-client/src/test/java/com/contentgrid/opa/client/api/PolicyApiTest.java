package com.contentgrid.opa.client.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.contentgrid.opa.client.api.PolicyApi.ListPoliciesResponse;
import com.contentgrid.opa.rego.ast.Term;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

class PolicyApiTest {

    @Test
    void testPoliciesResponseObjectMapping() {
        var objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build()
                .registerModule(new JavaTimeModule());

        // read listpoliciesresponse.json into ListPoliciesResponse
        try (var is = getClass().getClassLoader().getResourceAsStream("com/contentgrid/opa/client/api/policyapi/listpoliciesrsponse.json")) {
            var response = objectMapper.readValue(is, ListPoliciesResponse.class);
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
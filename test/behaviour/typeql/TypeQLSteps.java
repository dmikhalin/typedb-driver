/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.typedb.client.test.behaviour.typeql;

import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.answer.ConceptMapGroup;
import com.vaticle.typedb.client.api.answer.Numeric;
import com.vaticle.typedb.client.api.answer.NumericGroup;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.query.QueryFuture;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.common.exception.TypeQLException;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrowsWithMessage;
import static com.vaticle.typedb.common.collection.Collections.set;
import static com.vaticle.typedb.common.util.Double.equalsApproximate;
import static com.vaticle.typeql.lang.common.TypeQLToken.Annotation.KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TypeQLSteps {

    private static List<ConceptMap> answers;
    private static Numeric numericAnswer;
    private static List<ConceptMapGroup> answerGroups;
    private static List<NumericGroup> numericAnswerGroups;
    private Map<String, Map<String, String>> rules;

    public static List<ConceptMap> answers() {
        return answers;
    }

    @Given("the integrity is validated")
    public void integrity_is_validated() {

        // TODO

    }

    @Given("typeql define")
    public QueryFuture<Void> typeql_define(String defineQueryStatements) {
        TypeQLDefine typeQLQuery = TypeQL.parseQuery(String.join("\n", defineQueryStatements));
        return tx().query().define(typeQLQuery);
    }

    @Given("typeql define; throws exception")
    public void typeql_define_throws(String defineQueryStatements) {
        assertThrows(() -> typeql_define(defineQueryStatements).get());
    }

    @Given("typeql define; throws exception containing {string}")
    public void typeql_define_throws_exception(String exception, String defineQueryStatements) {
        assertThrowsWithMessage(() -> typeql_define(defineQueryStatements).get(), exception);
    }

    @Given("typeql undefine")
    public QueryFuture<Void> typeql_undefine(String undefineQueryStatements) {
        TypeQLUndefine typeQLQuery = TypeQL.parseQuery(String.join("\n", undefineQueryStatements));
        return tx().query().undefine(typeQLQuery);
    }

    @Given("typeql undefine; throws exception")
    public void typeql_undefine_throws(String undefineQueryStatements) {
        assertThrows(() -> typeql_undefine(undefineQueryStatements).get());
    }

    @Given("typeql undefine; throws exception containing {string}")
    public void typeql_undefine_throws_exception(String exception, String undefineQueryStatements) {
        assertThrowsWithMessage(() -> typeql_undefine(undefineQueryStatements).get(), exception);
    }

    @Given("typeql insert")
    public Stream<ConceptMap> typeql_insert(String insertQueryStatements) {
        TypeQLInsert typeQLQuery = TypeQL.parseQuery(String.join("\n", insertQueryStatements));
        return tx().query().insert(typeQLQuery);
    }

    @Given("typeql insert; throws exception")
    @SuppressWarnings("ReturnValueIgnored")
    public void typeql_insert_throws(String insertQueryStatements) {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(() -> typeql_insert(insertQueryStatements).collect(Collectors.toList()));
    }

    @Given("typeql insert; throws exception containing {string}")
    @SuppressWarnings("ReturnValueIgnored")
    public void typeql_insert_throws_exception(String exception, String insertQueryStatements) {
        //noinspection ResultOfMethodCallIgnored
        assertThrowsWithMessage(() -> typeql_insert(insertQueryStatements).collect(Collectors.toList()), exception);
    }

    @Given("typeql delete")
    public QueryFuture<Void> typeql_delete(String deleteQueryStatements) {
        TypeQLDelete typeQLQuery = TypeQL.parseQuery(String.join("\n", deleteQueryStatements));
        return tx().query().delete(typeQLQuery);
    }

    @Given("typeql delete; throws exception")
    public void typeql_delete_throws(String deleteQueryStatements) {
        assertThrows(() -> typeql_delete(deleteQueryStatements).get());
    }

    @Given("typeql delete; throws exception containing {string}")
    public void typeql_delete_throws_exception(String exception, String deleteQueryStatements) {
        assertThrowsWithMessage(() -> typeql_delete(deleteQueryStatements).get(), exception);
    }

    @Given("typeql update")
    public Stream<ConceptMap> typeql_update(String updateQueryStatements) {
        TypeQLUpdate typeQLQuery = TypeQL.parseQuery(String.join("\n", updateQueryStatements));
        return tx().query().update(typeQLQuery);
    }

    @Given("typeql update; throws exception")
    public void typeql_update_throws(String updateQueryStatements) {
        assertThrows(() -> typeql_update(updateQueryStatements).iterator().next());
    }

    @Given("typeql update; throws exception containing {string}")
    public void typeql_update_throws_exception(String exception, String updateQueryStatements) {
        assertThrowsWithMessage(() -> typeql_update(updateQueryStatements).iterator().next(), exception);
    }

    private void clearAnswers() {
        answers = null;
        numericAnswer = null;
        answerGroups = null;
        numericAnswerGroups = null;
    }

    @When("get answers of typeql insert")
    public void get_answers_of_typeql_insert(String typeQLQueryStatements) {
        TypeQLInsert typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements));
        clearAnswers();
        answers = tx().query().insert(typeQLQuery).collect(Collectors.toList());
    }

    @When("get answers of typeql match")
    public void typeql_match(String typeQLQueryStatements) {
        try {
            TypeQLMatch typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asMatch();
            clearAnswers();
            answers = tx().query().match(typeQLQuery).collect(Collectors.toList());
        } catch (TypeQLException e) {
            // NOTE: We manually close transaction here, because we want to align with all non-java clients,
            // where parsing happens at server-side which closes transaction if they fail
            tx().close();
            throw e;
        }
    }

    @When("typeql match; throws exception")
    public void typeql_match_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_match(typeQLQueryStatements));
    }

    @When("typeql match; throws exception containing {string}")
    public void typeql_match_throws_exception_containing(String error, String typeQLQueryStatements) {
        assertThrowsWithMessage(() -> typeql_match(typeQLQueryStatements), error);
    }

    @When("get answer of typeql match aggregate")
    public void typeql_match_aggregate(String typeQLQueryStatements) {
        TypeQLMatch.Aggregate typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asMatchAggregate();
        clearAnswers();
        numericAnswer = tx().query().match(typeQLQuery).get();
    }

    @When("typeql match aggregate; throws exception")
    public void typeql_match_aggregate_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_match_aggregate(typeQLQueryStatements));
    }

    @When("get answers of typeql match group")
    public void typeql_match_group(String typeQLQueryStatements) {
        TypeQLMatch.Group typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asMatchGroup();
        clearAnswers();
        answerGroups = tx().query().match(typeQLQuery).collect(Collectors.toList());
    }

    @When("typeql match group; throws exception")
    public void typeql_match_group_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_match_group(typeQLQueryStatements));
    }

    @When("get answers of typeql match group aggregate")
    public void typeql_match_group_aggregate(String typeQLQueryStatements) {
        TypeQLMatch.Group.Aggregate typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asMatchGroupAggregate();
        clearAnswers();
        numericAnswerGroups = tx().query().match(typeQLQuery).collect(Collectors.toList());
    }

    @Then("answer size is: {number}")
    public void answer_quantity_assertion(int expectedAnswers) {
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answers.size()),
                expectedAnswers, answers.size());
    }

    @Then("uniquely identify answer concepts")
    public void uniquely_identify_answer_concepts(List<Map<String, String>> answerConcepts) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answerConcepts.size(), answers.size()),
                answerConcepts.size(), answers.size()
        );

        for (ConceptMap answer : answers) {
            List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

            for (Map<String, String> answerIdentifier : answerConcepts) {

                if (matchAnswerConcept(answerIdentifier, answer)) {
                    matchingIdentifiers.add(answerIdentifier);
                }
            }
            assertEquals(
                    String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were %d matching identifier entries for answer with variables %s.",
                            matchingIdentifiers.size(), answer.map().keySet().toString()),
                    1, matchingIdentifiers.size()
            );
        }
    }

    @Then("order of answer concepts is")
    public void order_of_answer_concepts_is(List<Map<String, String>> answersIdentifiers) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answersIdentifiers.size(), answers.size()),
                answersIdentifiers.size(), answers.size()
        );
        for (int i = 0; i < answers.size(); i++) {
            ConceptMap answer = answers.get(i);
            Map<String, String> answerIdentifiers = answersIdentifiers.get(i);
            assertTrue(
                    String.format("The answer at index %d does not match the identifier entry (row) at index %d.", i, i),
                    matchAnswerConcept(answerIdentifiers, answer)
            );
        }
    }

    @Then("aggregate value is: {double}")
    public void aggregate_value_is(double expectedAnswer) {
        assertNotNull("The last executed query was not an aggregate query", numericAnswer);
        assertEquals(String.format("Expected answer to equal %f, but it was %f.", expectedAnswer, numericAnswer.asNumber().doubleValue()),
                expectedAnswer, numericAnswer.asNumber().doubleValue(), 0.001);
    }

    @Then("aggregate answer is not a number")
    public void aggregate_answer_is_not_a_number() {
        assertTrue(numericAnswer.isNaN());
    }

    @Then("answer groups are")
    public void answer_groups_are(List<Map<String, String>> answerIdentifierTable) {
        Set<AnswerIdentifierGroup> answerIdentifierGroups = answerIdentifierTable.stream()
                .collect(Collectors.groupingBy(x -> x.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME)))
                .values()
                .stream()
                .map(AnswerIdentifierGroup::new)
                .collect(Collectors.toSet());

        assertEquals(String.format("Expected [%d] answer groups, but found [%d].",
                        answerIdentifierGroups.size(), answerGroups.size()),
                answerIdentifierGroups.size(), answerGroups.size()
        );

        for (AnswerIdentifierGroup answerIdentifierGroup : answerIdentifierGroups) {
            String[] identifier = answerIdentifierGroup.ownerIdentifier.split(":", 2);
            UniquenessCheck checker;
            switch (identifier[0]) {
                case "label":
                    checker = new LabelUniquenessCheck(identifier[1]);
                    break;
                case "key":
                    checker = new KeyUniquenessCheck(identifier[1]);
                    break;
                case "attr":
                    checker = new AttributeValueUniquenessCheck(identifier[1]);
                    break;
                case "value":
                    checker = new ValueUniquenessCheck(identifier[1]);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
            }
            ConceptMapGroup answerGroup = answerGroups.stream()
                    .filter(ag -> checker.check(ag.owner()))
                    .findAny()
                    .orElse(null);
            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", answerIdentifierGroup.ownerIdentifier), answerGroup);

            List<Map<String, String>> answersIdentifiers = answerIdentifierGroup.answersIdentifiers;
            for (ConceptMap answer : answerGroup.conceptMaps()) {
                List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

                for (Map<String, String> answerIdentifiers : answersIdentifiers) {

                    if (matchAnswerConcept(answerIdentifiers, answer)) {
                        matchingIdentifiers.add(answerIdentifiers);
                    }
                }
                assertEquals(
                        String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were [%d] matching identifier entries for answer with variables %s.",
                                matchingIdentifiers.size(), answer.map().keySet().toString()),
                        1, matchingIdentifiers.size()
                );
            }
        }
    }

    @Then("group aggregate values are")
    public void group_aggregate_values_are(List<Map<String, String>> answerIdentifierTable) {
        Map<String, Double> expectations = new HashMap<>();
        for (Map<String, String> answerIdentifierRow : answerIdentifierTable) {
            String groupOwnerIdentifier = answerIdentifierRow.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME);
            double expectedAnswer = Double.parseDouble(answerIdentifierRow.get("value"));
            expectations.put(groupOwnerIdentifier, expectedAnswer);
        }

        assertEquals(String.format("Expected [%d] answer groups, but found [%d].", expectations.size(), numericAnswerGroups.size()),
                expectations.size(), numericAnswerGroups.size()
        );

        for (Map.Entry<String, Double> expectation : expectations.entrySet()) {
            String[] identifier = expectation.getKey().split(":", 2);
            UniquenessCheck checker;
            switch (identifier[0]) {
                case "label":
                    checker = new LabelUniquenessCheck(identifier[1]);
                    break;
                case "key":
                    checker = new KeyUniquenessCheck(identifier[1]);
                    break;
                case "attr":
                    checker = new AttributeValueUniquenessCheck(identifier[1]);
                    break;
                case "value":
                    checker = new ValueUniquenessCheck(identifier[1]);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
            }
            double expectedAnswer = expectation.getValue();
            NumericGroup answerGroup = numericAnswerGroups.stream()
                    .filter(ag -> checker.check(ag.owner()))
                    .findAny()
                    .orElse(null);
            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", expectation.getKey()), answerGroup);

            double actualAnswer = answerGroup.numeric().asNumber().doubleValue();
            assertEquals(
                    String.format("Expected answer [%f] for group [%s], but got [%f]",
                            expectedAnswer, expectation.getKey(), actualAnswer),
                    expectedAnswer, actualAnswer, 0.001
            );
        }
    }

    @Then("number of groups is: {int}")
    public void number_of_groups_is(int expectedGroupCount) {
        assertEquals(expectedGroupCount, answerGroups.size());
    }

    public static class AnswerIdentifierGroup {
        private final String ownerIdentifier;
        private final List<Map<String, String>> answersIdentifiers;

        private static final String GROUP_COLUMN_NAME = "owner";

        public AnswerIdentifierGroup(List<Map<String, String>> answerIdentifierTable) {
            ownerIdentifier = answerIdentifierTable.get(0).get(GROUP_COLUMN_NAME);
            answersIdentifiers = new ArrayList<>();
            for (Map<String, String> rawAnswerIdentifiers : answerIdentifierTable) {
                answersIdentifiers.add(rawAnswerIdentifiers.entrySet().stream()
                        .filter(e -> !e.getKey().equals(GROUP_COLUMN_NAME))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        }
    }

    // This method is used by explanation steps
//    private boolean matchAnswer(Map<String, String> answerIdentifiers, ConceptMap answer) {
//
//        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
//            final Reference.Name var = Reference.named(entry.getKey());
//            final String identifier = entry.getValue();
//
//            if (!identifierChecks.containsKey(identifier)) {
//                throw new ScenarioDefinitionException(String.format("Identifier \"%s\" hasn't previously been declared.", identifier));
//            }
//
//            if (!identifierChecks.get(identifier).check(answer.get(var))) {
//                return false;
//            }
//        }
//        return true;
//    }

    private boolean matchAnswerConcept(Map<String, String> answerIdentifiers, ConceptMap answer) {
        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
            String var = entry.getKey();
            String[] identifier = entry.getValue().split(":", 2);
            switch (identifier[0]) {
                case "label":
                    if (!new LabelUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "key":
                    if (!new KeyUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "attr":
                    if (!new AttributeValueUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "value":
                    if (!new ValueUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    @Then("rules are")
    public void rules_are(Map<String, Map<String, String>> rules) {
        this.rules = rules;
    }

    @Then("rules contain: {type_label}")
    public void rules_contain(String ruleLabel) {
        assert (tx().logic().getRules().anyMatch(rule -> rule.getLabel().equals(ruleLabel)));
    }

    @Then("rules do not contain: {type_label}")
    public void rules_do_not_contain(String ruleLabel) {
        assert (tx().logic().getRules().noneMatch(rule -> rule.getLabel().equals(ruleLabel)));
    }

    @Then("answers contain explanation tree")
    public void answers_contain_explanation_tree(Map<Integer, Map<String, String>> explanationTree) {
        // TODO
        throw new UnsupportedOperationException();
//        checkExplanationEntry(answers, explanationTree, 0);
    }

    /* private void checkExplanationEntry(List<ConceptMap> answers, Map<Integer, Map<String, String>> explanationTree, Integer entryId) {
        Map<String, String> explanationEntry = explanationTree.get(entryId);
        String[] vars = explanationEntry.get("vars").split(", ");
        String[] identifiers = explanationEntry.get("identifiers").split(", ");
        String[] children = explanationEntry.get("children").split(", ");

        if (vars.length != identifiers.length) {
            throw new ScenarioDefinitionException(String.format("vars and identifiers do not correspond for explanation entry %d. Found %d vars and %s identifiers", entryId, vars.length, identifiers.length));
        }

        Map<String, String> answerIdentifiers = IntStream.range(0, vars.length).boxed().collect(Collectors.toMap(i -> vars[i], i -> identifiers[i]));

        Optional<ConceptMap> matchingAnswer = answers.stream().filter(answer -> matchAnswer(answerIdentifiers, answer)).findFirst();

        assertTrue(String.format("No answer found for explanation entry %d that satisfies the vars and identifiers given", entryId), matchingAnswer.isPresent());
        ConceptMap answer = matchingAnswer.get();

        String queryWithIds = applyQueryTemplate(explanationEntry.get("pattern"), answer);
        Conjunction<?> queryWithIdsConj = TypeQL.and(TypeQL.parsePatternList(queryWithIds));
        assertEquals(
                String.format("Explanation entry %d has an incorrect pattern.\nExpected: %s\nActual: %s", entryId, queryWithIdsConj, answer.queryPattern()),
                queryWithIdsConj, answer.queryPattern()
        );

        String expectedRule = explanationEntry.get("rule");
        boolean hasExplanation = answer.hasExplanation();

        if (expectedRule.equals("lookup")) {

            assertFalse(String.format("Explanation entry %d is declared as a lookup, but an explanation was found", entryId), hasExplanation);

            String[] expectedChildren = {"-"};
            assertArrayEquals(String.format("Explanation entry %d is declared as a lookup, and so it should have no children, indicated as \"-\", but got children %s instead", entryId, Arrays.toString(children)), expectedChildren, children);
        } else {

            Explanation explanation = answer.explanation();
            List<ConceptMap> explAnswers = explanation.getAnswers();

            assertEquals(String.format("Explanation entry %d should have as many children as it has answers. Instead, %d children were declared, and %d answers were found.", entryId, children.length, explAnswers.size()), children.length, explAnswers.size());

            if (expectedRule.equals("join")) {
                assertNull(String.format("Explanation entry %d is declared as a join, and should not have a rule attached, but one was found", entryId), explanation.getRule());
            } else {
                // rule
                Rule.Remote rule = explanation.getRule();
                String ruleLabel = rule.getLabel();
                assertEquals(String.format("Incorrect rule label for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRule, ruleLabel), expectedRule, ruleLabel);

                Map<String, String> expectedRuleDefinition = rules.get(expectedRule);
                String when = Objects.requireNonNull(rule.getWhen()).toString();
                assertEquals(String.format("Incorrect rule body (when) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("when"), when), expectedRuleDefinition.get("when"), when);

                String then = Objects.requireNonNull(rule.getThen()).toString();
                assertEquals(String.format("Incorrect rule head (then) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("then"), then), expectedRuleDefinition.get("then"), then);
            }
            for (String child : children) {
                // Recurse
                checkExplanationEntry(explAnswers, explanationTree, Integer.valueOf(child));
            }
        }
    } */

    @Then("each answer satisfies")
    public void each_answer_satisfies(String templatedQuery) {
        for (ConceptMap answer : answers) {
            String query = applyQueryTemplate(templatedQuery, answer);
            TypeQLMatch typeQLQuery = TypeQL.parseQuery(query).asMatch();
            long answerSize = tx().query().match(typeQLQuery).count();
            assertEquals(1, answerSize);
        }
    }

    @Then("templated typeql match; throws exception")
    public void templated_typeql_match_throws_exception(String templatedTypeQLQuery) {
        String templatedQuery = String.join("\n", templatedTypeQLQuery);
        for (ConceptMap answer : answers) {
            String queryString = applyQueryTemplate(templatedQuery, answer);
            assertThrows(() -> {
                TypeQLMatch query = TypeQL.parseQuery(queryString).asMatch();
                long ignored = tx().query().match(query).count();
            });
        }
    }

    private String applyQueryTemplate(String template, ConceptMap templateFiller) {
        // find shortest matching strings between <>
        Pattern pattern = Pattern.compile("<.+?>");
        Matcher matcher = pattern.matcher(template);

        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String matched = matcher.group(0);
            String requiredVariable = variableFromTemplatePlaceholder(matched.substring(1, matched.length() - 1));

            builder.append(template, i, matcher.start());
            if (templateFiller.map().containsKey(requiredVariable)) {

                Concept concept = templateFiller.get(requiredVariable);
                if (!concept.isThing())
                    throw new ScenarioDefinitionException("Cannot apply IID templating to Type concepts");
                String conceptId = concept.asThing().getIID();
                builder.append(conceptId);

            } else {
                throw new ScenarioDefinitionException(String.format("No IID available for template placeholder: %s.", matched));
            }
            i = matcher.end();
        }
        builder.append(template.substring(i));
        return builder.toString();
    }

    private String variableFromTemplatePlaceholder(String placeholder) {
        if (placeholder.endsWith(".iid")) {
            String stripped = placeholder.replace(".iid", "");
            String withoutPrefix = stripped.replace("answer.", "");
            return withoutPrefix;
        } else {
            throw new ScenarioDefinitionException("Cannot replace template not based on ID.");
        }
    }

    private static class ScenarioDefinitionException extends RuntimeException {
        ScenarioDefinitionException(String message) {
            super(message);
        }
    }

    private interface UniquenessCheck {
        boolean check(Concept concept);
    }

    public static class LabelUniquenessCheck implements UniquenessCheck {

        private final Label label;

        LabelUniquenessCheck(String scopedLabel) {
            String[] tokens = scopedLabel.split(":");
            this.label = tokens.length > 1 ? Label.of(tokens[0], tokens[1]) : Label.of(tokens[0]);
        }

        @Override
        public boolean check(Concept concept) {
            if (concept.isType()) return label.equals(concept.asType().getLabel());
            throw new ScenarioDefinitionException("Concept was checked for label uniqueness, but it is not a Type.");
        }
    }

    public static class AttributeUniquenessCheck {

        protected final Label type;
        protected final String value;

        AttributeUniquenessCheck(String typeAndValue) {
            String[] s = typeAndValue.split(":");
            assertEquals(
                    String.format("A check for attribute uniqueness should be given in the format \"type:value\", but received %s.", typeAndValue),
                    2, s.length
            );
            type = Label.of(s[0]);
            value = s[1];
        }
    }

    public static class AttributeValueUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        AttributeValueUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        public boolean check(Concept concept) {
            if (!concept.isAttribute()) {
                return false;
            }

            Attribute<?> attribute = concept.asAttribute();
            AttributeType attributeType = attribute.getType();

            if (!type.equals(attributeType.getLabel())) {
                return false;
            }

            switch (attributeType.getValueType()) {
                case BOOLEAN:
                    return Boolean.valueOf(value).equals(attribute.asBoolean().getValue());
                case LONG:
                    return Long.valueOf(value).equals(attribute.asLong().getValue());
                case DOUBLE:
                    return equalsApproximate(Double.parseDouble(value), attribute.asDouble().getValue());
                case STRING:
                    return value.equals(attribute.asString().getValue());
                case DATETIME:
                    LocalDateTime dateTime;
                    try {
                        dateTime = LocalDateTime.parse(value);
                    } catch (DateTimeParseException e) {
                        dateTime = LocalDate.parse(value).atStartOfDay();
                    }
                    return dateTime.equals(attribute.asDateTime().getValue());
                case OBJECT:
                default:
                    throw new ScenarioDefinitionException("Unrecognised value type " + attributeType.getValueType());
            }
        }
    }

    public static class KeyUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        KeyUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        @Override
        public boolean check(Concept concept) {
            if (!concept.isThing()) return false;

            Optional<? extends Attribute<?>> keyOpt = concept.asThing().asRemote(tx()).getHas(set(KEY))
                    .filter(attr -> attr.getType().getLabel().equals(type)).findFirst();
            if (keyOpt.isEmpty()) return false;

            Attribute<?> key = keyOpt.get().asAttribute();
            switch (key.getType().getValueType()) {
                case BOOLEAN:
                    return Boolean.valueOf(value).equals(key.asBoolean().getValue());
                case LONG:
                    return Long.valueOf(value).equals(key.asLong().getValue());
                case DOUBLE:
                    return equalsApproximate(Double.parseDouble(value), key.asDouble().getValue());
                case STRING:
                    return value.equals(key.asString().getValue());
                case DATETIME:
                    LocalDateTime dateTime;
                    try {
                        dateTime = LocalDateTime.parse(value);
                    } catch (DateTimeParseException e) {
                        dateTime = LocalDate.parse(value).atStartOfDay();
                    }
                    return dateTime.equals(key.asDateTime().getValue());
                case OBJECT:
                default:
                    throw new ScenarioDefinitionException("Unrecognised value type " + key.getType().getValueType());
            }
        }
    }

    public static class ValueUniquenessCheck implements UniquenessCheck {
        private final String valueType;
        private final String value;

        ValueUniquenessCheck(String valueTypeAndValue) {
            String[] s = valueTypeAndValue.split(":");
            this.valueType = s[0].toLowerCase().strip();
            this.value = s[1].strip();
        }

        public boolean check(Concept concept) {
            if (!concept.isValue()) {
                return false;
            }

            switch (concept.asValue().getValueType()) {
                case BOOLEAN:
                    return Boolean.valueOf(value).equals(concept.asValue().asBoolean().getValue());
                case LONG:
                    return Long.valueOf(value).equals(concept.asValue().asLong().getValue());
                case DOUBLE:
                    return equalsApproximate(Double.parseDouble(value), concept.asValue().asDouble().getValue());
                case STRING:
                    return value.equals(concept.asValue().asString().getValue());
                case DATETIME:
                    LocalDateTime dateTime;
                    try {
                        dateTime = LocalDateTime.parse(value);
                    } catch (DateTimeParseException e) {
                        dateTime = LocalDate.parse(value).atStartOfDay();
                    }
                    return dateTime.equals(concept.asValue().asDateTime().getValue());
                default:
                    throw new ScenarioDefinitionException("Unrecognised value type specified in test " + this.valueType);
            }
        }
    }
}

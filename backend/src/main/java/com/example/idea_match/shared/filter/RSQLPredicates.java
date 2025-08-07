package com.example.idea_match.shared.filter;

import cz.jirutka.rsql.parser.ast.Arity;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import io.github.perplexhub.rsql.RSQLCustomPredicate;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;
@NoArgsConstructor
public class RSQLPredicates {

    public static final List<RSQLCustomPredicate<?>> PREDICATES = List.of(
            dateEqualsPredicate(),
            dateNotEqualPredicate(),
            dateGreaterThanPredicate(),
            dateLessThanPredicate(),
            dateBetweenPredicate()
    );

    private static RSQLCustomPredicate<?> dateEqualsPredicate() {
        return new RSQLCustomPredicate<>(
                new ComparisonOperator("=deq="),
                LocalDate.class,
                input -> input.getCriteriaBuilder().equal(
                        input.getCriteriaBuilder().function("TO_CHAR", String.class,
                                input.getPath(),
                                input.getCriteriaBuilder().literal("YYYY-MM-DD")),
                        input.getArguments().getFirst().toString()
                )
        );
    }

    private static RSQLCustomPredicate<?> dateNotEqualPredicate() {
        return new RSQLCustomPredicate<>(
                new ComparisonOperator("=dneq="),
                LocalDate.class,
                input -> input.getCriteriaBuilder().notEqual(
                        input.getCriteriaBuilder().function("TO_CHAR", String.class,
                                input.getPath(),
                                input.getCriteriaBuilder().literal("YYYY-MM-DD")),
                        input.getArguments().getFirst().toString()
                )
        );
    }

    private static RSQLCustomPredicate<?> dateGreaterThanPredicate() {
        return new RSQLCustomPredicate<>(
                new ComparisonOperator("=dgt="),
                LocalDate.class,
                input -> input.getCriteriaBuilder().greaterThan(
                        input.getCriteriaBuilder().function("TO_CHAR", String.class,
                                input.getPath(),
                                input.getCriteriaBuilder().literal("YYYY-MM-DD")),
                        ofNullable(input.getArguments().getFirst()).map(Object::toString).orElse("")
                )
        );
    }

    private static RSQLCustomPredicate<?> dateLessThanPredicate() {
        return new RSQLCustomPredicate<>(
                new ComparisonOperator("=dlt="),
                LocalDate.class,
                input -> input.getCriteriaBuilder().lessThan(
                        input.getCriteriaBuilder().function("TO_CHAR", String.class,
                                input.getPath(),
                                input.getCriteriaBuilder().literal("YYYY-MM-DD")),
                        ofNullable(input.getArguments().getFirst()).map(Object::toString).orElse("")
                )
        );
    }

    private static RSQLCustomPredicate<?> dateBetweenPredicate() {
        return new RSQLCustomPredicate<>(
                new ComparisonOperator("=dbt=", Arity.nary(2)),
                LocalDate.class,
                input -> input.getCriteriaBuilder().between(
                        input.getCriteriaBuilder().function("TO_CHAR", String.class,
                                input.getPath(),
                                input.getCriteriaBuilder().literal("YYYY-MM-DD")),
                        ofNullable(input.getArguments().getFirst()).map(Object::toString).orElse(""),
                        ofNullable(input.getArguments().get(1)).map(Object::toString).orElse("")
                )
        );
    }
}
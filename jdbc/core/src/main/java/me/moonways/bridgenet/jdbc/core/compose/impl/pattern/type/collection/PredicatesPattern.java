package me.moonways.bridgenet.jdbc.core.compose.impl.pattern.type.collection;

import lombok.*;
import me.moonways.bridgenet.jdbc.core.compose.CombinedStructs;
import me.moonways.bridgenet.jdbc.core.compose.ConditionBinder;
import me.moonways.bridgenet.jdbc.core.compose.ConditionMatcher;
import me.moonways.bridgenet.jdbc.core.compose.template.collection.PredicatesTemplate;
import me.moonways.bridgenet.jdbc.core.compose.template.completed.CompletedPredicates;

import java.util.LinkedList;

@ToString
public class PredicatesPattern implements PredicatesTemplate, CompletedPredicates {

    private final LinkedList<CompletedPredicateNodePattern> list = new LinkedList<>();
    private ConditionBinder lastBinder = ConditionBinder.WHERE;

    @Override
    public CompletedPredicates combine() {
        return this;
    }

    @Override
    public PredicationAgent ifEqual(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.EQUALS);
    }

    @Override
    public PredicationAgent ifMatches(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.LIKE_IS);
    }

    @Override
    public PredicationAgent ifMoreThen(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.MORE);
    }

    @Override
    public PredicationAgent ifLessThen(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.LESS);
    }

    @Override
    public PredicationAgent ifMoreOrEqual(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.MORE_OR_EQUAL);
    }

    @Override
    public PredicationAgent ifLessOrEqual(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.LESS_OR_EQUAL);
    }

    @Override
    public PredicationAgent ifInside(CombinedStructs.CombinedField field) {
        return new PredicationAgentPattern(field, ConditionMatcher.INSIDE);
    }

    @Override
    public PredicationAgent isNull(CombinedStructs.CombinedLabel label) {
        return new PredicationAgentPattern(CombinedStructs.CombinedField.builder()
                .label(label.getLabel()).build(), ConditionMatcher.IS);
    }

    @Override
    public CompletedPredicateNode first() {
        return list.getFirst();
    }

    @RequiredArgsConstructor
    private class PredicationAgentPattern implements PredicationAgent {

        private final CombinedStructs.CombinedField field;
        private final ConditionMatcher matcherSign;

        private PredicatesTemplate bindOn(ConditionBinder binder) {
            if (binder == ConditionBinder.WHERE && list.stream().anyMatch(pattern -> pattern.current.binder == ConditionBinder.WHERE)) {
                throw new IllegalArgumentException("bind() must be used only 1 times");
            }

            var condition = InternalCondition.builder()
                    .binder(binder)
                    .field(field)
                    .matcherSign(matcherSign)
                    .build();

            var pattern = new CompletedPredicateNodePattern(condition);
            list.add(pattern);

            return PredicatesPattern.this;
        }

        @Override
        public PredicatesTemplate bind() {
            return bindOn(lastBinder);
        }

        @Override
        public PredicatesTemplate or() {
            var that = bindOn(lastBinder);
            lastBinder = ConditionBinder.OR;

            return that;
        }

        @Override
        public PredicatesTemplate and() {
            var that = bindOn(lastBinder);
            lastBinder = ConditionBinder.AND;

            return that;
        }
    }

    @ToString
    @AllArgsConstructor
    private class CompletedPredicateNodePattern implements CompletedPredicateNode {

        private InternalCondition current;

        @Override
        public CompletedPredicateNode poll() {
            var pattern = list.pollFirst();

            if (pattern != null) {
                current = pattern.current;
            }

            return pattern;
        }

        @Override
        public CombinedStructs.CombinedField field() {
            return current != null ? current.field : null;
        }

        @Override
        public ConditionMatcher matcher() {
            return current != null ? current.matcherSign : null;
        }

        @Override
        public ConditionBinder binder() {
            return current != null ? current.binder : null;
        }
    }

    @Builder
    @ToString
    private static class InternalCondition {

        private final CombinedStructs.CombinedField field;
        private final ConditionMatcher matcherSign;
        private final ConditionBinder binder;
    }
}

package com.ideax.sm;

import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author xinrd.xu
 * @version 15/11/23
 */
public class StateManagerBuilder<S extends State, E extends Action> {

    private DefaultStateManager<S, E> defaultStateManager = new DefaultStateManager<S, E>();

    public static <S extends State, E extends Action> StateManagerBuilder<S, E> create() {
        return new StateManagerBuilder<>();
    }

    public StateManagerBuilder<S, E> init(E action, S toState) {
        defaultStateManager.init(action, toState);
        return this;
    }

    public StateManagerBuilder<S, E> addTransition(E action, S fromState, S toState) {
        defaultStateManager.addTransition(action, fromState, toState);
        return this;
    }

    public StateManagerBuilder<S, E> addTransactionSupport(TransactionTemplate template) {
        defaultStateManager.setTransactionTemplate(template);
        return this;
    }

    public StateManager<S, E> build() {
        check(defaultStateManager);
        return defaultStateManager;
    }

    private void check(DefaultStateManager defaultStateManager) {
        // TODO check
    }
}

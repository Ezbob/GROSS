package elements

import elements.conflictHandler.ConflictChecker
import elements.conflictHandler.TwoWatchChecker

class AssertionTrail {
    List<Literal> trailer = []
    int lastDecisionLevel = 0

    void assertLiteral(Literal literal, boolean isDecision, ConflictChecker checker, PropagationQueue queue) {
        literal.isDecision = isDecision
        Utils.printTrace("Asserting literal: $literal")
        addToTrail(literal)

        if ( checker instanceof TwoWatchChecker) {
            checker.notifyWatches(literal.opposite(), this, queue)
        }
    }

    void addToTrail(Literal literal) {
        if ( literal.isDecision ) {
            this.lastDecisionLevel++
        }
        this.trailer = [*trailer, literal.clone()] as List<Literal>
    }

    void removeToLevel( Integer level ) {
        this.lastDecisionLevel = level
        this.trailer = prefixToLevel(level)
    }

    Literal getAt(int i) {
        trailer[i]
    }

    int getCurrentDecisionLevel() {
        lastDecisionLevel
    }

    List<Literal> decisionsTo(Literal literal) {
        List<Literal> result = []
        for (Literal lit in trailer) {
            if (lit.isDecision) {
                result << lit
            }
            if (lit == literal) {
                break
            }
        }
        result
    }

    List<Integer> getSolution() {
        trailer.collect { it.value }
    }

    void reset() {
        lastDecisionLevel = 0
        trailer = []
    }

    Integer levelTo(Literal literal) {
        decisionsTo(literal).size() // decision levels startSolving at zero ?
    }

    Integer maxLevel(Clause clause) {
        levelTo(lastAssertedLiteral(clause))
    }

    List<Literal> prefixToLevel(int decisionLevel) {
        int currentDecisionLevel = 0
        trailer.takeWhile {
            if (it.isDecision) {
                currentDecisionLevel++
            }
            currentDecisionLevel <= decisionLevel // this is what gets me in trouble (sometimes)
        }
    }

    Literal lastAssertedLiteral(Clause clause) {
        if (clause.literals.val.empty) {
            return null
        }
        for (int i = trailer.size() - 1; i >= 0; --i) {
            if (trailer[i] in clause.literals.val) {
                return trailer[i]
            }
        }
        null
    }

    String toString() {
        this.trailer
    }
}

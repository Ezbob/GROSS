package elements

import elements.clauseLearning.ClauseLearner
import elements.conflictHandler.ConflictChecker
import elements.conflictHandler.TwoWatchChecker
import elements.literalSelector.LiteralSelector
import elements.literalSelector.RandomLiteralSelector

class SpaceSearcher {

    LiteralSelector selector
    AssertionTrail trail
    ConflictChecker checker
    ClauseLearner clauseLearner
    Integer cThreshold = 100
    Double cFactor = 0.95
    Long seed = null

    SpaceSearcher(AssertionTrail trail, ConflictChecker checker,
                  ClauseLearner clauseLearner, Integer numberOfVariables) {
        init(trail, checker, clauseLearner)
        this.selector = new RandomLiteralSelector(trail, numberOfVariables)
    }

    SpaceSearcher(AssertionTrail trail, ConflictChecker checker, ClauseLearner clauseLearner,
                  Integer numberOfVariables, Long seed) {
        init(trail, checker, clauseLearner)
        this.selector = new RandomLiteralSelector(trail, numberOfVariables, seed)
        this.seed = seed
    }

    def init(AssertionTrail trail, ConflictChecker checker,
             ClauseLearner clauseLearner) {
        this.trail = trail
        this.checker = checker
        this.clauseLearner = clauseLearner
    }

    def applyDecide(PropagationQueue propagationQueue, Map stats) {
        stats["decisions"]++
        Literal lit = selector.selectLiteral()
        Utils.printTrace("apply decide: $lit\t")
        trail.assertLiteral(lit, true, checker, propagationQueue)
    }

    def applyBackJump(PropagationQueue propagationQueue) {
        Utils.printTrace "Goto: ${clauseLearner.lastAsserted} at level $backJumpLevel"
        Literal lastAsserted = clauseLearner.lastAsserted

        trail.removeToLevel backJumpLevel
        propagationQueue.reset()

        if (backJumpLevel == 0 && checker instanceof TwoWatchChecker) {
            // add implied single literal clauses
            checker.assignSingleLiteralClauses(trail)
        }

        trail.assertLiteral lastAsserted.opposite(), false, checker, propagationQueue

        if ( checker instanceof TwoWatchChecker ) {
            checker.reasons[(-lastAsserted.value)] = clauseLearner.backJumpClause
            checker.resetConflictFlag()
        }
        clauseLearner.backJumpClause = null
    }

    int getBackJumpLevel() {
        if (!clauseLearner.lowerLiterals.empty) {
            return trail.maxLevel(new Clause(clauseLearner.lowerLiterals).opposite())
        }
        return 0
    }

    // fun restart heuristic borrowed from Yices2 sat solver
    boolean shouldRestart(Map internalBookkeeping) {
        internalBookkeeping.conflictsBeforeRestart == cThreshold
    }

    def restart(PropagationQueue queue, Map stats, Map internalBookkeeping) {
        stats["restarts"]++
        internalBookkeeping.conflictsBeforeRestart = 0
        trail.reset()
        queue.queue.clear()
        cThreshold *= cFactor

        if (checker instanceof TwoWatchChecker) {
            checker.assignSingleLiteralClauses(trail)
        }
    }
}

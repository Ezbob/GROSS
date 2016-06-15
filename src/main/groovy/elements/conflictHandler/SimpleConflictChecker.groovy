package elements.conflictHandler

import SATUtils.SolutionValidator.SolutionException
import elements.AssertionTrail
import elements.Clause
import elements.Formula

class SimpleConflictChecker implements ConflictChecker {

    @Override
    boolean hasConflict(Formula formula, AssertionTrail trail) {
        formula.clauses.any { isClauseFalse(it, trail) }
    }

    @Override
    void preprocessChecker(Formula formula) {
        def clauseCount = formula.clauses.size()
        if (clauseCount != formula.numberOfClauses) {
            throw new SolutionException("Too many clauses")
        }
    }
// clause is only false iff all literals are false in the trail
    // clause is not false iff any literals are true or undefined
    private boolean isClauseFalse(Clause clause, AssertionTrail trail) {
        return !clause.literals.val.any { it in trail.trailer || !(it.opposite() in trail.trailer) }
    }
}

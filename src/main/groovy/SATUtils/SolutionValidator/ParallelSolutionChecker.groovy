package SATUtils.SolutionValidator

import elements.Clause
import elements.Formula
import groovyx.gpars.GParsPool

class ParallelSolutionChecker implements SolutionChecker {

    private throwOrPrint(String message, Boolean stopOnError) {
        if (stopOnError) {
            throw new SolutionException(message)
        } else {
            System.err.println message
        }
    }

    @Override
    Boolean validateFormula(Formula formula, List<Integer> solution, Boolean stopOnError = true) {
        if (solution == null) {
            throwOrPrint("Solution timed out", stopOnError)
        }

        if (solution != []) {
            if (formula.numberOfVariables != solution.size()) {
                throwOrPrint("Number of elements in solution and formula does not match", stopOnError)
                return false
            }

            if (formula.numberOfVariables != solution.unique().size()) {
                throwOrPrint("Solution has duplicate entries", stopOnError)
                return false
            }

            GParsPool.withPool {
                Clause clause
                if ( (clause = formula.clauses.findAnyParallel { isClauseFalse(it, solution) }) ) {
                    throwOrPrint("False clause detected: ${clause} ", stopOnError)
                }
            }
        }
        true
    }

    private Boolean isClauseFalse(Clause clause, List<Integer> solution) {
        return !clause.literals.val.any { it.value in solution }
    }
}

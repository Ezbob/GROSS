package SATUtils.SolutionValidator

import elements.Clause
import elements.Formula

class SimpleSolutionChecker implements SolutionChecker {

    private throwOrPrint(String message, Boolean stopOnError) {
        if (stopOnError) {
            throw new SolutionException(message)
        } else {
            System.err.println message
        }
    }

    @Override
    Boolean validateFormula(Formula formula, List<Integer> solution, Boolean stopOnError = true) {
        if (!solution) {
            throwOrPrint("Solution was unsatisfiable", stopOnError)
            return false
        }

        if (formula.numberOfVariables != solution.size()) {
            throwOrPrint("Number of elements in solution and formula does not match", stopOnError)
            return false
        }

        if (formula.numberOfVariables != solution.unique().size()) {
            throwOrPrint("Solution has not unique", stopOnError)
            return false
        }

        for (int i = 0; i < formula.clauses.size(); ++i) {
            def current = formula[i]
            if ( isClauseFalse(current, solution) ) {
                throwOrPrint("False clause detected: $i, ${current}", stopOnError)
                return false
            }
        }
        true
    }

    private Boolean isClauseFalse(Clause clause, List<Integer> solution) {
        return !clause.literals.val.any { it.value in solution }
    }

}

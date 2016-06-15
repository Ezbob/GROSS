package SATUtils.SolutionValidator

import elements.Formula

interface SolutionChecker {
    Boolean validateFormula(Formula formula, List<Integer> solution, Boolean stopOnError)
}
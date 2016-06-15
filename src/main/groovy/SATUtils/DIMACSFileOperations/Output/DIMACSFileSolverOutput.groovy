package SATUtils.DIMACSFileOperations.Output

import groovy.transform.InheritConstructors

@InheritConstructors
class DIMACSFileSolverOutput extends DIMACSFileOutput {

    private final int DEFAULT_LINEBREAK = 20
    private String SOLVER_ACRONYM = "GROSS"
    private String SOLVER_FULL_NAME = "GROovy Satisfiability Solver"
    private String LICENSE = "MIT"
    int solutionLineBreak = DEFAULT_LINEBREAK
    Map<String, Long> stats
    Boolean writeFooter = true

    @Override
    void write(int literals, List solution) {
        writeHeadSignature(0, literals)
        output << sLineString(!solution.empty) << "\n"
        output << vLineString(solution as List<Integer>) << "\n"
        if (writeFooter) { writeFootSignature() }
    }

    private String vLineString(List<Integer> solution) {
        String output = "v "
        solution.eachWithIndex { literal, index ->
            if ((index + 1) % solutionLineBreak == 0) {
                output <<= "\nv "
            }
            output <<= "$literal "
        }
        output
    }

    private String sLineString(boolean isSatisfiable) {
        isSatisfiable ? "s SATISFIABLE" : "s UNSATISFIABLE"
    }

    void setSolutionLineBreak(int newValue) {
        this.solutionLineBreak = newValue == 0 ? DEFAULT_LINEBREAK : newValue.abs()
    }

    @Override
    protected void writeHeadSignature(int numberOfClauses, int numberOfLiterals) {
        output << """\
c ---------
c SAT solving output produced by ${SOLVER_ACRONYM}, the ${SOLVER_FULL_NAME}
c Written by Anders Busch (2016)
c Based on ideas from the paper "Formalization and Implementation of Modern SAT Solvers" by Filip Marić
c Licensed under the ${LICENSE} license
c Output produced at ${new Date().format("HH:mm:ss dd-MM-yyyy")}
c ---------
"""
    }

    protected void writeFootSignature() {
        output << "c --STATS--\n"
        stats.each {
            output << "c ${it.key.capitalize()}: ${it.value}\n"
        }
        output << "c ---------\n"
    }

    void leftShift(List<Integer> solution, Map stats) {
        this.stats = stats
        write(0, solution)
    }

    public void setHeaderInfo( String license, String solverAcronym, String solverFullname ) {
        SOLVER_ACRONYM = solverAcronym
        SOLVER_FULL_NAME = solverFullname
        LICENSE = license
    }
}

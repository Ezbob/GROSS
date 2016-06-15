package SATUtils.DIMACSFileOperations.Output

import groovy.transform.InheritConstructors

@InheritConstructors
class DIMACSFileEncodingOutput extends DIMACSFileOutput {

    protected void writeHeadSignature(int numberOfClauses, int numberOfLiterals) {
        output << """\
c ----------------------------------------------------------
c CNF encoding generated for SAT at ${new Date().format("yyyyMMddHHmmss")}
c ----------------------------------------------------------
p cnf $numberOfLiterals $numberOfClauses\n"""
    }

    @Override
    void write(int literals, List cnfFormula) {
        int numberOfClauses = cnfFormula.size()
        writeHeadSignature(numberOfClauses, literals)

        cnfFormula.each {
            it.each {
                output << "$it "
            }
            output << "0\n"
        }
        output << "\n"
    }

    void leftShift(int literals, List<List<Integer>> cnfFormula) {
        write(literals, cnfFormula)
    }
}

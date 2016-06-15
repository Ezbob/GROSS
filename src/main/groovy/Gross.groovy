import SATUtils.SolutionValidator.SolutionChecker
import elements.Formula
import elements.Solver
import SATUtils.DIMACSFileOperations.DIMACSFileCNFInput
import SATUtils.DIMACSFileOperations.Output.DIMACSFileSolverOutput
import SATUtils.ExternalSATSolver.ExternalSolver
import SATUtils.SolutionValidator.ParallelSolutionChecker

import java.nio.file.NoSuchFileException

class Gross {

    static private OptionAccessor options
    static final private String DEFAULT_FILE_OUT = "out.cnf"

    static private File inputFile
    static private Formula inputFormula
    static private SolutionChecker checker
    static private Solver solver
    static private ExternalSolver eSolver

    static void main(String... args) {

        argumentHandler( args )
        init()

        if ( options.e ) {
            runExternalSolver()
        } else if ( options.t ) {
            runExternalSolver(true)
        }

        def startTime = System.nanoTime()
        def localSolution = solver.solve( inputFormula )
        def endTime = System.nanoTime()

        if ( options.t ) {
            println "GROSS: Time spent solving: ${ (endTime - startTime) / (1000 ** 3) } secs"
            return
        } else {

            localSolution.sort { it.abs() }

            if ( !options.s ) {

                if ( !options.d ) {
                    println "GROSS solution: ${ localSolution }"
                }
                println "Time spent solving (GROSS solver): ${ (endTime - startTime) / (1000 ** 3) } secs"

                solver.stats.each {
                    println "Number of ${it.key}: ${it.value} "
                }
            }

            String fileName
            if ( options.o ) {
                fileName = options.o.trim()
                new DIMACSFileSolverOutput( fileName ) << [ localSolution, solver.stats ]
            } else {
                fileName = DEFAULT_FILE_OUT
                new DIMACSFileSolverOutput( fileName ) << [ localSolution, solver.stats ]
            }

            if (!options.s) {
                println("Solution written to ${fileName}")
            }

            if ( options.c ) {
                if ( localSolution ) {
                    print("Checking solution...")
                    Boolean doesHold = checker.validateFormula( inputFormula, localSolution, false )
                    if ( doesHold ) {
                        println("solution holds.")
                    } else {
                        println("solution does NOT hold.")
                    }
                }
            }
        }
    }

    static void runExternalSolver(Boolean timeOnly = false) {
        if ( timeOnly ) {
            eSolver.solve(inputFile, true, false)
        } else {
            if ( !options.s ) {
                println "Running external solver: ${eSolver.solver}..."
            }
            List solution = eSolver.solve(inputFile, true, false)
            if ( !options.d || !options.s ) {
                println "${eSolver.solver} solution: $solution"
            }
            if ( !options.s ) {
                println "done."
            }
        }
    }

    static void argumentHandler( String... args ) {

        def cli = new CliBuilder(usage: "./grossArt -[hscet] -[p|-set-external] <solver_path> -[i|-input] <inputFile> -[o|-output] <outputFile>")

        cli.with {
            h longOpt: "help", 'Show usage information'
            i longOpt: "input", args: 1, argName: 'input file', 'Required: Solve SAT using DIMACS CNF "input file"'
            o longOpt: "output", args: 1, argName: 'output file', 'Solution output CNF file'
            s longOpt: "silent", 'Run in silent mode'
            c longOpt: "validate", 'Check against input formula if satisfied output is valid'
            e longOpt: "external", 'Run external solver first and then run GROSS'
            p longOpt: "set-external", 'Set which external solver to use'
            t longOpt: "time-only", "Run in time comparison mode to compare external solver with GROSS. Does not write solutions to disk."
            d longOpt: "no-solution", "Don't show solution vector in stdout"
        }

        options = cli.parse(args)

        if ( !options ) {
            System.exit(1)
        }

        if ( !args || !options.i || options.h ) {
            println "GROSS Satisfiability Solver"
            cli.usage()
            System.exit(1)
        }
    }


    static void init() {
        inputFile = new File( options.i.trim() as String )
        if ( !inputFile.exists() ) {
            throw new NoSuchFileException("Input file not found")
        }
        inputFormula = new DIMACSFileCNFInput( inputFile ).parseSolverFormula()
        if ( !options.s ) {
            println "Input Formula has ${inputFormula.numberOfClauses} clauses, ${inputFormula.numberOfLiterals} literals and ${inputFormula.numberOfVariables} variables."
        }
        checker = new ParallelSolutionChecker()
        solver = new Solver()
        if ( options.ep ) {
            eSolver = new ExternalSolver(solver: options.ep.trim())
        } else {
            eSolver = new ExternalSolver()
        }
    }
}

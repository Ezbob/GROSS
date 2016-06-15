package SATUtils.ExternalSATSolver

import java.util.concurrent.TimeUnit

class ExternalSolver {

    String solver = "lingeling"
    List<String> args
    long timeOut = 10 // minutes
    Boolean isSatisfiable = false

    private String resolveArguments(String inputPath) {
        if ( args && args.size() > 0 ) {
            String argLine = ""
            args.each { argLine <<= "$it " }
            return "$solver $argLine$inputPath"
        } else {
            return "$solver $inputPath"
        }
    }

    List<Integer> solve(String fileName, boolean printTime = false, boolean verbose = false) {
        solve(new File(fileName), printTime, verbose)
    }

    List<Integer> solve(File input, boolean printTime = false, boolean verbose = false) {
        def command = resolveArguments(input.absolutePath)
        def startTime = System.nanoTime()
        def process = command.execute()

        List result = null
        String processOutput
        if ( process.waitFor(timeOut, TimeUnit.MINUTES) ) {
            def endTime = System.nanoTime()
            if ( (processOutput = process.err.text) ) {
                if (verbose) {
                    System.err.println processOutput
                }
            } else {
                processOutput = process.text
                if (verbose) {
                    println processOutput
                }
            }
            if (printTime) {
                println "${solver}: Time spent solving: ${(endTime - startTime) / (1000 ** 3)} secs"
            }
            result = analyseOutput(processOutput)
        }
        if ( result == null && printTime ) {
            System.err.println "$solver: Timed out."
        }
        process.destroy()
        result
    }

    private List<Integer> analyseOutput(String output) {
        List<Integer> result = []
        output.eachLine {
            if ( it.startsWith('s') && it.contains(' SAT') ) {
                this.isSatisfiable = true
            } else if ( it.startsWith('v') ) {
                result.addAll ( (it.split(' ').drop(1)).findAll {it.toInteger() != 0}*.toInteger() )
            }
        }
        result
    }
}

package SATUtils.DIMACSFileOperations

import elements.Clause
import elements.Formula

class DIMACSFileCNFInput {

    private String inputFile
    private BufferedReader lineReader
    int numberOfVariables = 0
    int numberOfClauses = 0
    int numberOfLiterals = 0
    String problem = ""

    DIMACSFileCNFInput(String fileName) {
        init( new File(fileName) )
    }

    DIMACSFileCNFInput(File file) {
        init( file )
    }

    String getFileName() {
        this.inputFile
    }

    private init(File file) {
        if ( !file.exists() ) {
            throw new FileNotFoundException()
        }
        this.lineReader = file.newReader()
        this.inputFile = file.absolutePath
    }

    private resetReader() {
        this.lineReader.reset()
    }

    private HashSet<Integer> parseIntegerLine(String line) {
        HashSet result = []
        for ( int i = 0; i < line.size() - 1; ++i ) {
            StringBuilder numb = new StringBuilder()
            while ( line[i] != " " && line[i] != "\n" ) {
                numb << line[i]
                i += 1
            }
            if ( numb.size() > 0 && numb.toString() != "0" ) {
                result << Integer.parseInt(numb.toString())
                this.numberOfLiterals++
            }
        }
        result
    }

    private List<String> split(String problemClause) {
        List<String> splitting = []
        for ( int i = 0; i < problemClause.length(); ++i ) {
            StringBuilder tokenCollector = new StringBuilder()
            while ( i < problemClause.length() && problemClause[i] != " " ) {
                tokenCollector << problemClause[i]
                i += 1
            }
            splitting << tokenCollector.toString()
        }
        splitting
    }

    Formula parseSolverFormula() {

        HashSet<Clause> elements = []
        Integer clausesRead = 0

        lineReader.eachLine { String line ->
            def trimmed = line.trim()
            if ( trimmed ) {
                switch (trimmed[0]) {
                    case "c":
                        break
                    case "p":
                        List<String> splitting = split(line)
                        this.numberOfClauses = Integer.parseInt(splitting[3])
                        this.numberOfVariables = Integer.parseInt(splitting[2])
                        this.problem = splitting[1]
                        break
                    default:
                        elements << Clause.readClause(parseIntegerLine(line))
                        clausesRead++
                        break
                }
            }
        }
        if (clausesRead != numberOfClauses) {
            throw new Exception("Number of clauses and number of clauses read does not match")
        }
        new Formula( elements, numberOfClauses, numberOfVariables, numberOfLiterals, fileName)
    }

}

package SATUtils.DIMACSFileOperations.Output

abstract class DIMACSFileOutput {
    protected File output

    DIMACSFileOutput(String fileName) {
        output = new File(fileName)
        if (output.exists()) {
            output.setText("")
        }
    }

    DIMACSFileOutput(File file) {
        output = file
        if (output.exists()) {
            output.setText("")
        }
    }

    String getFileName() {
        output.absolutePath
    }

    abstract void write(int literals, List output)
    abstract protected void writeHeadSignature(int numberOfClauses, int numberOfLiterals)
}

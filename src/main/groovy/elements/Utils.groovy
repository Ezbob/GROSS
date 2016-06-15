package elements

class Utils {
    static Boolean trace = false

    static void printTrace(String message) {
        if (trace) {
            println message
        }
    }
}
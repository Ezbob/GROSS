package elements

import elements.conflictHandler.ConflictChecker


class PropagationQueue {
    LinkedHashSet<Literal> queue = []

    def applyUnitPropagation(ConflictChecker checker, AssertionTrail trail, Map stats) {
        def nextToBeAsserted = poll()
        if ( nextToBeAsserted ) {
            stats["propagations"]++
            Utils.printTrace("asserting unit: $nextToBeAsserted \t")
            trail.assertLiteral(nextToBeAsserted, false, checker, this)
        }
    }

    def exhaustiveUnitPropagation(Formula formula, ConflictChecker checker, AssertionTrail trail, Map stats) {
        while (true) {
            applyUnitPropagation(checker, trail, stats)
            if (queue.isEmpty() || checker.hasConflict(formula, trail)) {
                break
            }
        }
    }

    Literal poll() {
        if ( queue.empty ) {
            return null
        }
        def head = queue.head()
        queue.removeElement(head)
        head
    }

    def reset() {
        queue.clear()
    }

    int size() {
        queue.size()
    }

    def add(Literal literal) {
        queue << literal
    }

    String toString() {
        queue
    }
}

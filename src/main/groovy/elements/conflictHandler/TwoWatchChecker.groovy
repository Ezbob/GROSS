package elements.conflictHandler

import elements.Clause
import elements.Formula
import elements.Literal
import elements.AssertionTrail
import elements.PropagationQueue

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The two-watch literal scheme allows for efficient checking whether any clause is false in a formula,
 * and detection of unit clauses which will be used in unit propagation.
 * If one of the two watched literals of a gets set to false (e.g. falsified) then we know that
 */
class TwoWatchChecker implements ConflictChecker {

    private final Map<Integer, HashSet<Clause>> watchMap = [:].withDefault { [].asSynchronized() }.asSynchronized()
    private ConcurrentLinkedQueue<Tuple2<Integer, Clause>> learnedClauses = new ConcurrentLinkedQueue<>()
    Map<Integer, Clause> reasons = [:]
    List<Clause> singleLiteralClauses = []
    private boolean hasConflict = false
    Clause conflictClause = null

    void preprocessChecker(Formula formula) {

        formula.clauses.each { Clause clause ->
            if ( clause.literals.val.size() >= 1 ) { // has at least one literal
                def firstList = watchMap[(clause.firstWatched.value)]
                if (!(clause in firstList)) {
                    firstList << clause
                }

                if ( clause.literals.val.size() >= 2 ) { // has at least two literals
                    def secondList = watchMap[(clause.secondWatched.value)]

                    if (!(clause in secondList)) {
                        secondList << clause
                    }
                } else {
                    singleLiteralClauses << clause
                }
            }
        }
    }

    void assignSingleLiteralClauses(AssertionTrail trail) {
        for (Clause clause in singleLiteralClauses) {
            if ( !(clause.firstWatched in trail.trailer) ) {
                trail.addToTrail(clause.firstWatched)
            }
        }
    }

    void updateWatch(Clause clause, Literal literal, boolean isSecond = true) {
        if (isSecond) {
            clause.secondWatched = literal
            watchMap[(clause.secondWatched.value)] << clause
        } else {
            clause.firstWatched = literal
            watchMap[(clause.firstWatched.value)] << clause
        }
    }

    // add all learned clauses send by the sharer (running it's own thread) to the watchMap
    private void synchronizeMaps() {
        Tuple2<Integer, Clause> entry
        while ( (entry = learnedClauses.poll()) ) {
            watchMap[(entry.first)] << entry.second
        }
    }

    void notifyWatches(Literal literal, AssertionTrail trail, PropagationQueue queue) {
        HashSet<Clause> newWatchList = new HashSet<>()
        synchronizeMaps()
        watchMap[(literal.value)].each { Clause clause ->
            if (clause.firstWatched == literal) {
                clause.swapWatches()
            }
            if (clause.firstWatched in trail.trailer) {
                newWatchList << clause
            } else {
                Literal literalMark = clause.literals.val.find { Literal lit ->
                    lit != clause.firstWatched && lit != clause.secondWatched && !(lit.opposite() in trail.trailer)
                }
                if (literalMark) {
                    updateWatch(clause, literalMark)
                } else if (clause.firstWatched.opposite() in trail.trailer) {
                    conflictClause = clause
                    hasConflict = true
                    newWatchList << clause
                } else {
                    queue.add(clause.firstWatched)
                    reasons[(clause.firstWatched.value)] = clause
                    newWatchList << clause
                }
            }
        }
        watchMap[(literal.value)] = newWatchList
    }

    void resetConflictFlag() {
        this.hasConflict = false
    }

    void register(Clause newClause) {
        watchMap[(newClause.firstWatched.value)] << newClause
        watchMap[(newClause.secondWatched.value)] << newClause
    }

    void share(Clause clause) {
        learnedClauses << new Tuple2<Integer,Clause>(clause.firstWatched.value, clause)
        learnedClauses << new Tuple2<Integer,Clause>(clause.secondWatched.value, clause)
    }

    @Override
    boolean hasConflict(Formula formula, AssertionTrail trail) {
        hasConflict
    }

}
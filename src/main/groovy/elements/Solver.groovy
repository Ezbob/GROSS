package elements

import elements.clauseLearning.ClauseLearner
import elements.clauseLearning.ClauseSharerActor
import elements.conflictHandler.ConflictChecker
import elements.conflictHandler.TwoWatchChecker
import org.pmw.tinylog.Logger

class Solver {

    enum SATSituation { UNDEFINED, UNSAT, SAT }

    protected Formula formula
    protected AssertionTrail trail
    protected ConflictChecker checker
    protected PropagationQueue propagationQueue
    protected ClauseLearner clauseLearner
    protected SpaceSearcher searcher
    ClauseSharerActor sharerActor
    Long id

    Map<String, Long> stats = [
            "conflicts" : 0,
            "decisions" : 0,
            "propagations" : 0,
            "learned clauses" : 0,
            "restarts" : 0
    ]

    Map internalBookkeeping = [
            "conflictsBeforeRestart": 0
    ]

    ConflictChecker getChecker() {
        this.checker
    }

    Solver() {
        this.trail = new AssertionTrail()
        this.propagationQueue = new PropagationQueue()
        this.checker = new TwoWatchChecker()
    }

    Solver(Long id, ConflictChecker checker, ClauseSharerActor sharerActor) {
        this.trail = new AssertionTrail()
        this.propagationQueue = new PropagationQueue()
        this.checker = checker
        this.sharerActor = sharerActor
        this.sharerActor.start()
        this.id = id
    }

    private void initSolver( Formula formula ) {
        this.formula = formula
        this.clauseLearner = new ClauseLearner( stats, internalBookkeeping, checker, trail, sharerActor )
        this.searcher = this.id ? new SpaceSearcher( trail, checker, clauseLearner, formula.numberOfVariables, id ) :
                new SpaceSearcher( trail, checker, clauseLearner, formula.numberOfVariables )
        this.clauseLearner.searcher = this.searcher
    }

    List<Integer> solve(Formula formula, Boolean trace = false) {
        SATSituation flag = SATSituation.UNDEFINED
        Utils.trace = trace
        initSolver(formula)
        checker.preprocessChecker(formula)
        if (checker instanceof TwoWatchChecker) {
            checker.assignSingleLiteralClauses trail
        }

        List<Integer> result = []
        if ( searcher.seed ) {
            Utils.printTrace "Using id: ${searcher.seed}"
        }

        while ( flag == SATSituation.UNDEFINED ) {
            if ( checker instanceof TwoWatchChecker ) {
                propagationQueue.exhaustiveUnitPropagation formula, checker, trail, stats
            }
            if ( checker.hasConflict(formula, trail) ) {
                clauseLearner.applyConflict()
                if ( trail.currentDecisionLevel == 0 ) { // UNSAT
                    Utils.printTrace "top level conflict"
                    flag = SATSituation.UNSAT
                } else {
                    clauseLearner.applyExplainUIP()
                    clauseLearner.applyLearn( this.id )
                    searcher.applyBackJump propagationQueue
                    Utils.printTrace "back jump:\t\t $trail"
                }
            } else {
                if ( formula.numberOfVariables == trail.trailer.size() ) { // SAT
                    Utils.printTrace "Satisfiable: \t\t $trail"
                    result = trail.solution
                    flag = SATSituation.SAT
                } else {
                    if ( searcher.shouldRestart( internalBookkeeping ) ) {
                        searcher.restart propagationQueue, stats, internalBookkeeping
                    }
                    Utils.printTrace("Applying decide: \t\t $trail")
                    searcher.applyDecide propagationQueue, stats
                }
            }
        }
        result
    }
}

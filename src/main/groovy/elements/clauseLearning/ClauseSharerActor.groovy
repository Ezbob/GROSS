package elements.clauseLearning

import elements.Solver
import elements.Clause
import elements.conflictHandler.TwoWatchChecker
import groovyx.gpars.actor.DefaultActor

class ClauseSharerActor extends DefaultActor {

    List<Solver> solverList

    def shareClause(Long senderId, Clause clause) {
        for ( Solver solver in solverList ) {
            if (solver.id != senderId && solver.checker instanceof TwoWatchChecker) {
                (solver.checker as TwoWatchChecker).share( clause )
            }
        }
    }

    void act() {
        loop {
            react { Map message ->
                Long id = message.id
                Clause clause = message.clause
                shareClause id, clause
            }
        }
    }

}


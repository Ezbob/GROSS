package elements.clauseLearning

import elements.Clause
import groovyx.gpars.agent.Agent

/**
 * Data structure for handling multiple thread access
 */
class LearnedClauseAgent {
    Agent<HashSet<Clause>> learnedClauses = new Agent<>(new HashSet<Clause>())
    ClauseSharerActor clauseSharerActor
    Integer clauseShareLimit = 5

    LearnedClauseAgent(ClauseSharerActor clauseSharerActor) {
        this.clauseSharerActor = clauseSharerActor
    }

    def learn(Clause clause, Long id) {
        learnedClauses << { it << clause }
        if ( clauseSharerActor && clause.size() <= clauseShareLimit ) {
            clauseSharerActor.sendAndContinue( [id: id, clause: clause], { Utils.printTrace("learned $it") } )
        }
    }

    def forget(Clause clause) {
        learnedClauses << { it.removeElement(clause) }
    }

    def getAt(int i) {
        learnedClauses.val[i]
    }

    def isCase(Clause clause) {
        clause in learnedClauses.val
    }
}

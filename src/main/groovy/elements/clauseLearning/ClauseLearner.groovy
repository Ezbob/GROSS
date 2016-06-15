package elements.clauseLearning

import elements.AssertionTrail
import elements.Clause
import elements.Literal
import elements.SpaceSearcher
import elements.conflictHandler.ConflictChecker
import elements.conflictHandler.TwoWatchChecker
import elements.Utils


class ClauseLearner {
    Integer numberOfTopLevelLiterals = 0 // C_n
    Clause backJumpClause = null // C
    HashSet<Literal> lowerLiterals = null // C_P
    HashSet<Literal> clauseMap = null // C_H
    Literal lastAsserted = null // C_l
    LearnedClauseAgent learnedClauses
    SpaceSearcher searcher

    Map stats
    Map internalBookkeeping
    elements.conflictHandler.ConflictChecker checker
    AssertionTrail trail

    ClauseLearner(Map stats, Map internalBookkeeping, ConflictChecker checker, AssertionTrail trail, ClauseSharerActor sharer) {
        this.stats = stats
        this.checker = checker
        this.trail = trail

        this.internalBookkeeping = internalBookkeeping
        learnedClauses = new LearnedClauseAgent( sharer )
    }

    def applyLearn(Long id) {
        if ( !( this.backJumpClause in learnedClauses ) && this.backJumpClause.literals.val.size() > 1 ) {
            stats["learned clauses"]++
            learnedClauses.learn this.backJumpClause, id
            if (checker instanceof TwoWatchChecker) {
                checker.register backJumpClause
            }
        }
    }

    def findLastAsserted() {
        while ( true ) {
            lastAsserted = trail.trailer.pop()
            if (lastAsserted.opposite() in clauseMap) {
                break
            }
        }
    }

    def applyConflict() {
        lowerLiterals = []
        clauseMap = []
        numberOfTopLevelLiterals = 0
        stats["conflicts"]++
        internalBookkeeping.conflictsBeforeRestart++
        Utils.printTrace "Conflict\t"
        Clause conflictClause = (checker as TwoWatchChecker).conflictClause
        Utils.printTrace "Has conflict clause ${conflictClause}"

        for (Literal literal in conflictClause.literals.val) {
            addLiteral(literal)
        }
        findLastAsserted()
    }

    def applyExplain(Literal literal) {
        def reasonClause = (checker as TwoWatchChecker).reasons[(literal.value)]

        resolve reasonClause, literal
        findLastAsserted()
    }

    def applyExplainUIP() {
        //println "Last asserted $lastAsserted"
        while ( numberOfTopLevelLiterals != 1 && !lastAsserted.isDecision ) {
            applyExplain lastAsserted
        }
        buildBackJump()
    }

    def resolve(Clause clause, Literal literal) {
        Utils.printTrace "Resolving: ${literal}, with reason: $clause with ${trail}"
        removeCurrentLevel literal.opposite()
        for ( Literal literalMark in clause.literals.val ) {
            if ( literalMark != literal ) {
                addLiteral(literalMark)
            }
        }
        //println "Backjump is now: $clauseMap"
    }

    def buildBackJump() {
        HashSet<Literal> combine = new HashSet<>(lowerLiterals)
        combine.add lastAsserted.opposite()
        this.backJumpClause = new Clause(combine)
    }

    def addLiteral(Literal literal) {
        if (!(literal in clauseMap)) {
            Utils.printTrace "Adding literal: $literal \t $trail"
            clauseMap << literal
            if ( trail.levelTo(literal.opposite()) == trail.currentDecisionLevel ) {
                numberOfTopLevelLiterals++
            } else {
                lowerLiterals << literal
            }
        }
    }

    def removeCurrentLevel(Literal literal) {
        Utils.printTrace "Removing: $literal from $clauseMap"
        clauseMap.removeElement(literal)
        numberOfTopLevelLiterals--
    }

}

package elements

import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.Synchronized
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import groovyx.gpars.agent.Agent

import java.util.concurrent.locks.ReentrantReadWriteLock

final class Formula {

    private HashSet<Clause> clauses = []
    private Integer numberOfClauses
    private Integer numberOfVariables
    private Integer numberOfLiterals
    String file

    Formula(HashSet<Clause> clauses, Integer numberOfClauses,
            Integer numberOfVariables, Integer numberOfLiterals, String file) {
        this.clauses = clauses
        this.numberOfClauses = numberOfClauses
        this.numberOfVariables = numberOfVariables
        this.numberOfLiterals = numberOfLiterals
        this.file = file
    }

    @Synchronized
    Integer getNumberOfClauses() {
        this.numberOfClauses
    }

    @Synchronized
    Integer getNumberOfVariables() {
        this.numberOfVariables
    }

    @Synchronized
    Integer getNumberOfLiterals() {
        this.numberOfLiterals
    }

    @Synchronized
    HashSet<Clause> getClauses() {
        this.clauses.collect({it.clone()})
    }

    @Synchronized
    Clause getAt(int i) {
        this.clauses[i]
    }

    String toString() {
        this.clauses
    }
}


@AutoClone
@EqualsAndHashCode(excludes = ['firstWatched', 'secondWatched'])
class Clause {
    Agent<HashSet<Literal>> literals = new Agent<>(new HashSet<Literal>())
    private Literal firstWatched
    private Literal secondWatched

    def firstWriteLock = new ReentrantReadWriteLock()
    def secondWriteLock = new ReentrantReadWriteLock()

    Clause(HashSet<Literal> clause) {
        this.literals << { updateValue(clause) }
        this.firstWatched = literals.val[0]
        this.secondWatched = literals.val[1]
    }

    // using static method, because java can't distinguish between two same types with different subtype
    @Synchronized
    static Clause readClause(HashSet<Integer> rawUnorderedLiterals) {
        HashSet<Literal> hs = []
        rawUnorderedLiterals.each { hs << new Literal(value: it, isDecision: false) }
        new Clause(hs)
    }

    @Synchronized
    Literal getFirstWatched() {
        firstWatched ?: firstWatched.clone()
    }

    @Synchronized
    Literal getSecondWatched() {
        secondWatched ?: secondWatched.clone()
    }

    @WithWriteLock('secondWriteLock')
    void setSecondWatched(Literal literal) {
        this.secondWatched = literal
    }

    @WithWriteLock('firstWriteLock')
    void setFirstWatched(Literal literal) {
        this.secondWatched = literal
    }

    @Synchronized
    void size() {
        this.literals.val.size()
    }

    @Synchronized
    void swapWatches() {
        ( firstWatched, secondWatched ) = [ secondWatched, firstWatched ]
    }

    @Synchronized
    Clause opposite() {
        new Clause(literals.val.collect(new HashSet<Literal>()) { it.opposite() } as HashSet<Literal>)
    }

    String toString() {
        literals.val
    }
}

@AutoClone
@EqualsAndHashCode(excludes = ['isDecision'])
class Literal {
    Integer value
    boolean isDecision

    Integer asVariable() {
        this.value.abs()
    }

    Literal opposite() {
        new Literal(value: -value)
    }

    boolean isFalse() {
        this.value < 0
    }

    boolean isTrue() {
        this.value > 0
    }

    String toString() {
        isDecision ? "|$value" : value
    }

}


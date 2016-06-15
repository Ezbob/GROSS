package elements.literalSelector

import elements.AssertionTrail
import elements.Clause
import elements.Literal

class VSIDSLiteralSelector implements LiteralSelector {

    AssertionTrail trail
    Map<Literal, Long> VSIDSMap
    LiteralSelector tiebreaker
    Double decayFactor = 0.95

    VSIDSLiteralSelector(AssertionTrail trail, Integer numberOfVariables) {
        this.trail = trail
        this.VSIDSMap = [:].withDefault { 0L }
        this.tiebreaker = new RandomLiteralSelector(trail ,numberOfVariables)
    }

    void decay() {
        for (def entry in VSIDSMap) {
            Long newValue = entry.value
            newValue *= decayFactor
            entry.value = newValue
        }
    }

    Literal findMax() {
        def max = VSIDSMap.values().max()
        def maxima = VSIDSMap.findAll { it.value == max }
        if (!maxima || maxima.size() > 1) {
            return tiebreaker.selectLiteral()
        } else if (maxima.keySet()[0] in trail.trailer) {
            return tiebreaker.selectLiteral()
        } else {
            return maxima.keySet()[0]
        }
    }

    @Override
    Literal selectLiteral() {
        findMax()
    }

}

package elements.literalSelector

import elements.AssertionTrail
import elements.Literal

class RandomLiteralSelector implements LiteralSelector {

    private AssertionTrail trail
    private Random randomGenerator
    private Integer variables
    private Long seed

    public RandomLiteralSelector(AssertionTrail trail, int numberOfVariables, Long seed = null) {
        this.trail = trail
        this.randomGenerator = seed ? new Random(seed) : new Random()
        this.variables = numberOfVariables
        this.seed = seed
    }

    private nextLiteral() {
        def literalValue = (randomGenerator.nextInt(this.variables) + 1)

        // sign bit choosing
        if ( randomGenerator.nextBoolean() ) {
            return new Literal(value: -literalValue)
        } else {
            return new Literal(value: literalValue)
        }
    }

    Long getSeed() {
        seed
    }

    @Override
    Literal selectLiteral() {
        Literal literal = nextLiteral()
        while ( trail.trailer.any { it.asVariable() == literal.asVariable() } ) {
            // if the variable is in the trail draw a new literal
            literal = nextLiteral()
        }
        literal
    }
}

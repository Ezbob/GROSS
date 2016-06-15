package elements.conflictHandler

import elements.Formula
import elements.AssertionTrail

interface ConflictChecker {
    boolean hasConflict(Formula formula, AssertionTrail trail)
    void preprocessChecker(Formula formula)
}
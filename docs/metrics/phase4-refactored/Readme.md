# Phase 4 Refactored Metrics

- Tool: Lizard
- Version: 1.26.1
- Configuration: Default settings
- Git tag: phase4-refactored

## Commands Used

python -m lizard backend/src frontend/src
python -m lizard --csv backend/src frontend/src

Same tool version, configuration and analysis scope as phase3 and
phase4. Measured after the three refactorings (R1 to R3) and before
the PROMO module.

## Files

- lizard-output.txt: raw Lizard text output
- lizard-output.csv: raw Lizard CSV output

## Characterisation Tests

Run from the backend folder:

.\mvnw.cmd test "-Dtest=*CharacterisationTest"

Each refactoring's tests are in the commit just before it, and pass on
both that commit and the refactoring commit.

## Comparison

Run from the repository root:

python docs/metrics/compare.py

This prints the phase3, phase4-refactored and phase4 comparison for the
hotspot classes and the refactored methods.
# Phase 4 Metrics

- Tool: Lizard
- Version: 1.26.1
- Configuration: Default settings
- Git tag: phase4

## Commands Used

python -m lizard backend/src frontend/src
python -m lizard --csv backend/src frontend/src

Same tool version, configuration and analysis scope as phase3 and
phase4-refactored. Measured after adding the PROMO module.

## Files

- lizard-output.txt: raw Lizard text output
- lizard-output.csv: raw Lizard CSV output

## Comparison

Run from the repository root:

python docs/metrics/compare.py

This prints the phase3, phase4-refactored and phase4 comparison for the
hotspot classes and the classes added by PROMO.
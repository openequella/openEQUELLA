# Selenium Hotfix Regression Tests

This folder contains **Selenium regression tests** that are added after a **production bug / hotfix**.
The goal is to ensure the same issue **never regresses** (i.e., becomes broken again) in future changes.

## What belongs here

Add a test here when:
- A bug required a **hotfix** (or was high risk / high impact in production), and
- The safest long-term prevention is an **end-to-end UI flow** check.

## What does NOT belong here

Do **not** add tests here when:
- The issue can be covered by unit/integration tests reliably
- The scenario is just general UI coverage (put it in the feature test folder instead)

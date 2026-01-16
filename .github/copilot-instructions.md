# openEQUELLA Copilot Instructions

This file provides guidance for AI assistants and developers working with the openEQUELLA codebase.

## Project Overview

openEQUELLA is a digital repository platform that provides a single platform to house teaching and
learning, research, media, and library content. It is primarily developed and maintained
by [Edalex](https://edalex.com).

**Key Characteristics:**

- Runs on Java 21
- Runs within an embedded Tomcat server
- Supports Linux and Windows platforms
- Uses a hybrid architecture with both modern and legacy code
- Development primarily done in IntelliJ IDEA

## Technology Stack & Architecture

### Backend Technologies

**Languages:**

- **Scala** - Preferred for all new server-side code (Scala 2.13)
- **Java** - Used in legacy code and some specific contexts
- Mix of both throughout the codebase

**Frameworks & Libraries:**

- **Guice** - Primary dependency injection framework (use `@Inject` annotations)
- **Spring** - Used for supporting functions (less common than Guice)
- **JPF (Java Plugin Framework)** - Customized plugin system that structures the application into
  modules
- **Hibernate** - ORM for database persistence
- **JAX-RS** - REST endpoint definitions (use `@Path` annotations)
- **Apache Lucene** - File-based search index for content and ACL resolution

**Databases:**

- PostgreSQL (recommended for development)
- MS SQL Server
- Oracle

### Frontend Technologies

**New UI (Modern):**

- **TypeScript** - All new UI code
- **React** (v19.x) - UI framework
- **Material-UI (MUI)** (v7.x) - Component library (`@mui/material`)
- **fp-ts** - Functional programming utilities
- **io-ts** - Runtime type validation
- Located in `react-front-end/` directory
- REST communication handled by `oeq-ts-rest-api/` module

**Legacy UI:**

- **Sections Framework** - Bespoke Java-based web framework
- **Freemarker** - HTML templating (`.ftl` files)
- **Legacy JavaScript** - AJAX functionality in older pages
- Being gradually migrated to New UI

**Admin Console:**

- Java Swing application (standalone)
- Being migrated to New UI

### Build System

**Primary Build Tool: SBT**

- Main build configuration in `build.sbt`
- Extensive customization via SBT plugins in `project/` directory
- Custom AutoPlugins handle JPF-specific tasks:
  - `JPFScanPlugin` - Scans for plugin projects
  - `JPFPlugin` - Default settings for JPF plugins
  - `JPFRunnerPlugin` - Collects plugins for deployment/running
- Run `./sbt` from project root

**Frontend Build:**

- **NPM** - Package management (Node 24.11.0)
- **Parcel** - Bundler for TypeScript/React code
- Build commands in `react-front-end/package.json`

**JPF Plugins:**

- Each plugin has a `plugin-jpf.xml` descriptor file
- Defines plugin dependencies and exports
- SBT automatically discovers and processes these

## Code Quality & Style Standards

### General Principles

When writing or modifying code:

- Follow **Clean Code** principles
- Prefer **declarative** over imperative idioms
- Use **pragmatic functional programming** patterns where appropriate
- Leverage **modern language features** (Scala 2.13, Java 21, ES2020+)
- Match conventions in similar existing code
- Document complex logic with clear comments
- Add ScalaDoc/JavaDoc/JSDoc for public APIs

### Language-Specific Guidance

**Scala:**

- Preferred for all new server-side code
- Use functional programming patterns (immutability, pure functions, etc.)
- Leverage Scala collections and for-comprehensions
- Follow conventions in existing Scala code

**Java:**

- Use for specific contexts or when maintaining existing Java code
- Follow modern Java practices (streams, Optional, etc.)
- Consider refactoring to Scala when making substantial changes

**TypeScript:**

- Required for all new UI code
- Use strict type checking
- Leverage fp-ts for functional patterns
- Use io-ts for runtime type validation
- Prefer functional components with hooks in React

### Code Formatting

**All code must be formatted using these tools:**

| Language              | Tool               | Files                   | Configuration                |
|-----------------------|--------------------|-------------------------|------------------------------|
| Scala                 | ScalaFmt           | `*.scala`, `*.sbt`      | `.scalafmt.conf` (v3.10.3)   |
| Java                  | Google Java Format | `*.java`                | Run via `google-java-format` |
| TypeScript/JavaScript | ESLint + Prettier  | `*.ts`, `*.tsx`, `*.js` | `eslint.config.mjs`          |
| CSS/SCSS              | Prettier           | `*.css`, `*.scss`       | Prettier defaults            |

**Pre-commit Hooks:**

- Install with `npm ci` in project root
- Automatically formats staged files before commit
- Uses `husky` and `lint-staged` (see `package.json`)

**Manual Formatting:**

```bash
npm run format        # Format all code
npm run format:scala  # Scala only
npm run format:java   # Java only
npm run format:ts     # TypeScript only
```

**Validation:**

```bash
npm run check         # Check all formatting
```

### Commit Messages

- Follow **Conventional Commits** specification
- Format: `<type>(<scope>): <description>`
- Types: feat, fix, docs, style, refactor, test, chore
- Enforced via `@commitlint/config-conventional`

## Project Structure & Navigation

### Key Directories

**Backend Code:**

```
Source/
├── Plugins/          # JPF plugin modules
│   ├── Core/         # Core plugins (most business logic)
│   │   └── com.equella.core/  # Main core plugin
│   ├── Platform/     # Platform-level plugins
│   ├── Extensions/   # Extension plugins
│   ├── Admin/        # Admin console related
│   └── RemoteRepositories/  # Remote repo integrations
├── Server/
│   ├── equellaserver/  # Server bootstrap and entry point
│   ├── adminTool/      # Admin console launcher
│   └── conversion/     # Conversion service
├── Reporting/        # BiRT reporting
├── Themes/           # UI themes
└── Tools/            # Utility tools
```

**Frontend Code:**

UI:

```
react-front-end/      # New UI React application
├── tsrc/            # TypeScript source
├── __tests__/       # Jest tests
└── __stories__/     # Storybook stories
```

REST client:

```
oeq-ts-rest-api/     # REST API client module
└── src/             # TypeScript REST client code
```

**Testing:**

```
autotest/
├── OldTests/        # Main Selenium test suite (misnomer - actively used)
│   ├── src/test/java/  # Test code and Page Objects
│   └── src/test/scala/ # Newer test code being written in Scala
├── Tests/           # Additional test suite
└── IntegTester/     # Integration test harness
```

**Build & Configuration:**

```
project/             # SBT build customization
├── Common.scala
├── JPFPlugin.scala
├── JPFScanPlugin.scala
└── JPFRunnerPlugin.scala

Dev/
└── learningedge-config/  # Development configuration (generated)

build.sbt            # Main SBT build file
package.json         # Root NPM configuration
```

### When to Work Where

**Adding New UI Features:**

- Work in `react-front-end/tsrc/`
- Add REST client methods in `oeq-ts-rest-api/src/`
- Write Jest tests in `react-front-end/__tests__/`
- Consider Storybook stories in `react-front-end/__stories__/`

**Adding REST Endpoints:**

- Create in `Source/Plugins/Core/com.equella.core/` (typically in `src/.../api/` packages)
- Use JAX-RS annotations (`@Path`, `@GET`, `@POST`, etc.)
- Add service layer in appropriate package
- Reference existing REST resources for patterns

**Modifying Legacy UI:**

- Java code in `Source/Plugins/` using Sections framework
- Freemarker templates (`.ftl`) in `resources/view/` directories
- Legacy JavaScript in `Source/Plugins/Core/com.equella.core/resources/web/js/`

**Adding Backend Business Logic:**

- Write new code in Scala
- Use Guice for dependency injection
- Add unit tests (ScalaTest preferred)

**Database Changes:**

- Use Hibernate entities
- Consider migration scripts for schema changes
- Support all three databases (Postgres, MS SQL, Oracle)

## Testing Standards

### Testing Triangle Principle

Follow the testing pyramid - lots of unit tests, fewer integration tests, very few end-to-end tests:

- **Unit Tests** - Test individual classes/functions in isolation
- **Integration Tests** - Test interactions between components
- **End-to-End Tests** - Test complete user workflows (Selenium)

### Backend Testing

**Unit Tests:**

- Use ScalaTest for Scala code
- Use JUnit for Java code
- Mock external dependencies (Mockito available)
- Test business logic thoroughly

**Integration Tests:**

- Located in `autotest/IntegTester/`
- Test REST endpoints
- Use TestNG

### Frontend Testing

**Unit/Component Tests:**

- Use Jest and React Testing Library
- Located in `react-front-end/__tests__/`
- Test component behaviour and user interactions
- Mock API calls

**Running Tests:**

```bash
cd react-front-end
npm run test
```

### Selenium End-to-End Tests

**Location:** `autotest/OldTests/` (despite name, this is the active test suite)

**Critical: Locator Strategy**

Always follow this priority order when writing Selenium locators to ensure accessibility and reduce
flakiness:

| Priority             | Locator Type                        | Examples                                         | Why?                                                                               |
|----------------------|-------------------------------------|--------------------------------------------------|------------------------------------------------------------------------------------|
| **1. Semantic/ARIA** | `[aria-label]`, `[role]`, `[title]` | `By.cssSelector("[aria-label='Submit']")`        | **Best Practice.** Mimics screen readers. If this breaks, accessibility is broken. |
| **2. Test IDs**      | `[data-testid]`                     | `By.cssSelector("[data-testid='login-button']")` | **Most Reliable.** Dedicated QA hooks that don't change with CSS/layout.           |
| **3. Text Content**  | `linkText`, `partialLinkText`       | `By.linkText("Submit")`                          | **User-Centric.** Users look for text. Fragile with i18n.                          |
| **4. Unique IDs**    | `By.id`                             | `By.id("submit-btn")`                            | **Fast Performance.** Often missing or auto-generated in modern frameworks.        |
| **5. CSS Selectors** | `By.cssSelector`                    | `By.cssSelector(".main-nav .login-btn")`         | **Structural.** Keep selectors short and specific.                                 |
| **6. XPath**         | `By.xpath`                          | `By.xpath("//button[text()='Submit']")`          | **Last Resort.** Only for complex parent-child traversals CSS cannot handle.       |

**Guidelines:**

- **Never use XPath unless absolutely necessary**
- Use Page Object pattern (see existing Page Objects in `autotest/OldTests/src/.../pageobject/`)
- Write tests that are robust to timing issues (proper waits, not sleeps)
- Reduce flakiness by using explicit waits and stable locators
- Match patterns in existing tests

**Running Selenium Tests:**

- Various TestNG XML configurations in `autotest/OldTests/`
- Requires running openEQUELLA instance
- See `autotest/OldTests/` for test organization

## Development Environment

### Required Tools

**Core:**

- Java 21 (Temurin via SDKMAN recommended)
- Node.js 24.11.0 (use NVM - `.nvmrc` provided)
- PostgreSQL (or MS SQL/Oracle)
- ImageMagick
- FFmpeg

**IDE:**

- **IntelliJ IDEA** (recommended and used by all current developers)
- Increase heap to 4GiB for SBT build
- Enable plugins: Scala, TypeScript, Gradle
- Import as SBT project

### Common Development Tasks

**Initial Setup:**

```bash
./sbt prepareDevConfig    # Generate dev configuration
./sbt compile             # Compile everything
cd react-front-end && npm ci && npm run dev  # Build New UI
```

**Running Server:**

```bash
./sbt compile equellaserver/run
```

**Running Admin Console:**

```bash
./sbt compile adminTool/run
```

**Building Installer:**

```bash
./sbt installerZip
```

**Frontend Development:**

```bash
cd react-front-end
npm run dev          # Watch and rebuild
npm run storybook    # Launch Storybook
```

**Updating Plugin Libraries (Dev Mode):**

```bash
./sbt jpfWriteDevJars
```

## Integration Points

When working with integration features, be aware of:

- **LMS Integration** - LTI support (Moodle, Canvas, Brightspace)
- **Authentication** - OIDC, LDAP, OAuth providers
- **Reporting** - BiRT with custom connectors
- **Search** - Apache Lucene indexing

## Additional Resources

- **Setup Guide:** `CONTRIBUTING.md` - Detailed development environment setup
- **Project Overview:** `README.md` - High-level project information
- **Release Process:** `RELEASING.md` - Release procedures
- **Build Configuration:** `build.conf` - CI build customisation

## Key Conventions to Match

When contributing:

1. Study similar existing code for patterns and conventions
2. Use consistent naming (e.g., match package structure, file organisation)
3. Follow the existing module/plugin organisation
4. Maintain consistency with error handling patterns
5. Use logging appropriately (SLF4J on backend)
6. Handle i18n properly (language bundles in New UI)
7. Consider accessibility in all UI changes
8. Write tests that match existing test patterns

---

*This document is intended to help AI assistants and new developers understand the openEQUELLA
codebase. For detailed setup instructions, always refer to CONTRIBUTING.md.*

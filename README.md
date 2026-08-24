# markflow

A Kotlin/JVM library and Gradle plugin for Markdown processing pipelines.
Parse, transform, and emit Markdown (or HTML) with a composable, AST-based API.

## Overview

markflow is built on [Flexmark-java](https://github.com/vsch/flexmark-java) and provides:

- A clean Kotlin API for parsing Markdown into a mutable AST
- Composable transformation passes (heading manipulation, front matter promotion, etc.)
- Round-trip Markdown output via Flexmark's `Formatter`, with reliable front matter handling
- A Gradle plugin exposing common transformations as cacheable, incremental tasks

It is **not** a static site generator. It is a document-processing library and build-tool
integration layer — the Markdown equivalent of what a CSS post-processor does for stylesheets.

## Design Decisions

### Parser: Flexmark-java

markflow uses [Flexmark-java](https://github.com/vsch/flexmark-java) as its parser and formatter.
Flexmark was chosen because:

- It has the most mature round-trip Markdown output (`Formatter`) of any JVM library
- Its extension API is clean and well-suited to custom AST node types
- It supports GFM tables, YAML front matter, and other common extensions out of the box
- It is Apache 2.0 licensed, compatible with all likely dependencies

[JetBrains Markdown](https://github.com/JetBrains/markdown) was considered but ruled out: its AST
is largely immutable, it has no Markdown output support, and it has no front matter extension.
It remains the only JVM option for Kotlin Multiplatform targets, but KMP is not a current requirement.

### Front Matter: Opaque Text Node

Flexmark's built-in `YamlFrontMatterExtension` parses front matter into key/value pairs but does
not reliably round-trip it through the `Formatter`. markflow instead provides a custom extension
that captures the entire front matter block as a single opaque text node in the AST. This means:

- The `Formatter` preserves front matter verbatim by default
- Callers that need to inspect or modify front matter values do so via Jackson YAML, extracting
  the raw text from the node, modifying it, and writing it back
- No front matter content is silently dropped or reordered

### Transformation Pipeline

Transformations are independent AST passes. Each pass is a `MarkdownTransformer` that receives
a `Document` and returns a (possibly mutated) `Document`. Passes are composed into a
`MarkdownPipeline` and applied in order. This keeps individual transformers small, testable, and
independently togglable.

## Module Structure

```
markflow/
  library/          # Pure Kotlin library — no Gradle or Maven dependencies
  gradle-plugin/    # Gradle plugin wrapping library tasks
```

### `library`

The standalone Kotlin library JAR. Depends only on Flexmark-java and Jackson YAML. No build-tool
APIs. Usable from any JVM context: Gradle, Maven, a CLI, application code.

Key types:

| Type | Description |
|---|---|
| `MarkdownDocument` | A parsed document: mutable Flexmark `Document` + raw front matter string |
| `MarkdownParser` | Parses a `String` or `File` into a `MarkdownDocument` |
| `MarkdownTransformer` | Single-responsibility AST transformation pass |
| `MarkdownPipeline` | Ordered composition of `MarkdownTransformer` instances |
| `MarkdownFormatter` | Emits a `MarkdownDocument` as Markdown (via Flexmark `Formatter`) or HTML (via `HtmlRenderer`) |
| `FrontMatter` | Typed view of the front matter block via Jackson YAML |

Built-in transformers:

| Transformer | Description |
|---|---|
| `TitlePromoter` | Extracts `title:` from front matter and prepends an H1 heading if none exists |
| `HeadingDemoter` | Increments all heading levels by a configurable offset (e.g. H1→H2) |
| `TableFormatter` | Pads GFM table columns to consistent widths |
| `SentencePerLineFormatter` | Splits paragraph text at sentence boundaries, one sentence per line |
| `TemplateSubstitutor` | Replaces `{{key}}` placeholders with provided values, skipping fenced code blocks |
| `ScreenshotPlaceholderExpander` | Expands `<!-- screenshot: name.png | alt -->` to `![alt](path/name.png)` |

### `gradle-plugin`

Plugin ID: `net.oxspring.markflow`

Exposes library functionality as cacheable, incremental Gradle tasks with `@InputFiles` /
`@OutputFiles` annotations and UP-TO-DATE checking.

Tasks:

| Task | Description |
|---|---|
| `ProcessMarkdownTask` | Applies a configured pipeline to a set of input `.md` files, writing results to an output directory |
| `MergeMarkdownTask` | Concatenates multiple `.md` files (AST-level merge) into a single output file, with optional heading demotion per file |
| `LintMarkdownTask` | Reports style violations (heading level skips, missing front matter fields, etc.) without modifying files |

The plugin registers a `markflow { }` extension block for project-level configuration.

## Package Namespace

`net.oxspring.markflow`

Sub-packages follow feature boundaries:

| Package | Contents |
|---|---|
| `net.oxspring.markflow` | Public API: `MarkdownDocument`, `MarkdownParser`, `MarkdownPipeline`, `MarkdownFormatter` |
| `net.oxspring.markflow.transform` | Built-in `MarkdownTransformer` implementations |
| `net.oxspring.markflow.frontmatter` | `FrontMatter`, opaque front matter AST extension |
| `net.oxspring.markflow.gradle` | Gradle plugin and task implementations |

## MVP Scope (v1.0)

The v1.0 MVP targets feature parity with the custom Gradle tasks in the
[wtn](https://github.com/roxspring/wtn) monorepo, specifically:

### Replaces `ProcessGuideTask`

`ProcessGuideTask` processes a directory of Hugo guide Markdown files, applying:

- `{{version}}`, `{{year}}`, `{{date}}` token substitution (skipping fenced code blocks)
- `<!-- screenshot: name.png | alt text -->` → `![alt](screenshots/name.png)` expansion

markflow replaces this with a `ProcessMarkdownTask` configured with:

- `TemplateSubstitutor` (token substitution, fence-aware)
- `ScreenshotPlaceholderExpander`

### Replaces front matter title extraction in `BuildPdfTask`

`BuildPdfTask` reads `title:` from each file's YAML front matter and manually prepends
`# Title

` before passing files to Pandoc. markflow replaces this with:

- `TitlePromoter` transformer, promoting `title:` front matter to an H1 heading
- `MergeMarkdownTask` concatenating pages in a declared order with a `HeadingDemoter` offset

### Linting (new capability)

`LintMarkdownTask` enforces the guide's documented style rules:

- One sentence per line (configurable)
- Maximum line length (configurable, default 120)
- No heading level skips
- Required front matter fields (configurable)

## Build & Test

```bash
./gradlew assemble   # Compile all modules
./gradlew check      # Tests + ktlint + Kover coverage verification
./gradlew build      # assemble + check
```

Test framework: JUnit 5 (Jupiter) with AssertJ.

## Key Dependencies

| Dependency | Version | License |
|---|---|---|
| Flexmark-java | 0.64.x | BSD 2-Clause |
| Jackson YAML | 2.x | Apache 2.0 |
| Kotlin | 2.x | Apache 2.0 |
| Gradle Plugin API | 8.x | Apache 2.0 |

## License

Apache 2.0. See [LICENSE](LICENSE).

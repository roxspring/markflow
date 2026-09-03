# markflow

A Kotlin/JVM library and Gradle plugin for Markdown processing pipelines.
Parse, transform, and emit Markdown (or HTML) with a composable, AST-based API.

## Overview

markflow is built on [commonmark-java](https://github.com/commonmark/commonmark-java) and provides:

- A clean Kotlin API for parsing Markdown into a mutable AST
- Composable transformation passes (heading manipulation, front matter promotion, etc.)
- Round-trip Markdown output via commonmark-java's `MarkdownRenderer`, with reliable front matter handling
- A Gradle plugin exposing common transformations as cacheable, incremental tasks

It is **not** a static site generator. It is a document-processing library and build-tool
integration layer — the Markdown equivalent of what a CSS post-processor does for stylesheets.

## Design Decisions

### Parser: commonmark-java

markflow uses [commonmark-java](https://github.com/commonmark/commonmark-java) as its parser and
formatter. commonmark-java was chosen because:

- It is actively maintained (Atlassian origin, used by OpenJDK, Google, Gerrit)
- It is spec-compliant with [CommonMark](https://spec.commonmark.org/)
- It provides round-trip Markdown output via `MarkdownRenderer`
- It supports GFM tables, YAML front matter (including raw/opaque capture), strikethrough, footnotes,
  and other common extensions out of the box
- It is BSD 2-Clause licensed, compatible with all likely dependencies

**Alternatives considered:**

[Flexmark-java](https://github.com/vsch/flexmark-java) was the original choice and has a richer
extension ecosystem (TOC, definition lists, typographic quotes, etc.), but has been effectively
unmaintained since 2023 with 178+ open issues. The maintenance risk outweighs the extension breadth
for a library intended for long-term use.

[JetBrains Markdown](https://github.com/JetBrains/markdown) is actively developed but ruled out:
its AST is largely immutable, it has no Markdown output support, and it targets Kotlin Multiplatform
which is not a current requirement.

### Front Matter: Opaque Capture via RawContentParser

commonmark-java's `YamlFrontMatterExtension` supports a `RawContentParser` mode that captures the
entire front matter block as a single raw string rather than parsing it into key/value pairs. This
means:

- The `MarkdownRenderer` preserves front matter verbatim by default
- Callers that need to inspect or modify front matter values do so via Jackson YAML, extracting
  the raw text, modifying it, and writing it back
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

The standalone Kotlin library JAR. Depends only on commonmark-java and Jackson YAML. No build-tool
APIs. Usable from any JVM context: Gradle, Maven, a CLI, application code.

Key types:

|Type|Description|
|---|---|
|`MarkdownDocument`|A parsed document: mutable commonmark-java `Document` + raw front matter string|
|`MarkdownParser`|Parses a `String` or `File` into a `MarkdownDocument`|
|`MarkdownTransformer`|Single-responsibility AST transformation pass|
|`MarkdownPipeline`|Ordered composition of `MarkdownTransformer` instances|
|`MarkdownFormatter`|Emits a `MarkdownDocument` as Markdown (via `MarkdownRenderer`) or HTML (via `HtmlRenderer`)|
|`FrontMatter`|Typed view of the front matter block via Jackson YAML|

Built-in transformers:

|Transformer|Description|
|---|---|
|`TitlePromoter`|Extracts `title:` from front matter and prepends an H1 heading if none exists|
|`HeadingDemoter`|Increments all heading levels by a configurable offset (e.g. H1→H2)|
|`TableFormatter`|Pads GFM table columns to consistent widths|
|`SentencePerLineFormatter`|Splits paragraph text at sentence boundaries, one sentence per line|
|`TemplateSubstitutor`|Replaces `{{key}}` placeholders with provided values, skipping fenced code blocks|
|`HtmlCommentExpander`|Expands structured HTML comments to Markdown content via registered handlers|

### `gradle-plugin`

Plugin ID: `net.oxspring.markflow`

Exposes library functionality as cacheable, incremental Gradle tasks with `@InputFiles` /
`@OutputFiles` annotations and UP-TO-DATE checking.

Tasks:

|Task|Description|
|---|---|
|`ProcessMarkdownTask`|Applies a configured pipeline to a set of input `.md` files, writing results to an output directory|
|`MergeMarkdownTask`|Concatenates multiple `.md` files (AST-level merge) into a single output file, with optional heading demotion per file|
|`LintMarkdownTask`|Reports style violations (heading level skips, missing front matter fields, etc.) without modifying files|

The plugin registers a `markflow { }` extension block for project-level configuration.

## Package Namespace

`net.oxspring.markflow`

Sub-packages follow feature boundaries:

|Package|Contents|
|---|---|
|`net.oxspring.markflow`|Public API: `MarkdownDocument`, `MarkdownParser`, `MarkdownPipeline`, `MarkdownFormatter`|
|`net.oxspring.markflow.transform`|Built-in `MarkdownTransformer` implementations|
|`net.oxspring.markflow.frontmatter`|`FrontMatter`, front matter access utilities|
|`net.oxspring.markflow.gradle`|Gradle plugin and task implementations|

## MVP Scope (v1.0)

The v1.0 MVP targets feature parity with the custom Gradle tasks in the
[wtn](https://github.com/roxspring/wtn) monorepo, specifically:

### Replaces `ProcessGuideTask`

`ProcessGuideTask` processes a directory of Hugo guide Markdown files, applying:

- `{{version}}`, `{{year}}`, `{{date}}` token substitution (skipping fenced code blocks)
- `<!-- screenshot: name.png | alt text -->` → `![alt](screenshots/name.png)` expansion

markflow replaces this with a `ProcessMarkdownTask` configured with:

- `TemplateSubstitutor` (token substitution, fence-aware)
- `HtmlCommentExpander` with a screenshot handler

### Replaces front matter title extraction in `BuildPdfTask`

`BuildPdfTask` reads `title:` from each file's YAML front matter and manually prepends:

```markdown
# Title

```

before passing files to Pandoc. markflow replaces this with:

- `TitlePromoter` transformer, promoting `title:` front matter to an H1 heading
- `MergeMarkdownTask` concatenating pages in a declared order with a `HeadingDemoter` offset

### Linting (new capability)

`LintMarkdownTask` enforces the guide's documented style rules:

- One sentence per line (configurable)
- Maximum line length (configurable, default 120)
- No heading level skips
- Required front matter fields (configurable)

## Build \& Test

```bash
./gradlew assemble   # Compile all modules
./gradlew check      # Tests + ktlint + Kover coverage verification
./gradlew build      # assemble + check
```

Test framework: JUnit 6 (Jupiter) with AssertJ.

## Contributing

### Before committing

Each commit should leave the build green. Run:

```bash
./gradlew build   # tests + ktlint + Kover coverage verification
```

Fix any ktlint violations with:

```bash
./gradlew ktlintFormat
```

### Linting the repo's own Markdown

The `library` module includes `markdownCheck` and `markdownFormat` tasks that check and fix
all `*.md` files in the repository. `markdownCheck` runs automatically as part of `./gradlew check`.

To fix violations:

```bash
./gradlew :library:markdownFormat
```

### Commit messages

Commit messages are linted by [commitlint](https://commitlint.io) on push (conventional commits
format). The most common trap: the **first word of the subject must be lowercase**, even if it is
a proper noun. Prefix with a lowercase verb if the natural first word is a proper noun:

```
# Bad — subject starts with a proper noun
feat: Gradle plugin skeleton

# Good — lowercase verb comes first, proper noun follows
feat: add Gradle plugin skeleton
```

## Key Dependencies

|Dependency|Version|License|
|---|---|---|
|commonmark-java|0.30.x|BSD 2-Clause|
|commonmark-ext-gfm-tables|0.30.x|BSD 2-Clause|
|commonmark-ext-yaml-front-matter|0.30.x|BSD 2-Clause|
|Jackson YAML|2.x|Apache 2.0|
|Kotlin|2.x|Apache 2.0|
|Gradle Plugin API|9.x|Apache 2.0|

## License

Apache 2.0. See [LICENSE](LICENSE).

---
name: maven-javadoc-skip
description: Configure maven-javadoc-plugin to skip specific modules from javadoc.jar generation. The skippedModules parameter only works with aggregate/javadoc goals, not jar goal. Use skip=true in submodule POMs for jar goal. Use when configuring Maven multi-module javadoc builds, excluding modules from javadoc generation, or troubleshooting maven-javadoc-plugin configuration errors.
---

# Maven Javadoc Plugin Module Skip Configuration

## Critical Rule: Goal Specificity

The `skippedModules` parameter has **goal specificity** — not all goals support it:

| Goal             | `skippedModules` | `skip` |
|------------------|:----------------:|:------:|
| `jar`            | ❌ NOT supported  |   ✅    |
| `javadoc`        |        ✅         |   ✅    |
| `aggregate`      |        ✅         |   ✅    |
| `test-javadoc`   |        ✅         |   ✅    |
| `test-aggregate` |        ✅         |   ✅    |

## Option A: `skip` in Submodule (Recommended for `jar` goal)

In the submodule's `pom.xml`:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <configuration>
                <skip>true</skip>
            </configuration>
        </plugin>
    </plugins>
</build>
```

`<skip>` is a boolean parameter supported by ALL goals. It completely skips javadoc processing for this module.

## Option B: `skippedModules` in Parent POM (Only for aggregate/javadoc)

In the parent `pom.xml`, **only** when using `aggregate` or `javadoc` goal:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <configuration>
        <!-- Comma-separated string, NOT nested XML elements -->
        <skippedModules>module-a,module-b</skippedModules>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>aggregate</goal>  <!-- or javadoc -->
            </goals>
        </execution>
    </executions>
</plugin>
```

### ⚠️ Two Common Mistakes

**1. Using XML child elements (WRONG — will fail with parse error):**

```xml
<!-- WRONG: "Basic element must not contain child elements" -->
<skippedModules>
    <skippedModule>module-a</skippedModule>
</skippedModules>
```

**2. Using with `jar` goal (WRONG — silently ignored):**

```xml
<!-- WRONG: skippedModules is ignored by jar goal -->
<execution>
    <goals>
        <goal>jar</goal>
    </goals>
</execution>
<configuration>
<skippedModules>module-a</skippedModules>
</configuration>
```

## Decision Flow

```
Which javadoc goal are you using?
├── jar → Use Option A (skip in submodule POM)
├── aggregate / javadoc → Use Option A or B
└── Not sure → Use Option A (always works)
```

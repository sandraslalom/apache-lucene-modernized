# Apache Lucene Modernization - Migration Guide

This guide helps you migrate from the original Apache Lucene to the modernized version that leverages Java 23+ features.

## 🎯 Overview

The modernized Apache Lucene maintains full API compatibility while introducing modern Java features and patterns. Most existing code will work without changes, but you can opt into modern features for improved safety and performance.

## 📋 Prerequisites

- Java 23 or later
- Enable preview features: `--enable-preview`
- Update build tools to support Java 23

## 🔄 Key Changes and Migration Paths

### 1. Document Class Enhancements

#### Stream API Integration
**New Feature**: Stream processing for fields

```java
// Modern approach - using streams
document.stream()
    .filter(field -> field.name().startsWith("title"))
    .map(IndexableField::stringValue)
    .collect(Collectors.toList());

// Traditional approach still works
List<String> titles = new ArrayList<>();
for (IndexableField field : document) {
    if (field.name().startsWith("title")) {
        titles.add(field.stringValue());
    }
}
```

#### Optional API for Null Safety
**Migration**: Replace null checks with Optional

```java
// Before (still works but deprecated)
String value = document.get("field");
if (value != null) {
    processValue(value);
}

// After (recommended)
document.getOptional("field")
    .ifPresent(this::processValue);

// Or with default values
String value = document.getOptional("field")
    .orElse("default");
```

#### Enhanced Field Operations
**New Features**: Additional utility methods

```java
// Check field existence
if (document.hasField("title")) {
    // Process field
}

// Check if document is empty
if (!document.isEmpty()) {
    // Process document
}

// Get field count
int fieldCount = document.size();
```

### 2. BytesRef Modernization

#### Factory Methods
**New Feature**: Static factory methods for cleaner creation

```java
// Modern approach
BytesRef ref1 = BytesRef.of("hello world");
BytesRef ref2 = BytesRef.of(byteArray);
BytesRef ref3 = BytesRef.of(byteArray, offset, length);

// Traditional approach still works
BytesRef ref = new BytesRef("hello world");
```

#### Enhanced Utility Methods
**New Features**: Additional convenience methods

```java
// Check if empty
if (bytesRef.isEmpty()) {
    // Handle empty case
}

// Get byte at index
byte b = bytesRef.byteAt(2);

// Convert to byte array copy
byte[] copy = bytesRef.toByteArray();
```

#### Modern Switch Expressions
**Internal Enhancement**: Uses pattern matching (no API changes)

```java
// The equals method now uses modern switch expressions internally
// No changes needed in your code
```

### 3. Query Class Enhancements

#### Sealed Classes and Pattern Matching
**New Feature**: Type-safe query handling

```java
// Pattern matching with sealed classes
String analyzeQuery(Query query) {
    return switch (query) {
        case TermQuery tq -> "Term: " + tq.getTerm();
        case BooleanQuery bq -> "Boolean with " + bq.clauses().size() + " clauses";
        case PhraseQuery pq -> "Phrase: " + pq.getTerms().length + " terms";
        default -> "Other query type: " + query.getClass().getSimpleName();
    };
}
```

#### Optional-based Query Operations
**New Features**: Safer query operations

```java
// Safe query casting
query.as(TermQuery.class)
    .ifPresent(termQuery -> {
        // Work with TermQuery
        Term term = termQuery.getTerm();
    });

// Type checking
if (query.isInstanceOf(BooleanQuery.class)) {
    // Handle boolean query
}

// Optional rewriting
query.tryRewrite(searcher)
    .ifPresent(rewrittenQuery -> {
        // Use rewritten query
    });
```

#### Enhanced Debugging
**New Features**: Better debugging support

```java
// Get query type name
String type = query.getQueryType();

// Detailed debugging information
String details = query.toDetailedString();

// Fluent boost application
Query boostedQuery = query.boost(2.0f);
```

### 4. Build System Migration

#### Gradle Configuration Updates
**Migration**: Update your build.gradle

```gradle
// Enable Java 23 with preview features
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs.addAll([
        '--enable-preview',
        '--add-modules', 'jdk.incubator.vector',
        '--add-modules', 'jdk.incubator.concurrent'
    ])
    options.release = 23
}

tasks.withType(Test).configureEach {
    jvmArgs(['--enable-preview'])
}
```

#### Dependency Updates
**Migration**: Update version references

```gradle
dependencies {
    implementation 'org.apache.lucene:lucene-core:10.0.0-modernized'
    // Other Lucene modules
}
```

### 5. Performance Optimizations

#### Virtual Threads Integration
**New Feature**: Leverage virtual threads for I/O operations

```java
// The framework automatically uses virtual threads where beneficial
// No code changes required, but you can explicitly use them:

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Submit search tasks
    Future<TopDocs> future = executor.submit(() -> 
        searcher.search(query, 10));
    
    TopDocs results = future.get();
}
```

#### Vector API Integration
**Internal Enhancement**: Automatic optimization for numerical operations

```java
// Vector operations are automatically optimized internally
// No code changes required
```

## 🚨 Breaking Changes

### Deprecated Methods
The following methods are deprecated but still functional:

- `Document.get(String)` → Use `Document.getOptional(String)`
- `Document.getField(String)` → Use `Document.getFieldOptional(String)`
- `BytesRef.getBinaryValue(String)` → Use `BytesRef.getBinaryValueOptional(String)`

### Removed Features
- None in this version - full backward compatibility maintained

## 🔧 IDE Configuration

### IntelliJ IDEA
```
File → Project Structure → Project Settings → Project
- Project SDK: Java 23
- Project language level: 23 (Preview)

File → Settings → Build → Compiler → Java Compiler
- Project bytecode version: 23
- Additional command line parameters: --enable-preview
```

### Eclipse
```
Project Properties → Java Build Path → Libraries
- Modulepath: JRE System Library [JavaSE-23]

Project Properties → Java Compiler
- Compiler compliance level: 23
- Enable preview features: Yes
```

### VS Code
```json
// settings.json
{
    "java.configuration.runtimes": [
        {
            "name": "JavaSE-23",
            "path": "/path/to/java23"
        }
    ],
    "java.compile.nullAnalysis.mode": "automatic",
    "java.jdt.ls.vmargs": "--enable-preview"
}
```

## 🧪 Testing Migration

### JUnit 5 Integration
**New Feature**: Enhanced testing support

```java
@Test
void testModernFeatures() {
    Document doc = new Document();
    doc.add(new TextField("title", "Modern Lucene", Field.Store.YES));
    
    // Use modern assertions
    assertThat(doc.hasField("title")).isTrue();
    assertThat(doc.getOptional("title")).isPresent();
    assertThat(doc.size()).isEqualTo(1);
}
```

### Property-Based Testing
**New Feature**: Enhanced test coverage

```java
@Property
void documentFieldOperations(@ForAll String fieldName, @ForAll String value) {
    Document doc = new Document();
    doc.add(new TextField(fieldName, value, Field.Store.YES));
    
    assertThat(doc.hasField(fieldName)).isTrue();
    assertThat(doc.getOptional(fieldName)).contains(value);
}
```

## 📊 Performance Considerations

### Memory Usage
- Records reduce memory overhead for data classes
- Optional usage has minimal overhead with modern JVMs
- Stream operations are optimized for performance

### Concurrency
- Virtual threads improve scalability for I/O-bound operations
- Structured concurrency provides better resource management
- Enhanced concurrent collections reduce contention

### Garbage Collection
- Optimized for modern GCs (ZGC, Shenandoah)
- Reduced object allocation with modern patterns
- Better memory locality with records

## 🔍 Troubleshooting

### Common Issues

#### Preview Features Not Enabled
```
Error: Preview features are not enabled
Solution: Add --enable-preview to compiler and runtime arguments
```

#### Module Path Issues
```
Error: Module not found
Solution: Ensure --add-modules flags are set correctly
```

#### Version Compatibility
```
Error: Unsupported class file version
Solution: Ensure all tools use Java 23+
```

## 📚 Additional Resources

- [Java 23 Documentation](https://docs.oracle.com/en/java/javase/23/)
- [Pattern Matching Guide](https://openjdk.org/jeps/441)
- [Virtual Threads Tutorial](https://openjdk.org/jeps/444)
- [Vector API Documentation](https://openjdk.org/jeps/448)

## 🤝 Getting Help

1. Check this migration guide
2. Review the modernized API documentation
3. Create an issue in the repository
4. Refer to the original Apache Lucene documentation for core concepts

---

**Remember**: The modernization maintains full backward compatibility. You can migrate gradually, adopting modern features at your own pace.
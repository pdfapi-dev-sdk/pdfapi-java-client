# PDF API Java Client

A Java client library for converting HTML to PDF using the PDF API service.

## Features

- Asynchronous API using CompletableFuture
- Streaming support for handling large files
- Builder pattern for easy configuration
- Support for custom page formats, margins, and scaling
- Asset attachment support
- Header and footer support
- OkHttp-based implementation

## Installation

### Gradle

```groovy
dependencies {
    implementation 'io.pdfapi:pdfapi-java-client:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.pdfapi</groupId>
    <artifactId>pdfapi-java-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

## HTTP Client Configuration

By default, the library uses OkHttp as its HTTP client implementation. However, you can use alternative HTTP clients:

### Using Apache HTTP Client

#### Gradle

```groovy
dependencies {
    implementation('io.pdfapi:pdfapi-java-client:1.0.0') {
        exclude group: 'com.squareup.okhttp3', module: 'okhttp'
    }
    implementation 'org.apache.httpcomponents:httpclient:4.5.13'
}
```

#### Maven

```xml
<dependency>
    <groupId>io.pdfapi</groupId>
    <artifactId>pdfapi-java-client</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>
```

### Using Spring RestTemplate

#### Gradle

```groovy
dependencies {
    implementation('io.pdfapi:pdfapi-java-client:1.0.0') {
        exclude group: 'com.squareup.okhttp3', module: 'okhttp'
    }
    implementation 'org.springframework:spring-web:5.3.13'
}
```

#### Maven

```xml
<dependency>
    <groupId>io.pdfapi</groupId>
    <artifactId>pdfapi-java-client</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.13</version>
</dependency>
```

### Using Different HTTP Client Implementations

```java
// Default (OkHttp)
PdfApiClient client = PdfApiClientFactory.createClient(config);

// Apache HTTP Client
PdfApiClient client = PdfApiClientFactory.createWithApacheHttpClient(config);

// Spring RestTemplate
PdfApiClient client = PdfApiClientFactory.createWithRestTemplate(config);

// Custom pre-configured clients
PdfApiClient client = PdfApiClientFactory.createWithOkHttp(config, customOkHttpClient);
PdfApiClient client = PdfApiClientFactory.createWithApacheHttpClient(config, customApacheClient);
PdfApiClient client = PdfApiClientFactory.createWithRestTemplate(config, customRestTemplate);
```

## Usage

### Minimal Usage

```java
// Create client
PdfApiClient client = PdfApiClientFactory.createClient(
    PdfApiClientConfig.builder().apiKey("your-api-key").build()
);

// Convert HTML to PDF with default A4 format
String html = "<html><body><h1>Hello World</h1></body></html>";
ConversionRequest request = ConversionRequest.builder()
    .htmlContent(new ByteArrayInputStream(html.getBytes()))
    .build();

try (FileOutputStream output = new FileOutputStream("output.pdf")) {
    client.convert(request, output).join();
}
```

### Basic Usage

```java
// Create client configuration
PdfApiClientConfig config = PdfApiClientConfig.builder()
        .apiKey("your-api-key")
        .build();

// Create client
PdfApiClient client = PdfApiClientFactory.createClient(config);

// Prepare conversion properties
ConversionProperties properties = ConversionProperties.builder()
        .format(PageFormat.A4)
        .margin(Margin.builder()
                .top(20)
                .bottom(20)
                .left(20)
                .right(20)
                .build())
        .scale(1.0f)
        .landscape(false)
        .build();

// Create conversion request
String html = "<html><body><h1>Hello World</h1></body></html>";
ConversionRequest request = ConversionRequest.builder()
        .properties(properties)
        .htmlContent(new ByteArrayInputStream(html.getBytes()))
        .build();

// Convert HTML to PDF
try (FileOutputStream output = new FileOutputStream("output.pdf")) {
    client.convert(request, output).join();
}
```

### Advanced Usage with Assets and Headers/Footers

```java
// Create header and footer assets
AssetInput header = AssetInput.of(
    new FileInputStream("header.html"),
    "header.html"
);
AssetInput footer = AssetInput.of(
    new FileInputStream("footer.html"),
    "footer.html"
);

// Create style and image assets
AssetInput styles = AssetInput.of(
    new FileInputStream("styles.css"),
    "styles.css"
);
AssetInput image = AssetInput.of(
    new FileInputStream("image.png"),
    "image.png"
);

// Create conversion request with all assets
ConversionRequest request = ConversionRequest.builder()
        .properties(ConversionProperties.builder()
                .format(PageFormat.A4)
                .margin(Margin.builder()
                        .top(50)    // Leave space for header
                        .bottom(50) // Leave space for footer
                        .left(20)
                        .right(20)
                        .build())
                .scale(1.0f)
                .landscape(false)
                .build())
        .htmlContent(new FileInputStream("content.html"))
        .headerFile(header)  // Header will be automatically registered in properties
        .footerFile(footer)  // Footer will be automatically registered in properties
        .addAsset(styles)
        .addAsset(image)
        .build();

// Convert HTML to PDF
try (FileOutputStream output = new FileOutputStream("output.pdf")) {
    client.convert(request, output).join();
}
```

## Configuration Options

The client can be configured with the following options:

```java
PdfApiClientConfig config = PdfApiClientConfig.builder()
        .apiKey("your-api-key")           // Required
        .baseUrl("https://api.example.com") // Optional, defaults to https://api.pdfapi.io
        .timeoutSeconds(60)                // Optional, defaults to 30
        .build();
```

## Page Formats

The following page formats are supported:

- Letter
- Legal
- Tabloid
- Ledger
- A0 through A6

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
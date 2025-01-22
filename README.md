# PDF API Java Client

A Java client library for converting HTML to PDF using the PDF API service.

## Features

- Asynchronous API using CompletableFuture
- Streaming support for handling large files
- Builder pattern for easy configuration
- Support for custom page formats, margins, and scaling
- Asset attachment support
- OkHttp-based implementation

## Installation

Add the following dependency to your project:

```gradle
dependencies {
    implementation 'io.pdfapi:pdfapi-java-client:0.1.0'
}
```

## Usage

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

// Convert HTML to PDF
String htmlContent = "<html><body><h1>Hello World</h1></body></html>";
try (InputStream pdfStream = client.convertHtmlToPdf(properties, htmlContent).join()) {
    // Save PDF to file
    File outputFile = new File("output.pdf");
    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        pdfStream.transferTo(fos);
    }
}
```

### Advanced Usage

For more control over the conversion process, you can use the individual API methods:

```java
// Initialize conversion
String conversionId = client.initializeConversion(properties).join();

// Attach assets (optional)
client.attachAsset(conversionId, new File("style.css")).join();
client.attachAsset(conversionId, new File("image.png")).join();

// Perform conversion
client.performConversion(conversionId, htmlContent).join();

// Get result (with polling)
InputStream pdfStream = client.getConversionResult(conversionId).join();
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
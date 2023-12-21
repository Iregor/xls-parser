package com.store.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.demo.dto.*;
import com.store.demo.models.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DataSender {
    private final HttpClient client;
    private final ParsedData parsedData;
    private final Scanner scanner = new Scanner(System.in);
    private final ObjectMapper mapper;
    private int requestCount;

    private String domain;

    public DataSender(ParsedData parsedData) {
        this.parsedData = parsedData;
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
        requestCount = 0;
    }

    public void processRequests() {
        System.out.println("Enter sending mode...");
        chooseDomain();
        int command = -1;
        do {
            showMenu();
            String userInput = scanner.nextLine();
            try {
                command = Integer.parseInt(userInput);
            } catch (Exception exc) {
                System.out.println("Wrong command.");
                continue;
            }
            switch (command) {
                case 1 -> sendAttributes();
                case 2 -> sendCategories();
                case 3 -> sendModels();
                case 4 -> sendCategoryAttributes();
                case 5 -> sendModelAttributes();
                case 6 -> sendCollections();
                case 7 -> sendModelImages();
                case 8 -> sendSales();
                case 9 -> showCount();
                case 10 -> chooseDomain();
            }
        } while (command != 0);
    }

    private void showMenu() {
        // order of post requests:
        // attributes -> categories -> models -> categoryAttributes -> modelAttributes
        System.out.println("\nSelect option.");
        System.out.println("1. Send attributes.");
        System.out.println("2. Send categories.");
        System.out.println("3. Send models.");
        System.out.println("4. Send category attributes.");
        System.out.println("5. Send model attributes.");
        System.out.println("6. Send collections.");
        System.out.println("7. Send model images.");
        System.out.println("8. Send sales.");
        System.out.println("9. Show count.");
        System.out.println("10. Choose domain.");
        System.out.println("0. Return to main menu.");
    }

    private void sendAttributes() {
        URI uri;
        String path = "/api/v1/admin/attributes";
        try {
            uri = new URI(domain + path);
        } catch (URISyntaxException exc) {
            System.out.println("Wrong URI syntax. Check domain and path.");
            return;
        }
        for (Map.Entry<String, Attribute> attributeEntry : parsedData.getAttributeMap().entrySet()) {
            try {
                Attribute attribute = attributeEntry.getValue();
                Attribute receivedAttribute = sendAttribute(attribute, uri);
                attribute.setId(receivedAttribute.getId());
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All attributes processed.");
    }

    private Attribute sendAttribute(Attribute attribute, URI uri) throws IOException, InterruptedException {
        String attributeJson;
        try {
            attributeJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(new AttributeDto(attribute.getName()));
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", attribute));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(attributeJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readValue(response.body(), Attribute.class);
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to parse JSON to object: %s%n", response.body()));
        }
    }

    private void sendCategories() {
        URI uri;
        String path = "/api/v1/categories";
        try {
            uri = new URI(domain + path);
        } catch (URISyntaxException exc) {
            System.out.println("Wrong URI syntax. Check domain and path.");
            return;
        }
        for (Map.Entry<String, Category> categoryEntry : parsedData.getCategoryMap().entrySet()) {
            try {
                Category category = categoryEntry.getValue();
                Long receivedId = sendCategory(category, uri);
                category.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All categories processed.");
    }

    private Long sendCategory(Category category, URI uri) throws IOException, InterruptedException {
        String categoryJson;
        CategoryDto dto = new CategoryDto(category.getName(), category.getParentCategory() != null ? category.getParentCategory().getId() : null, category.getImageLink());
        try {
            categoryJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(dto);
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", dto));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(categoryJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.reader().readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendModels() {
        for (Model model : parsedData.getModelMap().values()) {
            try {
                URI uri;
                String path = String.format("/api/v1/categories/%d/model", model.getCategory().getId());
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendModel(model, uri);
                model.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All models processed.");
    }

    private Long sendModel(Model model, URI uri) throws IOException, InterruptedException {
        String modelJson;
        try {
            modelJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(model);
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", model));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(modelJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendCategoryAttributes() {
        Set<CategoryAttribute> categoryAttributeSet = parsedData.getCategoryAttributeSet();
        for (CategoryAttribute categoryAttribute : categoryAttributeSet) {
            try {
                URI uri;
                String path = String.format("/api/v1/admin/category-attributes/%d?attributeId=%d",
                        categoryAttribute.getCategory().getId(),
                        categoryAttribute.getAttribute().getId());
//                System.out.println("Path: " + path + " CategoryAttribute: " + categoryAttribute);
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendCategoryAttribute(categoryAttribute, uri);
                categoryAttribute.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All category attributes processed.");
    }

    private Long sendCategoryAttribute(CategoryAttribute categoryAttribute, URI uri) throws IOException, InterruptedException {
        String categoryAttributesJson;
        try {
            categoryAttributesJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(categoryAttribute);
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", categoryAttribute));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(categoryAttributesJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendModelAttributes() {

        Set<ModelAttribute> modelAttributeSet = parsedData.getModelAttributeSet();
        for (ModelAttribute modelAttribute : modelAttributeSet) {
            try {
                URI uri;
                String path = String.format("/api/v1/admin/model-attributes/model/%d?categoryAttributeId=%d",
                        modelAttribute.getModel().getId(),
                        modelAttribute.getCategoryAttribute().getId());
//                System.out.println("Path: " + path + " modelAttribute: " + modelAttribute);
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendModelAttribute(modelAttribute, uri);
                modelAttribute.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All model attributes processed.");
    }

    private Long sendModelAttribute(ModelAttribute modelAttribute, URI uri) throws IOException, InterruptedException {
        String modelAttributeJson;
        try {
            modelAttributeJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(modelAttribute);
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", modelAttribute));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(modelAttributeJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendCollections() {
        for (Collection collection : parsedData.getCollectionSet()) {
            try {
                URI uri;
                String path = "/api/v1/collections";
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendCollection(collection, uri);
                collection.setId(receivedId);
                sendCollectionModels(collection);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All collections processed.");
    }

    private Long sendCollection(Collection collection, URI uri) throws IOException, InterruptedException {
        String collectionDtoJson;
        CollectionDto dto = new CollectionDto(collection.getId(), collection.getName(), collection.getImageLink());
        try {
            collectionDtoJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(dto);
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", dto));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(collectionDtoJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.reader().readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendCollectionModels(Collection collection) {
        for (Model model : collection.getModels()) {
            try {
                URI uri;
                String path = String.format("/api/v1/collections/%d?modelId=%d", collection.getId(), model.getId());
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                sendCollectionModel(uri);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
    }

    private void sendCollectionModel(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
    }

    private void sendModelImages() {
        for (ModelImage modelImage : parsedData.getModelImageSet()) {
            try {
                URI uri;
                String path = "/api/v1/images/upload";
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendModelImage(modelImage, uri);
                modelImage.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All models processed.");
    }

    private Long sendModelImage(ModelImage modelImage, URI uri) throws IOException, InterruptedException {
        String modelImageJson;
        try {
            modelImageJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(new ModelImageDto(modelImage.getModel().getId(), modelImage.getImageLink()));
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", modelImage));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(modelImageJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private void sendSales() {
        for (Sale sale : parsedData.getSaleMap().values()) {
            try {
                URI uri;
                String path = "/api/v1/sale";
                try {
                    uri = new URI(domain + path);
                } catch (URISyntaxException exc) {
                    System.out.println("Wrong URI syntax. Check domain and path.");
                    return;
                }
                Long receivedId = sendSale(sale, uri);
                sale.setId(receivedId);
            } catch (IOException | InterruptedException exc) {
                System.out.printf(exc.getMessage());
            }
        }
        System.out.println("All sales processed.");
    }

    private Long sendSale(Sale sale, URI uri) throws IOException, InterruptedException {
        String saleJson;
        try {
            saleJson = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(new SaleDto(sale.getModel().getId(), sale.getPercent()));
        } catch (JsonProcessingException exc) {
            throw new IOException(String.format("Fail to process to JSON: %s%n", sale));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(saleJson))
                .build();
        HttpResponse<String> response = sendRequest(request);
        checkResponseCode(response);
        try {
            return mapper.readTree(response.body()).get("id").asLong();
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to get id from JSON body: %s%n", response.body()));
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        try {
            requestCount++;
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exc) {
            throw new IOException(String.format("Fail to send request: %s%n", request));
        } catch (InterruptedException exc) {
            throw new InterruptedException("Sending http request was interrupted.");
        }
    }

    private void showCount() {
        System.out.printf("Current request count is: %d.%n", requestCount);
    }

    private void chooseDomain() {
        System.out.println("Enter domain: ");
        domain = scanner.nextLine();
        System.out.println("Domain saved.");
    }

    private void checkResponseCode(HttpResponse<String> response) throws IOException {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException(String.format("%nHttp response code %d received. %nBody: %s. %nHeaders: %s%n",
                    code, response.body(), response.headers().map().toString()));
        }
    }
}
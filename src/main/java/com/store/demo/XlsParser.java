package com.store.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class XlsParser {
    private final ParsedData parsedData;
    private final XlsCategoryParser xlsCategoryParser;
    private final XlsModelParser xlsModelParser;
    private final XlsCollectionParser xlsCollectionParser;
    private final XlsSaleParser xlsSaleParser;
    private final DataSender dataSender;

    private final Scanner scanner = new Scanner(System.in);
    private Path rootPath;

    public XlsParser() {
        parsedData = new ParsedData();
        xlsCategoryParser = new XlsCategoryParser(parsedData);
        xlsModelParser = new XlsModelParser(parsedData);
        xlsSaleParser = new XlsSaleParser(parsedData);
        this.xlsCollectionParser = new XlsCollectionParser(parsedData);
        dataSender = new DataSender(parsedData);
    }

    public void start() {
        System.out.println("Welcome to app!");
        chooseRoot();
        processRequests();
    }

    private void processRequests() {
        char userCommand;
        do {
            showMainMenu();
            userCommand = scanner.nextLine().charAt(0);
            try {
                switch (userCommand) {
                    case '1' -> {
                        Path path = selectFileToParse();
                        if (path == null) {
                            continue;
                        }
                        xlsCategoryParser.parseCategoriesData(path);
                    }
                    case '2' -> {
                        Path path = selectFileToParse();
                        if (path == null) {
                            continue;
                        }
                        xlsModelParser.parseModels(path);
                    }
                    case '3' -> {
                        Path path = selectFileToParse();
                        if (path == null) {
                            continue;
                        }
                        xlsCollectionParser.parseCollections(path);
                    }
                    case '4' -> {
                        Path path = selectFileToParse();
                        if (path == null) {
                            continue;
                        }
                        xlsSaleParser.parseSales(path);
                    }
                    case '5' -> transferDataToSever();
                    case '0' -> chooseRoot();
                }
            } catch (IOException exc) {
                System.out.printf("Exception while parsing occurred: %s. %n", exc.getMessage());
            }
        } while (userCommand != 'q');
        System.out.println("Thanks for using app.");
    }

    private void transferDataToSever() {
        dataSender.processRequests();
    }


    private void showMainMenu() {
        System.out.println("\nPick any option:");
        System.out.println("1. Parse category hierarchy");
        System.out.println("2. Parse model data");
        System.out.println("3. Parse collection data");
        System.out.println("4. Parse sale data");
        System.out.println("5. Transfer data to client");
        System.out.println("0. Change root directory.");
        System.out.println("Type \"q\" to exit.");
    }

    private void chooseRoot() {
        System.out.println("Enter root with files to parse:");
        String path = scanner.nextLine();
        this.rootPath = Paths.get(path);
        System.out.println("Root saved.");
    }

    private Path selectFileToParse() throws IOException {
        int command = 0;
        List<Path> files;
        do {
            try {
                files = Files.list(rootPath)
                        .filter(path -> !Files.isDirectory(path))
                        .filter(path -> {
                            String pathName = path.getFileName().toString();
                            String extension = pathName.substring(pathName.lastIndexOf('.') + 1);
                            return extension.equals("xlsx");
                        }).toList();
            } catch (IOException exc) {
                throw new IOException("Root not found.");
            }
            if (files.size() == 0) {
                System.out.println("No files found.");
                return null;
            }
            System.out.println("\nChoose any file:");
            for (int i = 0; i < files.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, files.get(i).getFileName().toString());
            }
            String userInput = scanner.nextLine();
            command = parseIntCommand(userInput, files.size());
        } while (command == 0);
        return files.get(command - 1);
    }

    private int parseIntCommand(String userInput, int filesNumber) {
        int command = 0;
        if (userInput == null || userInput.isBlank() || userInput.length() != 1) {
            return 0;
        } else {
            try {
                command = Integer.parseInt(userInput);
            } catch (NumberFormatException exc) {
                return 0;
            }
            if (command <= 0 || command > filesNumber) {
                return 0;
            }
        }
        return command;
    }
}

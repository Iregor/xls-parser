package com.store.demo;

import com.store.demo.models.ParsedData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class XlsParser {
    private final ParsedData parsedData = new ParsedData();
    private final Scanner scanner = new Scanner(System.in);
    private Path rootPath;

    public void start() {
        System.out.println("Welcome to app!");
        processRequests();
    }

    private void processRequests() {
        chooseRoot();
        char userCommand;
        while ((userCommand = scanner.nextLine().charAt(0)) != 'q') {

        }
        System.out.println("Thanks for using app.");
    }

    private void showMainMenu() {
        System.out.println("Pick any option:");
        System.out.println("1. Parse category hierarchy");
        System.out.println("2. Parse model data");
        System.out.println("3. Transfer data to client");
        System.out.println("0. Change root directory.");
        System.out.println("Type \"q\" to exit.");
    }

    private void chooseRoot() {
        System.out.println("Enter root with files to parse.");
        String path = scanner.nextLine();
        this.rootPath = Paths.get(path);
        System.out.println("Root saved.");
    }

    private void selectFile() {
        System.out.println("Enter root to find files.");
    }

    private Path selectFileToParse() throws IOException {
        int command = 0;
        List<Path> files;
        do {
            System.out.println("\nChoose any file:");
            files = Files.list(rootPath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> {
                        String pathName = path.getFileName().toString();
                        String extension = pathName.substring(pathName.lastIndexOf('.') + 1);
                        return !extension.equals("xlsx");
                    }).toList();
            for (int i = 1; i <= files.size(); i++) {
                System.out.printf("%d. %s%n", i, files.get(i).getFileName().toString());
            }
            String userInput = scanner.nextLine();
            command = parseIntCommand(userInput, files.size());
        } while (command == 0);
        return files.get(command);
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

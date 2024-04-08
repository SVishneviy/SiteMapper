package org.example;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class Application {
    private static final String filePath = "output/site-map.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Input URL: ");
        String url = normalizeUrlWithSlash(scanner.nextLine());

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        Link link = forkJoinPool.invoke(new LinkCrawlerTask(url, 10));

        if (!link.getUrl().isEmpty()) {
            while (true) {
                displayMethods();
                System.out.print("Input number: ");
                try {
                    int option = scanner.nextInt();
                    switch (option) {
                        case 0 -> System.exit(0);
                        case 1 -> displaySiteMapHierarchy(link, "");
                        case 2 -> {
                            List<String> list = new ArrayList<>();

                            buildSiteMapList(link, "", list);

                            boolean isSaved = saveSiteMapToFile(list);
                            if (isSaved) {
                                System.out.println("Site map saved to: " + filePath);
                            } else {
                                System.out.println("Failed to save the site map." +
                                        " Please check the log for more information.");
                            }
                            System.exit(0);
                        }
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine();
                }
            }
        } else {
            System.out.println("Failed to fetch the site map." +
                    " Please check the log for more information.");
        }
    }

    private static boolean saveSiteMapToFile(List<String> list) {
        try {
            Files.write(Paths.get(filePath), list);
        } catch (IOException e) {
            log.error("Error saving site map to file", e);
            return false;
        }
        return true;
    }

    private static void buildSiteMapList(Link link, String indent, List<String> list) {
        list.add(indent + link.getUrl());
        link.getChildren()
                .forEach(l -> buildSiteMapList(l, indent + "\t", list));
    }

    private static void displaySiteMapHierarchy(Link link, String indent) {
        System.out.println(indent + link.getUrl());
        link.getChildren()
                .forEach(l -> displaySiteMapHierarchy(l, indent + "\t"));
    }

    private static String normalizeUrlWithSlash(String url) {
        return !url.endsWith("/")
                ? url + "/"
                : url.replaceAll("/+$", "/");
    }

    private static void displayMethods() {
        String listMethods =
                """
                                        
                        || -- Methods -- ||
                        1. Display site map hierarchy
                        2. Save site map to file
                        0. Exit
                        || ------------- ||
                        """;
        System.out.println(listMethods);
    }
}
package com.company;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static List<String>[] runtimes = new ArrayList[1000];
    private static List<String> japaneseMovies = new ArrayList<>();
    private static Map<String, List<String>> yearList = new HashMap<>();
    public static void main(String[] args) throws IOException {
        Instant start = Instant.now();
//
//        List<String> immutableList = List.of("5 five", "1 one", "2 two", "3 three");
//        for (String s : immutableList) {
//            String[] t =  s.split("\\s+");
//            int index = Integer.valueOf(t[0]);
//            if (runtimes[index] == null) {
//                runtimes[index] = new ArrayList<>();
//            }
//            runtimes[index].add(s);
//        }
//        for (int i = 0; i < runtimes.length; i++) {
//            if (runtimes[i] != null) {
//                for(String r : runtimes[i]) {
//                    System.out.println(r);
//                }
//            }
//        }

//        doit();
        stereophile("https://www.stereophile.com/content/recommended-components-fall-2021-edition-loudspeaker-systems");
        stereophile("https://www.stereophile.com/content/recommended-components-fall-2021-edition-integrated-amplifiers-receivers");

        Instant end = Instant.now();
        System.out.println("ran in " + Duration.between(start, end).toSeconds() + " seconds.");

    }

    private static void doit() throws IOException {

        var url = new URL("https://sivers.org/ebert2");
        try (var br = new BufferedReader(new InputStreamReader(url.openStream()))) {

            String line;

            var sb = new StringBuilder();

            while ((line = br.readLine()) != null) {

                if (line.startsWith("<li>")) {

                    String movie = line.substring(line.indexOf("\">")+2, line.indexOf("</a>"));
                    String movieURL = line.substring(line.indexOf("=")+2, line.indexOf("\">"));
                    String year = movieURL.substring(movieURL.lastIndexOf('-') + 1);
                    movieURL = movieURL.replace("http", "https");

//                    System.out.println(movie);
                    String minutes = getMinutes(movieURL, movie);
                    sb.append(movie).append(" - ").append(minutes).append(" (").append(year).append(")");
//                    runningTimeList.add(minutes + " - " + movie);
//                    System.out.println("|" + minutes + "|" + movie);
                    int index = 0;
                    if (minutes != null && !minutes.trim().isEmpty()) {
                        String[] t = minutes.trim().split("\\s+");
                        index = Integer.valueOf(t[0]);
                    }
                    if (runtimes[index] == null) {
                        runtimes[index] = new ArrayList<>();
                    }
                    runtimes[index].add(minutes + " " + movie);

                    List<String> yearEntry = yearList.getOrDefault(year, new ArrayList<>());
                    yearEntry.add(movie + " " + minutes);
                    yearList.put(year, yearEntry);

                    sb.append(System.lineSeparator());
                }
            }

            System.out.println(sb);
        }

        System.out.println("======== running time ==========");
        for (int i = 0; i < runtimes.length; i++) {
            if (runtimes[i] != null) {
                for(String r : runtimes[i]) {
                    System.out.println(r);
                }
            }
        }

//        Map<String, Integer> result = yearList.entrySet().stream()
//            .sorted(Map.Entry.comparingByKey())
//            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        System.out.println("======== by year ==========");
        Map<String, List<String>> result = yearList.entrySet()
            .stream()
            .sorted(Map.Entry.<String, List<String>>comparingByKey())
//            .forEach(System.out::println);
            .collect(toMap(Map.Entry::getKey,
                Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)); // to preserve sorted insert order
        for(Map.Entry<String, List<String>>  r : result.entrySet()) {
            for(String yearEntry : r.getValue()) {
                System.out.println(r.getKey() + " " + yearEntry);
            }
        }

        System.out.println("======== japanese ==========");
        for(String j : japaneseMovies) {
            System.out.println(j);
        }

    }
    private static String getMinutes(String urlString, String movie) throws IOException {
        var sb = new StringBuilder();
        var url = new URL(urlString);
        boolean japanese = false;

        try (var br = new BufferedReader(new InputStreamReader(url.openStream()))) {

            String line;

            boolean readNextLine = false;
            while ((line = br.readLine()) != null) {

                if (readNextLine) {
                    sb.append(line);
                    readNextLine = false;
                    if (japanese)
                        break;
                }
                if (line.contains("cast-and-crew--running-time")) {

                    readNextLine = true;
                }
                if (line.contains("apan") && !japanese) {
//                    sb.append(" (maybe japanese)");
                    japaneseMovies.add(movie);
                    japanese = true;
                }
//                if (japanese && readNextLine)
//                    break;

            }

        }

        return sb.toString();
    }

    private static void stereophile(String urlString) throws IOException {

        var url = new URL(urlString);
        try (var br = new BufferedReader(new InputStreamReader(url.openStream()))) {

            String line;

            var sb = new StringBuilder();
            boolean newSection = false;
            Map<String, StringBuilder> budgetItems = new LinkedHashMap<>();
            Map<String, StringBuilder> starredItems = new LinkedHashMap<>();
            String sectionName = "";
            while ((line = br.readLine()) != null) {

                if (line.startsWith("<span class='product_grade'>")) {
                    sectionName = line.substring(line.indexOf(">")+1, line.indexOf("</span>"));
                    newSection = true;
                    sb.append(sectionName).append(System.lineSeparator());
                    budgetItems.put(sectionName, budgetItems.getOrDefault(sectionName, new StringBuilder()));
                    starredItems.put(sectionName, starredItems.getOrDefault(sectionName, new StringBuilder()));
                    continue;
                }
                if (newSection && line.startsWith("<B>")) {

                    String item = line.substring(line.indexOf(">")+1, line.indexOf("</B>"));
                    item = item.replace("&#9733", "[starred]");
                    sb.append(item).append(System.lineSeparator());

                    if (item.contains("$$$")) {
//                        System.out.println(" detected $$$ " + item);
//                        System.out.println(" getting " + sectionName + " " +  budgetItems.get(sectionName));
                        budgetItems.get(sectionName).append(System.lineSeparator()).append(item);
                    }
                    if (item.contains("starred")) {
//                        System.out.println(" detected $$$ " + item);
//                        System.out.println(" getting " + sectionName + " " +  budgetItems.get(sectionName));
                        starredItems.get(sectionName).append(System.lineSeparator()).append(item);
                    }
//                    sb.append(System.lineSeparator());
                }
            }

            System.out.println(sb);
            System.out.println("======= budget items ==============");
//            System.out.println(budgetItems);
            for(Map.Entry<String, StringBuilder>  r : budgetItems.entrySet()) {
                System.out.println(r.getKey() + " " + r.getValue().toString());
            }

            System.out.println("======= starred items ==============");
//            System.out.println(budgetItems);
            for(Map.Entry<String, StringBuilder>  r : starredItems.entrySet()) {
                System.out.println(r.getKey() + " " + r.getValue().toString());
            }
        }


    }
}

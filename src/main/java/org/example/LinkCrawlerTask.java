package org.example;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class LinkCrawlerTask extends RecursiveTask<Link> {
    private final String url;
    private final int maxLinkLimit;
    private final Link rootLink;
    private final List<String> forbiddenPatterns = new ArrayList<>(List.of("?", "&", "#"));

    public LinkCrawlerTask(String url) {
        this.url = url;
        maxLinkLimit = Integer.MAX_VALUE;
        rootLink = new Link(url, null);
    }

    public LinkCrawlerTask(String url, int maxLinkLimit) {
        this.url = url;
        this.maxLinkLimit = maxLinkLimit;
        rootLink = new Link(url, null);
    }

    public LinkCrawlerTask(String url, Link rootLink) {
        this.url = url;
        this.rootLink = rootLink;
        maxLinkLimit = Integer.MAX_VALUE;
    }

    public LinkCrawlerTask(String url, Link rootLink, int maxLinkLimit) {
        this.url = url;
        this.rootLink = rootLink;
        this.maxLinkLimit = maxLinkLimit;
    }

    @Override
    protected Link compute() {
        Document document;
        try {
            Thread.sleep(150);
            document = Jsoup.connect(url).get();

            Set<String> links = parseLinks(document.select("a"));

            ForkJoinTask.invokeAll(createSubtasks(links)).stream()
                    .map(ForkJoinTask::join)
                    .filter(l -> !l.getUrl().isEmpty())
                    .forEach(rootLink::addChildren);

        } catch (IOException e) {
            log.error("Error while processing URL: {}", e.getMessage());
            return rootLink.setUrl("");
        } catch (InterruptedException e) {
            log.error("Thread execution interrupted", e);
            Thread.currentThread().interrupt();
        }

        return rootLink;
    }

    private List<LinkCrawlerTask> createSubtasks(Set<String> links) {
        return links.stream()
                .map(link -> new LinkCrawlerTask(link, new Link(link, rootLink)))
                .collect(Collectors.toList());
    }

    private Set<String> parseLinks(Elements elements) {
        return elements.stream()
                .map(el -> el.attr("abs:href"))
                .limit(maxLinkLimit)
                .filter(this::isLinkValid)
                .map(link -> {
                    String path = link.substring(url.length());
                    return url + path.split("/")[0] + "/";
                })
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private boolean isLinkValid(String link) {
        return link.startsWith(url)
                && !link.equals(url)
                && forbiddenPatterns.stream().noneMatch(link::contains);
    }
}

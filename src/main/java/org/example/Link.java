package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public final class Link {
    private final String url;
    private final Link parent;
    private final Set<Link> children = new LinkedHashSet<>();

    public Link setUrl(String url) {
        return new Link(url, parent);
    }

    public void addChildren(Link link) {
        children.add(link);
    }
}

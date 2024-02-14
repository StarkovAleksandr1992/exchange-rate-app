package ru.starkov.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlParser {
    private UrlParser() {
    }

    public static Map<String, String> parseUrl(HttpServletRequest req) throws URISyntaxException {
        URI uri = new URI(req.getRequestURI());
        String query = req.getQueryString();
        if (query != null) {
            return parseQueryParameters(query);
        }
        return Map.of();
    }

    private static List<String> decodePathSegments(String path) {
        List<String> segments = new ArrayList<>();
        String[] pathSegments = path.split("/");
        for (String segment : pathSegments) {
            segments.add(URLDecoder.decode(segment, StandardCharsets.UTF_8));
        }
        return segments;
    }

    private static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> segments = new HashMap<>();
        for (String param : query.split("&")) {
            String[] nameValue = parseNameValue(param);
            segments.put(nameValue[0], nameValue[1]);
        }
        return segments;
    }

    private static String[] parseNameValue(String qParam) {
        int pos = qParam.indexOf("=");
        if (pos <= 0) {
            throw new IllegalArgumentException("Can't find equals symbol between name and value");
        }
        return new String[]{qParam.substring(0, pos), qParam.substring(pos + 1)};
    }
}

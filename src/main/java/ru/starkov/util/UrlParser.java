package ru.starkov.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UrlParser {
    private UrlParser() {
    }

    public static List<QueryParameter> parseUrl(HttpServletRequest req) throws URISyntaxException {
        URI uri = new URI(req.getRequestURI());
        List<QueryParameter> segments = new ArrayList<>();
        String query = req.getQueryString();
        if (query != null) {
            segments.addAll(parseQueryParameters(query));
        }
        return segments;
    }

    private static List<String> decodePathSegments(String path) {
        List<String> segments = new ArrayList<>();
        String[] pathSegments = path.split("/");
        for (String segment : pathSegments) {
            segments.add(URLDecoder.decode(segment, StandardCharsets.UTF_8));
        }
        return segments;
    }

    private static List<QueryParameter> parseQueryParameters(String query) {
        List<QueryParameter> segments = new ArrayList<>();
        for (String param : query.split("&")) {
            String[] nameValue = parseNameValue(param);
            segments.add(new QueryParameter(nameValue[0], nameValue[1]));
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

    public record QueryParameter(String key, String value) {}
}

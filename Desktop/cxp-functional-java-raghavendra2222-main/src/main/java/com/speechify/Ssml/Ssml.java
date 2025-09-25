package com.speechify.Ssml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * SSML (Speech Synthesis Markup Language) is a subset of XML specifically
 * designed for controlling synthesis. You can see examples of how the SSML
 * should be parsed in com.speechify.SSMLTest in `src/test/java/SSMLTest.java`.
 *
 * You may:
 *  - Read online guides to supplement information given in com.speechify.SSMLTest to understand SSML syntax.
 * You must not:
 *  - Use XML parsing libraries or the DocumentBuilderFactory. The task should be solved only using string manipulation.
 *  - Read guides about how to code an XML or SSML parser.
 */
public class Ssml {

    // Parses SSML to a SSMLNode, throwing on invalid SSML
    public static SSMLNode parseSSML(String ssml) {
        // NOTE: Don't forget to run unescapeXMLChars on the SSMLText
        if(ssml == null) ssml = "";
        Parser p = new Parser(ssml);
        SSMLNode node;
        try{
            p.skipWs();
            node = p.parseElement();
            p.skipWs();
            if(!p.eof()) throw new IllegalArgumentException("Tags could not be parsed");
        } catch(IllegalArgumentException ex) {
            if(!"Attributes could not be Parsed".equals(ex.getMessage())){
                throw new IllegalArgumentException("Tags could not be parsed");
            }
            throw ex;
        }
        if(!(node instanceof SSMLElement) || !"speak".equals(((SSMLElement) node).name)) {
            throw new IllegalArgumentException("Tags could not be parsed");
        }
        return node;
    }

    // Recursively converts SSML node to string and unescapes XML chars
    public static String ssmlNodeToText(SSMLNode node) {
        StringBuilder sb = new StringBuilder();
        toText(node, sb);
        return sb.toString();
    }

    // Already done for you
    public static String unescapeXMLChars(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    public sealed interface SSMLNode permits SSMLElement, SSMLText {}

    public record SSMLElement(String name, List<SSMLAttribute> attributes, List<SSMLNode> children) implements SSMLNode {}

    public record SSMLAttribute(String name, String value) {}

    public record SSMLText(String text) implements SSMLNode {}

    private static void toText(SSMLNode node, StringBuilder sb) {
        if (node instanceof SSMLText t) {
            sb.append(t.text);
            return;
        } if (node instanceof SSMLElement el) {
            for (SSMLNode c : el.children) toText(c, sb);
        }
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
            this.i = 0;
        }

        boolean eof() {
            return i >= s.length();
        }
        void skipWs() {
            while (!eof() && Character.isWhitespace(s.charAt(i))) i++;
        }
        char ch() {
            return s.charAt(i);
        }
        boolean startsWith(String x) {
            return s.startsWith(x, i);
        }

        SSMLNode parseElement() {
            if(eof() || ch() != '<') throw new IllegalArgumentException("Tags could not be parsed");
            i++;
            String name = parseName();
            if(name.isEmpty()) throw new IllegalArgumentException("Tags could not be parsed");

            skipWs();
            List<SSMLAttribute> attrs = parseAttributes();
            skipWs();

            if(eof() || ch() != '>') throw new IllegalArgumentException("Tags could not be parsed");
            i++;

            List<SSMLNode> children = new ArrayList<>();
            while(true) {
                if(eof()) throw new IllegalArgumentException("Tags could not be parsed");
                if(startsWith("</")) {
                    i += 2;
                    String close = parseName();
                    skipWs();
                    if(!name.equals(close)) throw new IllegalArgumentException("Tags could not be parsed");
                    if(eof() || ch() != '>') throw new IllegalArgumentException("Tags could not be parsed");
                    i++;
                    break;
                } else if(ch() == '<') {
                    children.add(parseElement());
                } else {
                    children.add(parseText());
                }
            }
            return new SSMLElement(name, Collections.unmodifiableList(attrs), Collections.unmodifiableList(children));
        }

        SSMLText parseText() {
            int start = i;
            while(!eof() && ch() != '<') i++;
            String raw = s.substring(start, i);
            return new SSMLText(unescapeXMLChars(raw));
        }

        String parseName() {
            int start = i;
            if(eof()) return "";
            char c = ch();
            if(!(Character.isLetter(c) || c == '_')) return "";
            i++;
            while(!eof()) {
                c = ch();
                if(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
                    i++;
                } else {
                    break;
                }
            }
            return s.substring(start, i);
        }

        List<SSMLAttribute> parseAttributes() {
            List<SSMLAttribute> list = new ArrayList<>();
            while(true) {
                skipWs();
                if(eof()) throw new IllegalArgumentException("Attributes could not be Parsed");
                if(ch() == '>' || startsWith("/>")) break;

                String name = parseName();
                if(name.isEmpty()) throw new IllegalArgumentException("Attributes could not be Parsed");
                skipWs();
                if(eof() || ch() != '=') throw new IllegalArgumentException("Attributes could not be Parsed");
                i++;
                skipWs();
                if(eof() || ch() != '"') throw new IllegalArgumentException("Attributes could not be Parsed");
                i++;
                int start = i;
                while(!eof() && ch() != '"') i++;
                if(eof()) throw new IllegalArgumentException("Attributes could not be Parsed");
                String value = s.substring(start, i);
                i++;
                list.add(new SSMLAttribute(name, value));
            }
            return list;
            
        }
    }
}

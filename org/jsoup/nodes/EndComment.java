package org.jsoup.nodes;

// zhijia added
public class EndComment extends Node {
    private static final String ENDCOMMENT_KEY = "endComment";

    public EndComment(String data, String baseUri) {
        super(baseUri);
        attributes.put(ENDCOMMENT_KEY, data);
    }

    public String nodeName() {
        return "#endComment";
    }

    public String getData() {
        return attributes.get(ENDCOMMENT_KEY);
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (out.prettyPrint())
            indent(accum, depth, out);
        accum.append(getData()).append("-->");
    }

    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
    }

    public String toString() {
        return outerHtml();
    }

    @Override
    public String nodeType() {
        return "EndComment";
    }
}

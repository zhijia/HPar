package org.jsoup.nodes;


// zhijia added
public class StartComment extends Node {
	private static final String STARTCOMMENT_KEY = "startComment";
	
    public StartComment(String data, String baseUri) {
        super(baseUri);
        attributes.put(STARTCOMMENT_KEY, data);
    }
	
    public String nodeName() {
        return "#startComment";
    }
    
    public String getData() {
        return attributes.get(STARTCOMMENT_KEY);
    }
    
    public void setData(String data) {
    	attributes.put(STARTCOMMENT_KEY, data);
    }
    
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (out.prettyPrint())
            indent(accum, depth, out);
        accum
                .append("<!--")
                .append(getData());
    }
    
    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {}

    public String toString() {
        return outerHtml();
    }
    
	@Override
	public String nodeType() {
		return "StartComment";
	}
}

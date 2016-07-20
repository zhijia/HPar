package org.jsoup.examples;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.*;
import java.util.*;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to
 * convert HTML input to lightly-formatted plain-text. That is divergent from
 * the general goal of jsoup's .text() methods, which is to get clean data from
 * a scrape.
 * <p/>
 * Note that this is a fairly simplistic formatter -- for real world use you'll
 * want to embrace and extend.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class HtmlToPlainText {

    public static void main(String... args) throws IOException {
        Validate.isTrue(args.length == 2, "usage: html numThreads");
        int numThreads = Integer.parseInt(args[1]);

        String html = "";
        StringBuilder sb = new StringBuilder();
        
        long start, duration; /* time profiling variables */
        
        start = System.currentTimeMillis();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(args[0]));
            String line = "";
            while((line = in.readLine()) != null)
            	 sb.append(line);
        }catch(IOException e){
            e.printStackTrace();
        }finally{
                in.close();
        }
        html = sb.toString();
        duration = System.currentTimeMillis() - start;
        //System.out.println("loading html: " + duration + " ms");

        
        /* measure time */
        Document doc = null;
        final int WARMUP = 20;
        int warmup = WARMUP;
        while(warmup-- > 0) {
            ParallelParser pparser = new ParallelParser(html, numThreads);
            doc = pparser.parse();
        }
        final int RUNS = 20;
        int runs = RUNS;
        float total = 0;
        while(runs-- > 0) {
            ParallelParser pparser = new ParallelParser(html, numThreads);
            start = System.currentTimeMillis();
            doc = pparser.parse();
            duration = System.currentTimeMillis() - start;
            total += duration;
        }
        System.out.println("total: " + total/RUNS + " ms");

        PrintWriter out = new PrintWriter("test/output.html");
        out.println(doc);
        out.close();
    }

    /**
     * Format an Element to plain-text
     * 
     * @param element
     *            the root element to format
     * @return formatted text
     */
    public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and
                                     // .tail() for each node

        return formatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the
                                                           // accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all
                                                  // user-readable text in the
                                                  // DOM.
            else if (name.equals("li"))
                append("\n * ");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("br"))
                append("\n");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from
                           // formats above, not in natural text
            if (text.equals(" ")
                    && (accum.length() == 0 || StringUtil.in(
                            accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset
                                                            // counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        public String toString() {
            return accum.toString();
        }
    }
}

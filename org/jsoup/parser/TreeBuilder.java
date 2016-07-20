package org.jsoup.parser;

//import java.io.*;
import java.util.Stack;

import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.*;


/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {
    CharacterReader reader;
    Tokeniser tokeniser;
    protected Document doc; // current doc we are building into
    protected DescendableLinkedList<Element> stack; // the stack of open elements
    protected String baseUri; // current base uri, for creating new elements
    protected Token currentToken; // currentToken is used only for error tracking.
    protected ParseErrorList errors; // null when not tracking errors    
    // zhijia add input
    protected String input;
    boolean resetFlag;
    
    protected void initialiseParse(String input, String baseUri, ParseErrorList errors) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new Document(baseUri);
        reader = new CharacterReader(input, this);
        this.errors = errors;
        tokeniser = new Tokeniser(reader, errors);
        stack = new DescendableLinkedList<Element>();
        this.baseUri = baseUri;
        this.input = input;
        this.resetFlag = false;
    }
    
    void reset() {
    	doc.body().removeChildNodes();
    	reader = new CharacterReader(input, this);
    	this.errors = ParseErrorList.tracking(100);
    	tokeniser = new Tokeniser(reader, errors);
    	while(stack.size() > 2)
    		stack.pollLast();
    }

    Document parse(String input, String baseUri) {
        return parse(input, baseUri, ParseErrorList.tracking(100));
    }

    Document parse(String input, String baseUri, ParseErrorList errors) {
    	initialiseParse(input, baseUri, errors);
    	runParser();
        
        //System.out.println("number of errors: "+errors.size());
        for(int i = 0; i < errors.size(); i++)
        	System.out.println(errors.get(i));
        
        return doc;
    }

    
    protected void runParser() {
        while (true) {   	
            Token token = tokeniser.read();
			// if(Thread.currentThread().getName().equals("1"))
			// System.out.println("type: "+token.tokenType()+" token: "+token);
            process(token);
            
            if (token.type == Token.TokenType.EOF)
                break;
        }
        updateDoc();
        //System.out.println(tokeniser.getReader().getProfileData());
    }
    
    // update doc tree according to open stack emlement
    void updateDoc() {
        Node root = (Node) doc;
        Stack<Node> shadow = new Stack<Node>();

        // get html Element
        Node rightMost = null;
        for (int i = 0; i < root.childNodesAsArray().length; i++) {
            if (root.childNode(i).nodeName().endsWith("html")) {
                rightMost = root.childNode(i);
                break;
            }
        }

        shadow.push(rightMost);
        // get body Element, considering body1
        while (rightMost.childNodesAsArray().length > 0) {
            rightMost = rightMost.childNodesAsArray()[rightMost.childNodesAsArray().length - 1];
            shadow.push(rightMost);
        }
        shadow.push(rightMost);

        // pop out the elements those are supposed to be complete
        while (shadow.size() > stack.size())
            shadow.pop();

        while (shadow.size() > 0) {
            Node current = shadow.pop();
            // System.out.println("stack.peekLast().nodeName(): "+stack.peekLast().nodeName());
            // System.out.println("current.nodeName()         : "+current.nodeName());
            if (stack.peekLast().nodeName().equals("body")
                    || stack.peekLast().nodeName().equals("head")
                    || current.nodeName().equals("body") || current.nodeName().equals("head"))
                break;

            if (current.nodeName().equals(stack.peekLast().nodeName())) {
                stack.pollLast();
                ((Element) current).onlyStartTag = true;
            } else {
                System.out.println(Thread.currentThread().getName()
                        + " err: stack doesn't match with shadow stack");
            }
        }

        // FileWriter file = null;
        // try {
        // file = new FileWriter(Thread.currentThread().getName()+"doc.html");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // PrintWriter out = new PrintWriter(file);
        // out.println(doc);
        // out.close();
    }

    protected abstract boolean process(Token token);

    protected Element currentElement() {
        return stack.getLast();
    }
}

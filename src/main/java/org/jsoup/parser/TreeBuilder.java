package org.jsoup.parser;

import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.*;

import java.util.*;


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
    
    protected void initialiseParse(String input, String baseUri, ParseErrorList errors) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new Document(baseUri);
        reader = new CharacterReader(input);
        this.errors = errors;
        tokeniser = new Tokeniser(reader, errors);
        stack = new DescendableLinkedList<Element>();
        this.baseUri = baseUri;
    }

    Document parse(String input, String baseUri) {
        return parse(input, baseUri, ParseErrorList.tracking(100));
    }

    Document parse(String input, String baseUri, ParseErrorList errors) {
    	initialiseParse(input, baseUri, errors);
    	runParser();
        
        System.out.println("number of errors: "+errors.size());
        for(int i = 0; i < errors.size(); i++)
        	System.out.println(errors.get(i));
        
        return doc;
    }

    
    protected void runParser() {
        while (true) {
            Token token = tokeniser.read();
            System.out.println(Thread.currentThread().getName()+" TokenType: "+token.type+"\t  "+token);
            process(token);
            
            if (token.type == Token.TokenType.EOF)
                break;
        }
        updateDoc();
    }
    
    // update doc tree according to open stack emlement
    void updateDoc()
    {
    	Node root = (Node)doc;
    	
    	Node rightMost = root;
    	do {
    		rightMost = rightMost.childNodesAsArray()[rightMost.childNodesAsArray().length - 1];
    	} while(rightMost.childNodesAsArray().length != 0);

    	Node current = rightMost;
    	while(stack.size() > 2)
    	{
    		if(current.nodeName().equals(stack.peekLast().nodeName()))
    		{
    			stack.pollLast();
    			((Element)current).onlyStartTag = true;
    		}
			current = current.parent();
    	}
    }

    protected abstract boolean process(Token token);

    protected Element currentElement() {
        return stack.getLast();
    }
}

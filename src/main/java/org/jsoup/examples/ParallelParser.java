package org.jsoup.examples;

import org.jsoup.Jsoup;
import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.nodes.*;


public class ParallelParser {

	int numThreads;
	String input;
	Document[] docs;
	String[] inputs;
	DescendableLinkedList<Element> stack;	// open emlement stack

	ParallelParser(String input, int numThreads) {
		this.input = input;
		this.numThreads = numThreads;
		inputs = partition(input);
		docs = new Document[numThreads];
	}

	Document parse() {
		
		Thread[] pparsers = new ParserThread[numThreads];
		
		long sta = System.currentTimeMillis();
		for (int i = 0; i < numThreads; i++) {
			pparsers[i] = new ParserThread(i+"", inputs[i]);
			pparsers[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				pparsers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long mid = System.currentTimeMillis();
		Document doc = postprocess(docs);
		long end = System.currentTimeMillis();
		
		System.out.println("parsing time:        "+(mid - sta));
		System.out.println("postprocessing time: "+(end - mid));
		
		return doc;
	}

	String[] partition(String input) {
		String[] inputs = new String[numThreads];
		int length = input.length();
		int start = 0;
		int end = 0;
		int step = length / numThreads;
		for (int i = 0; i < numThreads; i++) {
			start = end;
			end = start + step;
			if (end >= length)
				end = length;
			else {
				// find a good partition point i.e. after '>'
				while (input.charAt(end) != '>') {
					end++;
				}
				end++;
			}
			inputs[i] = input.substring(start, end);
		}
		for (int i = 0; i < numThreads; i++)
			System.out.println("input[" + i + "]" + inputs[i]);

		return inputs;
	}

	class ParserThread extends Thread {
		String input;

		ParserThread(String name, String input) {
			super(name);
			this.input = input;
		}

		public void run() {
			int threadID = Integer.parseInt(getName());
			docs[threadID] = Jsoup.parse(input);
		}
	}

	Document postprocess(Document[] docs) {
		for(int i = 1; i < numThreads; i++)
		{
			System.out.println("merge docs["+i+"]");
			docs[0] = merge(docs[0], docs[i]);
		}
		return docs[0];
	}
	
	// merge doc's body'children to doc0's body
	Document merge(Document doc0, Document doc) {
		
		Element body0 = doc0.body();
		Element body = doc.body();
		
		Node rightMost = getRightMost(body0);
		
		Node[] children = body.childNodesAsArray();
		
		Node current = rightMost;
		System.out.println("children.length: "+ children.length);
		for(int i = 0; i < children.length; i++)
		{
			// move to next start tag
			while(true)
			{				
				if(current.nodeType().equals("Element") == false) {
					current = current.parent();
					continue;
				}
				
				if(((Element)current) == doc0.body())
					break;
				
				if(((Element)current).onlyStartTag == true)
					break;
				current = current.parent();
			}
			
			// if match
			if(current.nodeName().equals(children[i].nodeName())
				&& ((Element)children[i]).onlyEndTag == true)
			{
				((Element)current).onlyStartTag = false;
				continue;
			}
			
			((Element)current).appendChild(children[i]);
			System.out.println("current.nodeName(): "+current.nodeName()+"children[i].nodeName()"+children[i].nodeName());
		}
		
		return doc0;
	}
	
	Node getRightMost(Node root) {
		Node rightMost = root;
    	do {
    		rightMost = rightMost.childNodesAsArray()[rightMost.childNodesAsArray().length - 1];
    	} while(rightMost.childNodesAsArray().length != 0);

		return rightMost;
	}
}

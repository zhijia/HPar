package org.jsoup.examples;

import java.io.*;
import java.util.*;

public class GenData {
	
	ArrayList<Rule> rules;
	static long BASE = 100;
	
	GenData() {
		rules = new ArrayList<Rule>();
		register("ul", "ul ul", 0.8);
		register("ul", "<ul> ul </ul>", 0.1);
		register("ul", "<ul> li </ul>", 0.1);
		register("li", "li li", 0.2);
		register("li", "<li> hello </li>", 0.8);
	}
	
	void register(String left, String right, double prob) {
		Rule r = new Rule(left, right, prob);
		rules.add(r);
	}
	
	String gen() {
		String str = "ul";
		ArrayList<String> usedRules = new ArrayList<String>();
		for(int r = 0; r < rules.size(); r++)
		{
			if(usedRules.contains(rules.get(r)))
				continue;
			str = genSubstring(rules.get(r), str);
			usedRules.add(rules.get(r).left);
			System.out.println("-----------\n"+str);

		}
		return str;
	}
	
	String genSubstring(Rule rule, String str) {
		String left = rule.left;
		ArrayList<Rule> subrules = new ArrayList<Rule>();
		for(int i=0; i<rules.size(); i++)
			if(rules.get(i).left.equals(left))
				subrules.add(rules.get(i));
		
		Random random = new Random(System.nanoTime());
		for(int i=0; i<20; i++)
		{
			double d = random.nextDouble();
			double value = 0.0;
			for(int j=0; j<subrules.size(); j++)
			{
				if(d < value )
				{
					str = apply(subrules.get(j), str);
					break;
				}
				value += subrules.get(j).prob;
			}
			str = apply(subrules.get(subrules.size()-1), str);
		}
		return str;
	}
	
	String apply(Rule r, String str) {
		String newStr = "";
		StringTokenizer tokenizer = new StringTokenizer(str);
		Random random = new Random();
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			double d = random.nextDouble();
			if(d < r.prob && token.equals(r.left)) {
				newStr += " "+r.right;
				continue;
			}
			newStr += " "+token;
		}
		return newStr;
	}
	
	public static void main(String[] args)
	{
		GenData genData = new GenData();
		FileWriter file = null;
		try {
			file = new FileWriter("generated.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		out.println(genData.gen());
		out.close();
		//System.out.println(genData.gen());
		
	}
	
	class Rule {
		String left;
		String right;
		double prob;
		
		Rule(String left, String right, double prob) {
			this.left = left;
			this.right = right;
			this.prob = prob;
		}
	}
}

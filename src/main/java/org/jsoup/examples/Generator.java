package org.jsoup.examples;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Generator {
	
	static String genRepetition(int repet, int depth)
	{
		if(depth-- == 0)
			return "";
		
		String str = "<ul>";
		Random r = new Random();
		int bound = r.nextInt(repet);
		for(int i=0; i<bound; i++)
		{
			if(r.nextDouble() < 0.5)
				str += "<li>hello</li>";
			else
				str += genRepetition(repet, depth);
		}
		str += "</ul>";
		
		return str;
	}
	
	public static void main(String[] args)
	{
		FileWriter file = null;
		try {
			file = new FileWriter("generated.html", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		for(int i=0; i<30; i++)
			out.println(genRepetition(10, 5));
		out.close();
	}
}

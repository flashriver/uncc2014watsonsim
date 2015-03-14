package edu.uncc.cs.watsonsim.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import static edu.uncc.cs.watsonsim.nlp.Trees.concat;
import static edu.uncc.cs.watsonsim.nlp.Trees.parse;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.uncc.cs.watsonsim.Environment;

/**
 * Detect the LAT as the noun in closest proximity to a determiner.
 */
public class ClueType {
	
	public ClueType(Environment env) {
	}
	
	/**
	 * Intermediate results from LAT detection
	 */
	private static final class Analysis {
		public final Tree dt, nn;	// Determiner, Noun// This is from worst to best! That way -1 is the worse-than-worst;
		private static final List<String> DT_RANK = Arrays.asList(new String[]{
				"those", "that", "these", "this"
		});
		public Analysis(Tree d, Tree n){
			dt = d; nn = n;
		}

		/**
		 * Case insensitively rank the LAT's by a predefined order
		 */
		public int rank() {
			if (dt == null) return -1;
			return DT_RANK.indexOf(concat(dt).toLowerCase());
		}
		
		public boolean ok() {
			return dt != null && nn != null;
		}
	}

	/**
	 * Merge two partial LAT analyses.
	 * 1) Favor complete analyses over fragments
	 * 2) Favor specific determiners in a specific order
	 * @return a new immutable partial LAT analysis  
	 */
	private static Analysis merge(Analysis a, Analysis b) {
		if (a.ok() && b.ok()) 	return (a.rank() < b.rank()) ? b : a;
		else if (a.ok())		return a;
		else if (b.ok()) 		return b; 			
		else {
			// Neither are viable. Merge them.
			return new Analysis(
					ObjectUtils.firstNonNull(a.dt, b.dt),
					ObjectUtils.firstNonNull(a.nn, b.nn));
		}
	}
	
	
	/**
	 * A very simple LAT detector. It wants the lowest subtree with both a determiner and a noun
	 */
	private static Analysis detectPart(Tree t) {
		switch (t.value()) {
		case "DT": return new Analysis(t, null);
		case "NN":
		case "NNS": return new Analysis(null, t);
		default:
			Analysis l = new Analysis((Tree) null, null);
			// The last noun tends to be the most general
			List<Tree> kids = t.getChildrenAsList();
			Collections.reverse(kids);
			for (Tree kid : kids)
				l = merge(l, detectPart(kid));
			return l;
		}
	}
	
	/**
	 * Detect the LAT using a simple rule-based approach
	 * @return The most general single-word noun LAT
	 */
	public static String fromClue(Tree t) {
		Analysis lat = detectPart(t);
		if (lat.ok() && lat.rank() >= 0) {
			return concat(lat.nn);
		} else {
			return "";
		}
	}
	
	/**
	 * Detect the LAT using a simple rule-based approach
	 * This is a thin wrapper for use as a string
	 * @return The most general single-word noun LAT
	 */
	public static String fromClue(String text) {
		
		for (CoreMap s : parse(text)) {
			Tree t = s.get(TreeAnnotation.class);
			Analysis lat = detectPart(t);
			if (lat.ok() && lat.rank() >= 0) {
				return concat(lat.nn).toLowerCase();
			}
		}
		return "";
	}
	
	
}

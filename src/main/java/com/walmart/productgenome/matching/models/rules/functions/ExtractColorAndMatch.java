package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class ExtractColorAndMatch extends Function {

	public static final String NAME = "EXTRACT_COLOR_AND_MATCH";
	public static final String DESCRIPTION = "Searches for color keywords from a"
			+ " pre-specified dictionary in the specified attributes and generates"
			+ " a set of colors for each tuple. Then"
			+ "computes a Jaccard score on the two sets of colors";
	public static final int NUM_ARGS = 2;

	public static final String[] COLOR_DICTIONARY = {"60s Butterfly",
		"Anthracite",
		"Aqua Blue",
		"Assorted",
		"Autumn Birds",
		"Ballistic Green",
		"Beige",
		"Black",
		"Black / Black",
		"Black / Red",
		"Black/Blue",
		"Black/Gray",
		"Black/Grey",
		"Black/Pink",
		"Black/Red",
		"Black|Gray",
		"Black|Pink",
		"Blue",
		"Brown",
		"Butterflies 2",
		"Caf",
		"Cafe",
		"Charcoal",
		"Cherry Red",
		"Chocolate",
		"City Life",
		"Clear",
		"Cognac",
		"Cyan, Magenta, Yellow",
		"Dark Blue",
		"Dark Brown",
		"Dark Gray",
		"Deep Red",
		"Drop",
		"Frog",
		"Fuchsia",
		"Giraffe",
		"Gold",
		"Grafiks",
		"Gray",
		"Green",
		"Green Neon Lights",
		"Grey",
		"Gun Metal",
		"Hammock",
		"Hounds Tooth",
		"Ice White",
		"Indigo",
		"Jet Black",
		"Khaki",
		"Leopard",
		"Light Blue",
		"Love Rocks",
		"Magenta",
		"Matrix",
		"Matte Black",
		"Melomania",
		"Midnight",
		"Multi-Color",
		"Multicolor",
		"Navy",
		"Navy Blue",
		"Neon Green",
		"Off-White",
		"Olive",
		"Olive Retro Curves",
		"Onyx",
		"Orange",
		"Paisley 1",
		"Paisley 2",
		"Paisley Blush",
		"Paisley Color",
		"Patience",
		"Peace",
		"Peacock",
		"Pebble Grain Brown",
		"Pink",
		"Pink Dreams",
		"Pink Hearts",
		"Pink Orient",
		"Poppies",
		"Purple",
		"Red",
		"Retro Stripes",
		"Ribbons",
		"Royal Blue",
		"Royal Vintage",
		"Rusty Plaid",
		"Sailor's Delight",
		"Sand",
		"Sapphire Blue",
		"Scarlet",
		"Silver",
		"Skelestar",
		"Sky Blue",
		"Smoke",
		"Spring Flowers",
		"Spring Pink and Lime",
		"Starry Night",
		"Sterling",
		"Love",
		"Tan",
		"Tannin",
		"Teal",
		"Tie Dye Love",
		"Tiger Eyes",
		"Tile",
		"Translucent Mercury",
		"Tropical Textile",
		"Turquoise",
		"Tuscan Black",
		"Tuscan Tan",
		"Vachetta Cafe",
		"Vachetta Tan",
		"Vintage",
		"Vintage Fleur",
		"Violet",
		"White",
		"Yellow",
		"Zebra Eye"};
	
	public ExtractColorAndMatch() {
		super(NAME, DESCRIPTION);
	}

	public ExtractColorAndMatch(String name, String description){
		super(name, description);
	}

	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
		if(args.length != NUM_ARGS){
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}

		if (args[0] == null || args[1] == null) {
			return 0.0f;
		}
		if (args[0].toLowerCase().equals("null") || args[1].toLowerCase().equals("null")) {
			return 0.0f;
		}
		if (args[0].isEmpty() || args[1].isEmpty()) {
			return 0.0f;
		}

		String newArg0 = args[0].toLowerCase();
		String newArg1 = args[1].toLowerCase();

		Set<String> colors0 = new HashSet<String>();
		Set<String> colors1 = new HashSet<String>();
		
		for (String s: COLOR_DICTIONARY) {
			if (newArg0.indexOf(" " + s.toLowerCase() + " ") != -1) {
				colors0.add(s);
				//System.out.println("Adding " + s + " to colors0");
			}
			if (newArg1.indexOf(" " + s.toLowerCase() + " ") != -1) {
				colors1.add(s);
				//System.out.println("Adding " + s + " to colors1");
			}
		}
		
		if (colors0.isEmpty() || colors1.isEmpty()) {
			return 0.0f;
		}
		
		int n = colors0.size() + colors1.size();
		//System.out.println("size0: " + colors0.size() + ", size1: " + colors1.size());
		colors0.retainAll(colors1);
		//System.out.println("size0: " + colors0.size() + ", size1: " + colors1.size());
		return 1.0f * colors0.size() / (n - colors0.size());
	}

	@Override
	public ArgType getArgType() {
		return ArgType.STRING;
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append(",");
		sb.append(this.getDescription());
		sb.append(",");
		sb.append(this.getClass().getName());
		sb.append(",");
		sb.append(Float.class.getName());
		sb.append(",");
		sb.append(NUM_ARGS);
		sb.append(",");
		sb.append(String.class.getName());
		sb.append(",");
		sb.append(String.class.getName());
		return sb.toString();
	}

	public static void main(String[] args){
		Function ecm = new ExtractColorAndMatch();
		String s1 = "[\"This rooCASE sleeve features super shock absorbing "
				+ "interior provide protection from bumps and scratches. Made "
				+ "specifically for iPad Generations 2, 3 & 4 Product Material: "
				+ "Neoprene Product Weight: 0.40 lbs. Laptop Compartment "
				+ "Dimensions: 9.8\" x 7.4\" x .5\" Super bubble shock absorbing "
				+ "foam interior provides protection from bumps and scratches "
				+ "Water resistant neoprene sleeve case Velcro pocket for charger, "
				+ "stylus and earbuds Color matching rubber zipper Designed for "
				+ "naked iPad Generations 2, 3 & 4 Category: iPad Accessories\"]";
		
		String s2 = "[\"This rooCASE sleeve features super shock absorbing "
				+ "interior provide protection from bumps and scratches. Made "
				+ "specifically for iPad Generations 2 3 & 4 Product Material: "
				+ "Neoprene Product Weight: 0.40 lbs. Laptop Compartment "
				+ "Dimensions: 9.8\" x 7.4\" x .5\" Super bubble shock absorbing "
				+ "foam interior provides protection from bumps and scratches "
				+ "Water resistant neoprene sleeve case Velcro pocket for charger "
				+ "stylus and earbuds Color matching rubber zipper Designed for "
				+ "naked iPad Generations 2 3 & 4 Category: iPad Accessories\"]";
		
		String[] params = {s1, s2};
		System.out.println(ecm.compute(params));
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		return new HashSet<AllTypes>();
	}
}

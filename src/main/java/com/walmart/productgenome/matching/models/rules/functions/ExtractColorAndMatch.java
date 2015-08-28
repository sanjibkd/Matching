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

	public static final String[] COLOR_DICTIONARY = {"60s butterfly",
		"anthracite",
		"aqua blue",
		"assorted",
		"autumn birds",
		"ballistic green",
		"beige",
		"black",
		"black / black",
		"black / red",
		"black/blue",
		"black/gray",
		"black/grey",
		"black/pink",
		"black/red",
		"black|gray",
		"black|pink",
		"blue",
		"brown",
		"butterflies 2",
		"caf",
		"cafe",
		"charcoal",
		"cherry red",
		"chocolate",
		"city life",
		"clear",
		"cognac",
		"dark blue",
		"dark brown",
		"dark gray",
		"deep red",
		"drop",
		"frog",
		"fuchsia",
		"giraffe",
		"gold",
		"grafiks",
		"gray",
		"green",
		"green neon lights",
		"grey",
		"gun metal",
		"hammock",
		"hounds tooth",
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
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].toLowerCase().replaceAll("[^\\dA-Za-z\\s]", "");
		//System.out.println(arg1);
		String arg2 = args[1].toLowerCase().replaceAll("[^\\dA-Za-z\\s]", "");
		//System.out.println(arg2);
		
		Set<String> colors0 = new HashSet<String>();
		Set<String> colors1 = new HashSet<String>();
		
		for (String s: COLOR_DICTIONARY) {
			if (arg1.indexOf(s.toLowerCase()) != -1) {
				colors0.add(s);
				//System.out.println("Adding " + s + " to colors0");
			}
			if (arg2.indexOf(s.toLowerCase()) != -1) {
				colors1.add(s);
				//System.out.println("Adding " + s + " to colors1");
			}
		}
		
		if (colors0.isEmpty() || colors1.isEmpty()) {
			return -1.0f;
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
		String[] s1 = {"[\"Lexmark Yellow Toner Cartridge LEX10B042Y\"]", "[\"Lexmark International Hi-Yield Print Cartridge F/C75015000 Page YldMagenta Ink\"]"};
		String[] s2 = {"[\"TOSHIBA AMERICA CONSUMER Toner Cartridge, Black\"]", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s3 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", "[\"rooCASE Deluxe Carrying Bag for iPad 2 10\" and 11.6\" Netbook\"]"};
		String[] s4 = {"[\"Belkin AV20500-25 Blue Series Subwoofer Cable (25 ft; Retail Packaging)\"]", "[\"Blue Series Subwoofer Cable (15 Ft; Retail Packaging)\"]"};
		String[] s5 = {"[\"Morning Industry Inc RF-01AQ Remote Control Electronic Deadbolt, Antique Brass\"]", "[\"Remote Control Electronic Dead Bolt (Polished Brass)\"]"};
		String[] s6 = {null, "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s7 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s10 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s13 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("Extracted Colors Do Not Match: " + ecm.compute(s1));
		System.out.println("Extracted Colors Match: " + ecm.compute(s2));
		System.out.println("Extracted Color Not Found: " + ecm.compute(s3));
		System.out.println("Extracted Colors Match: " + ecm.compute(s4));
		System.out.println("Extracted Color Not Found: " + ecm.compute(s5));
		System.out.println("NULL A: " + ecm.compute(s6));
		System.out.println("NULL B: " + ecm.compute(s7));
		System.out.println("NULL both A and B: " + ecm.compute(s8));
		System.out.println("Empty A: " + ecm.compute(s9));
		System.out.println("Empty B: " + ecm.compute(s10));
		System.out.println("Empty both A and B: " + ecm.compute(s11));
		System.out.println("Null string A: " + ecm.compute(s12));
		System.out.println("Null string B: " + ecm.compute(s13));
		System.out.println("Null string both A and B: " + ecm.compute(s14));
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		return new HashSet<AllTypes>();
	}
}

package com.walmart.productgenome.matching.models.rules.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;


public class Size extends Function{

	public static final String NAME = "SIZE";
	public static final String DESCRIPTION = "Size Constraint";
	public static final int NUM_ARGS = 2;
	
	class ozcnt{
		public float oz = 0.0f;
		public int cnt = 0;
	};
	
	public Size() {
		super(NAME, DESCRIPTION);
	}

  public Size(String name, String description){
		super(name, description);
	}
	
	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
		if(args.length != NUM_ARGS){
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
    // TODO: Sanjib review.
		if (args[0] == null || args[1] == null) {
      return 0.0f;
    }
		if (args[0].toLowerCase().equals("null") || args[1].toLowerCase().equals("null")) {
		  return 0.0f;
		}
		if (args[0].isEmpty() || args[1].isEmpty()) {
		  return 0.0f;
		}

		String newArg0 = args[0].replace("Home Page Walmart.com Wal-Mart", " ").toLowerCase();
		String newArg1 = args[1].replace("Home Page Walmart.com Wal-Mart"," ").toLowerCase();
		
		newArg0 = newArg0.replaceAll("[^\\dA-Za-z \\.]", " ");
    newArg1 = newArg1.replaceAll("[^\\dA-Za-z \\.]", " ");
    
    newArg0 = newArg0.replace("red berry", "redberry");
    newArg0 = newArg0.replace("purple berry", "purpleberry");
    newArg1 = newArg1.replace("red berry", "redberry");
    newArg1 = newArg1.replace("purple berry", "purpleberry");

    HashMap<String, Integer> fruit2CountA = new HashMap<String, Integer>();
    HashMap<String, Integer> fruit2CountB = new HashMap<String, Integer>();
  
    String[] wordsFruit = newArg0.split(" ");
    HashSet<String> flavors = new HashSet<String>(Arrays.asList("blurberry", "apple", "strawberry", "blueberry", "blackberry", "original", "maple", "chocolate", "honey","cinnamon", "raisin", "mix", "millet", "buckwheat", "chocolatey", "berry", "berries", "natural", "naturals", "fruit", "nut", "nuts", "macaroon", "raspberry", "pretzel", "drizzle", "redberry", "purpleberry"));
    

    for(int i = 0; i < wordsFruit.length; ++i)
        if(flavors.contains(wordsFruit[i]))
            fruit2CountA.put(wordsFruit[i], 1);

    wordsFruit = newArg1.split(" ");
    for(int i = 0; i < wordsFruit.length; ++i)
        if( flavors.contains(wordsFruit[i]) )    
            fruit2CountB.put(wordsFruit[i], 1);

    int common = 0;
    for(String word : fruit2CountA.keySet())
        if( fruit2CountB.containsKey(word) )
            common += 1;

    if( (fruit2CountA.size()>0 && fruit2CountB.size()>0) && common==0 )
        return 0.0f;

		//AbstractStringMetric metric = new JaccardSimilarity();
		//return metric.getSimilarity(newArg0, newArg1);
        
        // Get oz, pack, count from a string.
        ozcnt oc1 = this.GetOzPackFromStr(newArg0);
        ozcnt oc2 = this.GetOzPackFromStr(newArg1);
        if (oc1.oz== 0.0f || oc2.oz==0.0f) {
          if (oc1.cnt == 0 || oc2.cnt == 0) return 1.0f;
          if (oc1.cnt == oc2.cnt) return 1.0f;
          return 0.0f;
        }
        if (oc1.oz != oc2.oz) return 0.0f;
        if (oc1.cnt == 0 || oc2.cnt == 0) return 1.0f;
        if (oc1.cnt != oc2.cnt) return 0.0f;
        return 1.0f;
        
	}

  private ozcnt GetOzPackFromStr(String str) {
	  ozcnt oc = new ozcnt();
	  Pattern ozP = Pattern.compile("[\\.\\d]+[ ]*((oz)|(ounce))");
	  Matcher matcher = ozP.matcher(str);
	  if (matcher.find()) {
		  String ozStr = (matcher.group());
		  float oz = Float.parseFloat(ozStr.split("(oz)|(ounce)")[0].trim());
		  oc.oz = oz;
	  }
	  Pattern cntP = Pattern.compile("pack of \\d+");
	  matcher = cntP.matcher(str);
	  if (matcher.find()) {
		  String cntStr = matcher.group();
		  int cnt = Integer.parseInt(cntStr.split("pack of")[1].trim());
		  oc.cnt = cnt;
		  return oc;
	  } else {
		  cntP = Pattern.compile("\\d+[ ]*((cnt)|(ct)|(count))");
		  matcher = cntP.matcher(str);
		  if (matcher.find()) {
			  String cntStr = matcher.group();
			  int cnt = Integer.parseInt(cntStr.split("(cnt)|(ct)|(count)")[0].trim());
			  oc.cnt = cnt;
			  return oc;
		  }
	  }
	  return oc;
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
		Function jac = new Size();
		System.out.println(jac.getSignature());
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		return new HashSet<AllTypes>();
	}

}

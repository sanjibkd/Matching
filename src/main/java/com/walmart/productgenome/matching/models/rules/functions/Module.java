package com.walmart.productgenome.matching.models.rules.functions;

import com.walmart.productgenome.matching.models.data.Tuple;


public interface Module {

	public float compute(Tuple tuple1, Tuple tuple2);
	
}

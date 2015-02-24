package org.unipd.nbeghin.climbtheworld.util;

public class IntPair {

	int x;
	int y;
	
	public IntPair(int x, int y) {
		this.x=x;
		this.y=y;
	}	
	
	public int getFirstInt(){
		return x;
	}
	
	public int getSecondInt(){
		return y;
	}
	
	public void setFirstInt(int x){
		this.x=x;
	}
	
	public void setSecondInt(int y){
		this.y=y;
	}
			
}
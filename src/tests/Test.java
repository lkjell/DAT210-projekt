package tests;

import java.util.ArrayList;

public class Test {
	
	static ArrayList<Test> tests;
	
	public Test() { tests.add( this ); }
	
	public boolean run() { return true; }
	
	public static void main( String[] args ) {
		for ( Test t: tests ) {
			t.run();
		}
	}
}

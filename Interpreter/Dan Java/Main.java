package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	
	public int CORE_SIZE = 512;
    
    public static void main(String[] args) {
        String input = "MOV    0,1\n ADD 0,2";

        Parser parseCode = new Parser(input);
        
        for (int i = 0; i < parseCode.output.size(); i++) {
        	System.out.println(parseCode.output.get(i).toString());
        }
    
    }
    
}
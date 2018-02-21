package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	
	public int CORE_SIZE = 512;
	
	static List<Character> skippableChars = Arrays.asList(' ', '\t', ',', '\n');
	static List<String> opcodes = Arrays.asList("MOV", "ADD", "JMP");
	static ArrayList<Token> output = new ArrayList<Token>();
    
    public static void main(String[] args) {
    	
        Parser parser = new Parser("MOV    0,1 ");
        ArrayList<Token> list = parser.getTokenArray();
        for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i).toString());
		}
    }
}
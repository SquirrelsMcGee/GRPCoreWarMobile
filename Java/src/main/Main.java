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
    	
        Parser parser = new Parser("MOV    0,1");
        ArrayList<Token> list = parser.getTokenArray();
        for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i).toString());
		}
        
    }
    /*
    public static void readLine(String input) {
        int j = 0, i = 0;
        for (i = 0; i < input.length(); i++) {
        	System.out.print("(" + i+ "," +input.charAt(i) + ") ");
        }
        System.out.println("");

        for (i = 0; i < input.length();) {
        	
            while (skippableChars.contains(input.charAt(i))){            	
        		j = i;
        		i++;
        		System.out.println("Skipping: " + i);
            }
            if (i >= input.length()) return;
            while (!(skippableChars.contains(input.charAt(i))) && input.charAt(i) != '\n' && i < input.length()){
                
            	System.out.println("Reading: " + i+ " " + input.charAt(i));
                System.out.println(i);
                i++;
            }
            System.out.println(i);
            System.out.println("Adding substring (j,i); "+ j + " " + i + " = " + input.substring(j,i));
            output.add(new Token(input.substring(j,i)));
        }
        return;
    }
    */
    
}
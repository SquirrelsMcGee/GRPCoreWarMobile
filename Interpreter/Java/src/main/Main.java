package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	
	static List<Character> skippableChars = Arrays.asList(' ', '\t', ',');
	static List<String> opcodes = Arrays.asList("MOV", "ADD", "JMP");
    
    public static void main(String[] args) {
        ArrayList<Token> output = new ArrayList<Token>();
        
        String input = "MOV    0,  1  \n";
        
        int j = 0, i = 0;
        
        while (input.charAt(i) != '\n'){
        	while (skippableChars.contains(input.charAt(i))){
                j = i + 1;
                i++;
            }
        	
            while (!(skippableChars.contains(input.charAt(i))) && input.charAt(i) != '\n'){
                i++;
            }

            output.add(new Token(input.substring(j,i)));
        }
        
        for (i = 0; i < output.size(); i++) {
        	System.out.println(output.get(i).getType() + ", value= " + output.get(i).getValue());
        }
        
    }
}
package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	
	private ArrayList<Token> tokenArray;
	
	static List<Character> skippableChars = Arrays.asList(' ', '\t', ',');
	
	public Parser(String input) {
		createTokenArray(input);
	}
	
	private void createTokenArray(String input) {
		
		char[] characterArray;
		
		characterArray = input.toCharArray();
		
		readLine(characterArray);
		return;
	}
	
	public ArrayList<Token> getTokenArray() {
		return tokenArray;
	}
	
	private void readLine(char[] characterArray) {
		char character;
		String input = "";
		Token token;
		int i = 0, j = 0;
		
		for (i = 0; i < characterArray.length; i++) {
			character = characterArray[i];
			
			while (skippableChar(character)) {
				i++;
				j = i;
				character = characterArray[i];
			}
			
			// Skipped all whitespace
			
			while (!skippableChar(character)) {
				character = characterArray[i];
				input += character;
			}

			token = new Token(input);
			tokenArray.add(token);
		}
		return;
	}
	
	private Boolean skippableChar(char character) {
		if (skippableChars.contains(character)) return true;
		
		return false;
	}
}

package main;

import java.util.Arrays;
import java.util.List;

public class Token {
	static List<String> opcodes = Arrays.asList("MOV", "ADD", "JMP");
	
	private String type;
	private String value;

	public Token(String input) {
		this.value = input;
		this.type = (generateType(input));
	}
	
	private String generateType(String input) {
		if (opcodes.contains(input)) {
			return "OPCODE";
		} else {
			return "DATA";
		}
	}
	
	public String getType() {		
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return "Type: " + this.type + ", Value: " + this.value;
	}
	
}
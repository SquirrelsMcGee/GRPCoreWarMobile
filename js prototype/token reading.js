var OPCODES = ["DAT", "MOV", "ADD", "SUB", "MUL", "DIV", "MOD", "JMP", "JMZ", "JMN", "DJN", "CMP", "SPL", "SEQ", "SNE", "SLT", "XCH", "PCT", "NOP", "STP", "LDP"];

var inputString = "MOV 0,1";
var outputArray = [];

function read_line() {
    var currentValue = "";
    skip_whitespace();

    while (inputString.length > 1) {
        currentValue = read_until_whitespace();
        outputArray.push(currentValue);
    }
    console.log(outputArray);
    
}

function skip_whitespace() {
    while (inputString[0] == " " || inputString[0] == "\t") {
        inputString = inputString.substring(1);
    }
}

function read_until_whitespace() {
    var charBuffer = "";

    //for (var i = 0, len = str.length; i < len; i++) {
    while (inputString[0] != " " || inputString[0] != "\t") {
        console.log(inputString[0]);

        charBuffer += inputString[0];
        inputString = inputString.substring(1);
        if (inputString[0] == " " || inputString[0] == "\t") {
            return charBuffer;
        }
        if (inputString[0] == undefined) {
            return charBuffer;
        }
        
    }

    return charBuffer;
}
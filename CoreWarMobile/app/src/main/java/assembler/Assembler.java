package assembler;


import java.io.*;

import marsVM.*;

public class Assembler {
    protected BufferedReader in;
    protected StreamTokenizer tok;
    protected int IP;
    protected int maxLength;
    protected marsVM.Memory war[];
    protected int start;

    // meta values
    protected String name;
    protected String author;


    public Assembler(Reader reader, int maxl) {
        in = new BufferedReader(reader);
        tok = new StreamTokenizer(in);
        tok.lowerCaseMode(true);
        tok.ordinaryChar('/');
        tok.eolIsSignificant(true);
        tok.parseNumbers();
        tok.ordinaryChar('.');
        tok.ordinaryChar(',');

        IP = 0;
        start = 0;
        maxLength = maxl;

        war = new marsVM.Memory[maxl];

        for (int i = 0; i < maxl; i++)
            war[i] = new marsVM.Memory();
    }

    public marsVM.Memory[] getWarrior() {
        marsVM.Memory wMem[] = new marsVM.Memory[IP];

        for (int i = 0; i < IP; i++) {
            wMem[i] = war[i];
        }

        return wMem;
    }

    public int getOffset() {
        return start;
    }

    public String getName() {
        if (name != null)
            return new String(name);

        return "";
    }

    public String getAuthor() {
        if (author != null)
            return new String(author);

        return "";
    }

    public int length() {
        return IP;
    }

    public boolean assemble() {
        try {
            begin:
            while (tok.nextToken() != StreamTokenizer.TT_EOF) {
                System.out.println("Current Token = " + tok.toString() + " == mov :" + tok.sval.equals("mov"));
                if (tok.ttype == ';')
                    pComment();
                else if (tok.ttype == StreamTokenizer.TT_WORD && tok.sval.equals("org")) {
                    if (tok.nextToken() != tok.TT_NUMBER) {
                        System.out.println("tok.nextToken() != tok.TT_NUMBER");
                        return false;
                    }

                    start = (int) tok.nval;

                    tok.nextToken();

                    if (tok.ttype == ';')
                        pComment();

                } else if (tok.ttype == StreamTokenizer.TT_WORD) {
                    System.out.println("ttype == TT_WORD");
                    if (tok.sval.equals("mov"))
                        war[IP].opcode = marsVM.Memory.MOV;
                    else if (tok.sval.equals("add"))
                        war[IP].opcode = marsVM.Memory.ADD;
                    else if (tok.sval.equals("sub"))
                        war[IP].opcode = marsVM.Memory.SUB;
                    else if (tok.sval.equals("mul"))
                        war[IP].opcode = marsVM.Memory.MUL;
                    else if (tok.sval.equals("div"))
                        war[IP].opcode = marsVM.Memory.DIV;
                    else if (tok.sval.equals("mod"))
                        war[IP].opcode = marsVM.Memory.MOD;
                    else if (tok.sval.equals("jmz"))
                        war[IP].opcode = marsVM.Memory.JMZ;
                    else if (tok.sval.equals("jmn"))
                        war[IP].opcode = marsVM.Memory.JMN;
                    else if (tok.sval.equals("djn"))
                        war[IP].opcode = marsVM.Memory.DJN;
                    else if (tok.sval.equals("cmp"))
                        war[IP].opcode = marsVM.Memory.CMP;
                    else if (tok.sval.equals("seq"))
                        war[IP].opcode = marsVM.Memory.SEQ;
                    else if (tok.sval.equals("slt"))
                        war[IP].opcode = marsVM.Memory.SLT;
                    else if (tok.sval.equals("spl"))
                        war[IP].opcode = marsVM.Memory.SPL;
                    else if (tok.sval.equals("dat"))
                        war[IP].opcode = marsVM.Memory.DAT;
                    else if (tok.sval.equals("jmp"))
                        war[IP].opcode = marsVM.Memory.JMP;
                    else if (tok.sval.equals("sne"))
                        war[IP].opcode = marsVM.Memory.SNE;
                    else if (tok.sval.equals("nop"))
                        war[IP].opcode = marsVM.Memory.NOP;
                    else if (tok.sval.equals("ldp"))
                        war[IP].opcode = marsVM.Memory.LDP;
                    else if (tok.sval.equals("stp"))
                        war[IP].opcode = marsVM.Memory.STP;
                    else if (tok.sval.equals("end")) {
                        if (tok.nextToken() == tok.TT_NUMBER)
                            start = (int) tok.nval;
                        System.out.println("tok.sval.equals('end')");
                        return true;
                    } else {
                        System.out.println("!tok.sval.equals('end')");
                        return false;
                    }
                    if (!pModifier()) {
                        System.out.println("No modifier");
                        return false;
                    }

                    if (++IP > maxLength) {
                        System.out.println("++IP > maxLength");
                        return false;
                    }

                    if (tok.ttype == ';')
                        pComment();
                }

                if (tok.ttype != tok.TT_EOL) {
                    System.out.println("tok.ttype != tok.TT_EOL");
                    return false;
                }
            }

        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        return true;
    }

    void pComment() {
        // this function is in place to get meta data
        try {
            if (tok.nextToken() == tok.TT_WORD) {
                if (tok.sval.equals("name")) {
                    name = in.readLine();
                } else if (tok.sval.equals("author")) {
                    author = in.readLine();
                } else {
                    in.readLine();
                }
            } else {
                in.readLine();
            }

            tok.ttype = tok.TT_EOL;

        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        }

        return;
    }

    boolean pModifier() {
        try {
            if (tok.nextToken() != '.') {
                System.out.println("tok.nextToken() != '.'");
                return pAOperand();
            }else if (tok.nextToken() == tok.TT_WORD) {
                if (tok.sval.equals("a"))
                    war[IP].modifier = marsVM.Memory.mA;
                else if (tok.sval.equals("b"))
                    war[IP].modifier = marsVM.Memory.mB;
                else if (tok.sval.equals("ab"))
                    war[IP].modifier = marsVM.Memory.mAB;
                else if (tok.sval.equals("ba"))
                    war[IP].modifier = marsVM.Memory.mBA;
                else if (tok.sval.equals("f"))
                    war[IP].modifier = marsVM.Memory.mF;
                else if (tok.sval.equals("x"))
                    war[IP].modifier = marsVM.Memory.mX;
                else if (tok.sval.equals("i")) {
                    System.out.println("sval == i");
                    war[IP].modifier = marsVM.Memory.mI;
                }
                else
                    return false;

                tok.nextToken();

                return pAOperand();
            } else
                return false;

        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

    }

    boolean pAOperand() {
        switch (tok.ttype) {
            case StreamTokenizer.TT_NUMBER:
                return pAValue();

            case '#':
                war[IP].aIndir = marsVM.Memory.IMMEDIATE;
                war[IP].aTiming = marsVM.Memory.PRE;
                war[IP].aAction = marsVM.Memory.NONE;
                war[IP].aTarget = marsVM.Memory.B;
                break;

            case '$':
                war[IP].aIndir = marsVM.Memory.DIRECT;
                war[IP].aTiming = marsVM.Memory.POST;
                war[IP].aAction = marsVM.Memory.NONE;
                war[IP].aTarget = marsVM.Memory.B;
                break;

            case '@':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.POST;
                war[IP].aAction = marsVM.Memory.NONE;
                war[IP].aTarget = marsVM.Memory.B;
                break;

            case '<':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.PRE;
                war[IP].aAction = marsVM.Memory.DECREMENT;
                war[IP].aTarget = marsVM.Memory.B;
                break;

            case '>':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.POST;
                war[IP].aAction = marsVM.Memory.INCREMENT;
                war[IP].aTarget = marsVM.Memory.B;
                break;

            case '*':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.POST;
                war[IP].aAction = marsVM.Memory.NONE;
                war[IP].aTarget = marsVM.Memory.A;
                break;

            case '{':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.PRE;
                war[IP].aAction = marsVM.Memory.DECREMENT;
                war[IP].aTarget = marsVM.Memory.A;
                break;

            case '}':
                war[IP].aIndir = marsVM.Memory.INDIRECT;
                war[IP].aTiming = marsVM.Memory.PRE;
                war[IP].aAction = marsVM.Memory.INCREMENT;
                war[IP].aTarget = marsVM.Memory.A;
                break;

            default:
                return false;

        }

        try {
            tok.nextToken();
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        if (!pAValue())
            return false;

        return true;

    }

    boolean pAValue() {
        if (tok.ttype != tok.TT_NUMBER)
            return false;

        war[IP].aValue = (int) tok.nval;

        try {
            if (tok.nextToken() != ',') {
                System.out.println("no comma after aValue");
                return false;
            }

            tok.nextToken();
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        return pBOperand();
    }


    boolean pBOperand() {
        System.out.println("pBOperand() token="+tok.toString());
        if (tok.ttype != tok.TT_NUMBER)
            return pBValue();
        switch (tok.ttype) {


            case '#':
                war[IP].bIndir = marsVM.Memory.IMMEDIATE;
                war[IP].bTiming = marsVM.Memory.PRE;
                war[IP].bAction = marsVM.Memory.NONE;
                war[IP].bTarget = marsVM.Memory.B;
                break;

            case '$':
                war[IP].bIndir = marsVM.Memory.DIRECT;
                war[IP].bTiming = marsVM.Memory.POST;
                war[IP].bAction = marsVM.Memory.NONE;
                war[IP].bTarget = marsVM.Memory.B;
                break;

            case '@':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.POST;
                war[IP].bAction = marsVM.Memory.NONE;
                war[IP].bTarget = marsVM.Memory.B;
                break;

            case '<':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.PRE;
                war[IP].bAction = marsVM.Memory.DECREMENT;
                war[IP].bTarget = marsVM.Memory.B;
                break;

            case '>':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.POST;
                war[IP].bAction = marsVM.Memory.INCREMENT;
                war[IP].bTarget = marsVM.Memory.B;
                break;

            case '*':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.POST;
                war[IP].bAction = marsVM.Memory.NONE;
                war[IP].bTarget = marsVM.Memory.A;
                break;

            case '{':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.PRE;
                war[IP].bAction = marsVM.Memory.DECREMENT;
                war[IP].bTarget = marsVM.Memory.A;
                break;

            case '}':
                war[IP].bIndir = marsVM.Memory.INDIRECT;
                war[IP].bTiming = marsVM.Memory.POST;
                war[IP].bAction = marsVM.Memory.INCREMENT;
                war[IP].bTarget = marsVM.Memory.A;
                break;

            default:
                return false;

        }

        try {
            tok.nextToken();
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        return pBValue();
    }

    boolean pBValue() {
        System.out.println("pBValue() token="+tok.toString());
        if (tok.ttype != tok.TT_NUMBER)
            return false;

        war[IP].bValue = (int) tok.nval;

        try {
            tok.nextToken();
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        return true;
    }

}

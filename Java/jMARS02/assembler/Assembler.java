/*-
 * Copyright (c) 1998 Brian Haskin jr.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
 
package assembler;

import java.io.*;

import marsVM.Memory;

public class Assembler
{
	protected BufferedReader in;
	protected StreamTokenizer tok;
	protected int IP;
	protected int maxLength;
	protected Memory war[];
	protected int start;
	
	// meta values
	protected String name;
	protected String author;
	
	
	public Assembler(Reader reader, int maxl)
	{
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
		
		war = new Memory[maxl];
		
		for (int i=0; i<maxl; i++)
			war[i] = new Memory();
	}
	
	public Memory[] getWarrior()
	{
		Memory wMem[] = new Memory[IP];
		
		for (int i=0; i<IP; i++)
		{
			wMem[i] = war[i];
		}
		
		return wMem;
	}
	
	public int getOffset()
	{
		return start;
	}
	
	public String getName()
	{
		if (name != null)
			return new String(name);
			
		return "";
	}
	
	public String getAuthor()
	{
		if (author != null)
			return new String(author);
			
		return "";
	}
	
	public int length()
	{
		return IP;
	}
	
	public boolean assemble()
	{
		try
		{
			begin:
			while(tok.nextToken() != StreamTokenizer.TT_EOF)
			{
				if(tok.ttype == ';')
					pComment();
				else if (tok.ttype == StreamTokenizer.TT_WORD && tok.sval.equals("org"))
				{
						if(tok.nextToken() != tok.TT_NUMBER)
							return false;

						start = (int) tok.nval;
						
						tok.nextToken();
						
						if (tok.ttype == ';')
							pComment();
						
				} else if (tok.ttype == StreamTokenizer.TT_WORD)
				{
					if (tok.sval.equals("mov"))
						war[IP].opcode = Memory.MOV;
					else if (tok.sval.equals("add"))
						war[IP].opcode = Memory.ADD;
					else if (tok.sval.equals("sub"))
						war[IP].opcode = Memory.SUB;
					else if (tok.sval.equals("mul"))
						war[IP].opcode = Memory.MUL;
					else if (tok.sval.equals("div"))
						war[IP].opcode = Memory.DIV;
					else if (tok.sval.equals("mod"))
						war[IP].opcode = Memory.MOD;
					else if (tok.sval.equals("jmz"))
						war[IP].opcode = Memory.JMZ;
					else if (tok.sval.equals("jmn"))
						war[IP].opcode = Memory.JMN;
					else if (tok.sval.equals("djn"))
						war[IP].opcode = Memory.DJN;
					else if (tok.sval.equals("cmp"))
						war[IP].opcode = Memory.CMP;
					else if (tok.sval.equals("seq"))
						war[IP].opcode = Memory.SEQ;
					else if (tok.sval.equals("slt"))
						war[IP].opcode = Memory.SLT;
					else if (tok.sval.equals("spl"))
						war[IP].opcode = Memory.SPL;
					else if (tok.sval.equals("dat"))
						war[IP].opcode = Memory.DAT;
					else if (tok.sval.equals("jmp"))
						war[IP].opcode = Memory.JMP;
					else if (tok.sval.equals("sne"))
						war[IP].opcode = Memory.SNE;
					else if (tok.sval.equals("nop"))
						war[IP].opcode = Memory.NOP;
					else if (tok.sval.equals("ldp"))
						war[IP].opcode = Memory.LDP;
					else if (tok.sval.equals("stp"))
						war[IP].opcode = Memory.STP;
					else if (tok.sval.equals("end"))
					{
						if (tok.nextToken() == tok.TT_NUMBER)
							start = (int) tok.nval;
							
						return true;
					}
					else 
						return false;

					if (!pModifier())
						return false;
						
					if (++IP > maxLength) return false;

					if (tok.ttype == ';')
						pComment();
				} 
				
				if (tok.ttype != tok.TT_EOL)
					return false;
			}

		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}
		
		return true;
	}
	
	void pComment()
	{
		// this function is in place to get meta data
		try
		{
			if (tok.nextToken() == tok.TT_WORD)
			{ 
				if (tok.sval.equals("name"))
				{
					name = in.readLine();
				} else if (tok.sval.equals("author"))
				{
					author = in.readLine();
				} else
				{
					in.readLine();
				}
			} else
			{
				in.readLine();
			}
			
			tok.ttype = tok.TT_EOL;
			
		} catch (IOException e)
		{
			System.out.println(e.toString());
			return;
		}

		return;
	}
	
	boolean pModifier()
	{
		try
		{
			if (tok.nextToken() != '.')
				return pAOperand();
			else if (tok.nextToken() == tok.TT_WORD)
			{
				if (tok.sval.equals("a"))
					war[IP].modifier = Memory.mA;
				else if (tok.sval.equals("b"))
					war[IP].modifier = Memory.mB;
				else if (tok.sval.equals("ab"))
					war[IP].modifier = Memory.mAB;
				else if (tok.sval.equals("ba"))
					war[IP].modifier = Memory.mBA;
				else if (tok.sval.equals("f"))
					war[IP].modifier = Memory.mF;
				else if (tok.sval.equals("x"))
					war[IP].modifier = Memory.mX;
				else if (tok.sval.equals("i"))
					war[IP].modifier = Memory.mI;
				else 
					return false;

				tok.nextToken();

				return pAOperand();
			} else 
				return false;

		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}

	}
	
	boolean pAOperand()
	{
		switch (tok.ttype)
		{
			case tok.TT_NUMBER:
				return pAValue();
				
			case '#':
				war[IP].aIndir = Memory.IMMEDIATE;
				war[IP].aTiming = Memory.PRE;
				war[IP].aAction = Memory.NONE;
				war[IP].aTarget = Memory.B;
				break;
				
			case '$':
				war[IP].aIndir = Memory.DIRECT;
				war[IP].aTiming = Memory.POST;
				war[IP].aAction = Memory.NONE;
				war[IP].aTarget = Memory.B;
				break;
				
			case '@':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.POST;
				war[IP].aAction = Memory.NONE;
				war[IP].aTarget = Memory.B;
				break;
				
			case '<':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.PRE;
				war[IP].aAction = Memory.DECREMENT;
				war[IP].aTarget = Memory.B;
				break;
				
			case '>':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.POST;
				war[IP].aAction = Memory.INCREMENT;
				war[IP].aTarget = Memory.B;
				break;
				
			case '*':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.POST;
				war[IP].aAction = Memory.NONE;
				war[IP].aTarget = Memory.A;
				break;
				
			case '{':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.PRE;
				war[IP].aAction = Memory.DECREMENT;
				war[IP].aTarget = Memory.A;
				break;
				
			case '}':
				war[IP].aIndir = Memory.INDIRECT;
				war[IP].aTiming = Memory.PRE;
				war[IP].aAction = Memory.INCREMENT;
				war[IP].aTarget = Memory.A;
				break;
				
			default:
				return false;
				
		}
		
		try
		{
			tok.nextToken();
		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}

		if (!pAValue())
			return false;
			
		return true;
		
	}

	boolean pAValue()
	{
		if (tok.ttype != tok.TT_NUMBER)
			return false;
		
		war[IP].aValue = (int) tok.nval;
		
		try
		{
			if (tok.nextToken() != ',')
			{
				System.out.println("no comma after aValue");
				return false;
			}
		
			tok.nextToken();
		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}
		
		return pBOperand();
	}
	
	
	boolean pBOperand()
	{
		switch (tok.ttype)
		{
			case tok.TT_NUMBER:
				return pBValue();
				
			case '#':
				war[IP].bIndir = Memory.IMMEDIATE;
				war[IP].bTiming = Memory.PRE;
				war[IP].bAction = Memory.NONE;
				war[IP].bTarget = Memory.B;
				break;
				
			case '$':
				war[IP].bIndir = Memory.DIRECT;
				war[IP].bTiming = Memory.POST;
				war[IP].bAction = Memory.NONE;
				war[IP].bTarget = Memory.B;
				break;
				
			case '@':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.POST;
				war[IP].bAction = Memory.NONE;
				war[IP].bTarget = Memory.B;
				break;
				
			case '<':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.PRE;
				war[IP].bAction = Memory.DECREMENT;
				war[IP].bTarget = Memory.B;
				break;
				
			case '>':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.POST;
				war[IP].bAction = Memory.INCREMENT;
				war[IP].bTarget = Memory.B;
				break;
				
			case '*':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.POST;
				war[IP].bAction = Memory.NONE;
				war[IP].bTarget = Memory.A;
				break;

			case '{':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.PRE;
				war[IP].bAction = Memory.DECREMENT;
				war[IP].bTarget = Memory.A;
				break;
				
			case '}':
				war[IP].bIndir = Memory.INDIRECT;
				war[IP].bTiming = Memory.POST;
				war[IP].bAction = Memory.INCREMENT;
				war[IP].bTarget = Memory.A;
				break;
				
			default:
				return false;
				
		}
		
		try
		{
			tok.nextToken();
		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}

		return pBValue();
	}

	boolean pBValue()
	{
		if (tok.ttype != tok.TT_NUMBER)
			return false;
		
		war[IP].bValue = (int) tok.nval;
		
		try 
		{
			tok.nextToken();
		} catch (IOException e)
		{
			System.out.println(e.toString());
			return false;
		}
		
		return true;
	}
	
}

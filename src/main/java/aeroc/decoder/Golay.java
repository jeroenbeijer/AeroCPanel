/*
 * Copyright (C) 2010 mbelib Author
 * GPG Key ID: 0xEA5EFE2C (9E7A 5527 9CDC EBF7 BF1B  D772 4F98 E863 EA5E FE2C)
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND ISC DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS.  IN NO EVENT SHALL ISC BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
 * OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */
package decoder;

import java.math.BigInteger;

public class Golay {

	long encoding_table[] = new long[4096];
	long decoding_table[] = new long[2048];

	long position[] = { 0x00000001, 0x00000002, 0x00000004, 0x00000008,
			0x00000010, 0x00000020, 0x00000040, 0x00000080, 0x00000100,
			0x00000200, 0x00000400, 0x00000800, 0x00001000, 0x00002000,
			0x00004000, 0x00008000, 0x00010000, 0x00020000, 0x00040000,
			0x00080000, 0x00100000, 0x00200000, 0x00400000 };
	
	long pattern = 0;
	long temp = 0;
	int[] a = {0,0,0,0};

	long X22 = 0x00400000; /* vector representation of X^{22} */
	long X11 = 0x00000800; /* vector representation of X^{11} */
	long MASK12 = 0xfffff800; /* auxiliary vector for testing */
	long GENPOL = 0x00000ae3; /* generator polinomial, g(x) */

	public static int fromBinary(final String str) {
		return Integer.parseInt(str, 2);
	}

	public Golay() {

		for (pattern = 0; pattern < 4096; pattern++) {
			temp = pattern << 11; /* multiply information by X^{11} */
			encoding_table[(int) pattern] = get_syndrome(temp)+temp;
		}
		
		
		// generate decoding tables
		int i = 0;
		decoding_table[0] = 0;
	    decoding_table[1] = 1;
	    temp = 1; 
	    for (i=2; i<= 23; i++) {
	        temp *= 2;
	        decoding_table[(int) get_syndrome(temp)] = temp;
	        }
	 		
	}


	static String hexToBin(String s) {
		return new BigInteger(s, 8).toString(2);
	}

	public static String hexToBinary(String hex) {
		return new BigInteger(hex, 16).toString(2);
	}

	public String enc(String data) {

		int idata = fromBinary(data);

		int result = (int) encoding_table[idata];

	    return Integer.toBinaryString(result);

	}
	
	
	
	public long get_syndrome(long pattern)

	{
		long aux = X22;

		if (pattern >= X11) {

			long diff = (pattern & MASK12);

			while (diff > 0) {

				long diff2 = aux & pattern;

				while (diff2 <= 0) {
					aux = aux >> 1;
					diff2 = aux & pattern;

				}
				pattern ^= (aux / X11) * GENPOL;

				diff = (pattern & MASK12);

			}
		}
		return (pattern);
	}

	
	public static int parity(int cw){
		
		
		// This function checks the overall parity of codeword cw.
		// If parity is even, 0 is returned, else 1.
		//def parity(cw):
		 //   #/* XOR the bytes of the codeword */
		   
		
		    int p = cw & 0xff;
		    p = p ^ ((cw >> 8) & 0xff);
		    p = p ^ ((cw >> 16) & 0xff);
		    
		    p = p ^ (p >> 4);
		    p = p ^ (p >> 2);
		    p = p ^ (p >> 1);
		    
		    
		    int result1 = (p & 1);
		    		
		    return result1;
		
	}
	
	

	
	public int
	mbe_golay2312 (char[] in, char[] out)
	{

	  int i, errs;
	  int block;

	  block = 0;
	  for (i = 22; i >= 0; i--)
	    {
	      block = block << 1;
	      block = block + Integer.parseInt(String.valueOf( in[i]));
	    }
	  
	  block = mbe_checkGolayBlock (block);
	  
	  String cblock = leadingZeros(Integer.toBinaryString(block), 12);

	  for (i = 22; i >= 11; i--)
	    {
	      out[i] = cblock.charAt(22-i);
		  
		  block = block << 1;
	    }
	  for (i = 10; i >= 0; i--)
	    {
	      out[i] = in[i];
	    }

	  errs = 0;
	  for (i = 22; i >= 11; i--)
	    {
	      if (out[i] != in[i])
	        {
	          errs++;
	        }
	    }
	  return (errs);
	}
	
	public int
	mbe_checkGolayBlock (int block)
	{

	  int i, syndrome, eccexpected, eccbits, databits;
	  long mask, block_l;

	  block_l = block;

	  mask = 0x400000l;
	  eccexpected = 0;
	  for (i = 0; i < 12; i++)
	    {
	      if ((block_l & mask) != 0l)
	        {
	          eccexpected ^= EccConstants.golayGenerator[i];
	        }
	      mask = mask >> 1;
	    }
	  eccbits = (int) (block_l & 0x7ffl);
	  syndrome = eccexpected ^ eccbits;

	  databits = (int) (block_l >> 11);
	  databits = databits ^ EccConstants.golayMatrix[syndrome];

	  return databits;
	
	}

	public static String leadingZeros(String s, int length) {
		if (s.length() >= length)
			return s;
		else
			return String.format("%0" + (length - s.length()) + "d%s", 0, s);
	}
	
	
	public int mbe_hamming1511 (char in[], char out[])
	{
	  int i, j, errs, block, syndrome, stmp, stmp2;

	  errs = 0;

	  block = Integer.parseInt(new String(in), 2);
	 
	  syndrome = 0;
	  
	  for (i = 0; i < 4; i++)
	    {
	      syndrome <<= 1;
	      stmp = block;
	      stmp &= EccConstants.hammingGenerator[i];

	      stmp2 = (stmp % 2);
	      for (j = 0; j < 14; j++)
	        {
	          stmp >>= 1;
	          stmp2 ^= (stmp % 2);
	        }

	      syndrome |= stmp2;
	    }
	  if (syndrome > 0)
	    {
	      errs++;
	      block ^= EccConstants.hammingMatrix[syndrome];
	    }

	  
	  String cblock = leadingZeros(Integer.toBinaryString(block), 15);
	  
	 
	    for (i = 0; i < 15; i++)
	    {
	       
	     	 out[i] = cblock.charAt(i);
		      
	    }
	  return (errs);
	}

}
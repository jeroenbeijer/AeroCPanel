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

public class HammingEncoder {

	static int Hamming15113Gen[] = { 0x4009, 0x200d, 0x100f, 0x080e, 0x0407,
			0x020a, 0x0105, 0x008b, 0x004c, 0x0026, 0x0013 };
	
	
	static  int p25_Hamming15113Gen[] = {
	    0x400f, 0x200e, 0x100d, 0x080c, 0x040b, 0x020a, 0x0109, 0x0087, 0x0046, 0x0025, 0x0013
	};

	
	public int Hamming15_11_3_Encode(int input) {
		int i, codeword_out = 0;
		for (i = 0; i < 11; ++i) {

			
			int bit = input & (1 << (10 - i));
			if (bit > 0) {

				codeword_out ^= Hamming15113Gen[i];
			}
		}
		return codeword_out;
	}
	public int Hamming15_11_3_EncodeP25(int input) {
		int i, codeword_out = 0;
		for (i = 0; i < 11; ++i) {

			
			int bit = input & (1 << (10 - i));
			if (bit > 0) {

				codeword_out ^= p25_Hamming15113Gen[i];
			}
		}
		return codeword_out;
	}
}



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
import java.util.Arrays;

public class AudioDecoder {

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	char[] new97leavertable = { 89, 81, 73, 65, 57, 49, 41, 33, 25, 17, 9, 1, 88, 80, 72, 64, 56, 48, 40, 32, 24, 16, 8,
			0, 2, 10, 18, 26, 34, 42, 50, 58, 66, 74, 82, 90, 3, 11, 19, 27, 35, 43, 51, 59, 67, 75, 83, 91, 4, 12, 20,
			28, 36, 44, 52, 60, 68, 76, 84, 92, 5, 13, 21, 29, 37, 45, 53, 61, 69, 77, 85, 93, 6, 14, 22, 30, 38, 46,
			54, 62, 70, 78, 86, 94, 7, 15, 23, 31, 39, 47, 55, 63, 71, 79, 87, 95

	};
	
	private Parameters currentp = new Parameters();
	private Parameters prevp = new Parameters();
	private Parameters prevpenh = new Parameters();
	
	public static int audioRate = 8000/50;

	double gain = 0.42;
	
	Golay golay = new Golay();
	HammingEncoder coder = new HammingEncoder();

	BiquadFilter highpassFilter = new BiquadFilter();
	BiquadFilter lowpassFilter = new BiquadFilter();
	
	
	double quantstep[] = { 1.2, 0.85, 0.65, 0.42, 0.28, 0.14, 0.07, 0.035, 0.0175, 0.00875, 0.005, 0.0025 };
	
	double dq_prba_mul[] = { 0.383508, 0.241319, 0.192224, 0.142873, 0.120722, 0.110561, 0.107348 };
	double dq_prba_add[] = { 0.59876198, -0.212202, -0.020639, -0.167114, -0.107666, -0.082211003, -0.058773 };

	double dq_hoc_mul[][] = { { 0.251012, 0.134634, 0.108924, 0.090682, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.28937301, 0.204162, 0.18148801, 0.141091, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.265773, 0.190194, 0.138142, 0.107815, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.27769801, 0.186648, 0.1427, 0.133697, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.26810801, 0.179805, 0.131339, 0.112442, 0.083299004, 0.080834001, 0.084485002, 0.0, 0.0 },
			{ 0.281831, 0.199366, 0.147891, 0.113754, 0.082061999, 0.109516, 0.096391998, 0.069571003, 0.0 },
			{ 0.26216799, 0.169484, 0.12996501, 0.117727, 0.083796002, 0.10129, 0.073732004, 0.086384997, 0.0 },
			{ 0.276337, 0.196631, 0.14203, 0.108686, 0.079484001, 0.09183, 0.10001, 0.089230999, 0.0 }, };

	double dq_hoc_add[][] = { { -0.58701801, -0.044727001, -0.061985001, 0.052513, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.073668003, -0.057243999, -0.012928, 0.0078099999, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.113336, 0.065157004, 0.030675, -0.035709001, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.033447001, -0.010879, 0.040254999, 0.00087500003, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.051784001, -0.0052700001, 0.011678, -0.0027389999, 0.007216, -0.0092000002, 0.01352, 0.0, 0.0 },
			{ 0.038284, 0.016801, 0.0075110001, 0.001014, 0.0059420001, -0.00029600001, -0.015261, -0.001595, 0.0 },
			{ 0.19175699, 0.076344997, 0.019915, 0.000066000001, -0.0016129999, -0.002261, -0.002116, -0.017459, 0.0 },
			{ 0.352494, -0.17348, 0.048409, -0.041475002, 0.016357999, -0.0053940001, -0.01058, -0.01076, 0.0 } };

	

	double flookup0[] = { 0.15019999 };
	double flookup1[] = { -0.634166, 0.57873201 };
	double flookup2[] = { -1.779712, -0.44138399, 0.46759501, 1.793156, };
	double flookup3[] = { -4.279284, -2.434149, -1.321453, -0.50367099, -0.179827, 0.95061302, 1.948002, 3.5644481 };
	double flookup4[] = { -8.2962942, -6.1309638, -4.5550928, -3.4606631, -2.5201941, -1.753844, -1.099345, -0.57745999,
			-0.0081839999, 0.57659698, 1.292392, 2.085238, 2.9635129, 4.1180701, 5.794497, 8.2492685 };


	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String leadingZeros(String s, int length) {
		if (s.length() >= length)
			return s;
		else
			return String.format("%0" + (length - s.length()) + "d%s", 0, s);
	}

	static String hexToBin(String s) {
		return new BigInteger(s, 8).toString(2);
	}

	public static String hexToBinary(String hex) {
		return new BigInteger(hex, 16).toString(2);
	}

	public void init() {

		mbe_initMbeParms(currentp, prevp, prevpenh);
		highpassFilter.setHighPass(300, 8000, 4);
		lowpassFilter.setLowPass(3500, 8000, 4);
		   
		
	}

	public int processData(byte[] input, short[] response) {

		int frames = input.length / 12;

		short[] speech = new short[audioRate];

		int errors = 0;

		for (int a = 0; a < frames; a++) {

			StringBuilder record = new StringBuilder();

			errors += unfecRecord(Arrays.copyOfRange(input, a * 12, a * 12 + 12), record);

			int result = parseRecord(record.toString());

			if (result == 3) {

				speech = tone(1400);
				currentp.repeat = 0;
			} else if (errors > 3) {

				mbe_useLastMbeParms(currentp, prevp);
				currentp.repeat++;

			} else {

				currentp.repeat = 0;
			}

			if (result == 0) {

				if (currentp.repeat <= 3) {

					mbe_moveMbeParms(currentp, prevp);
					mbe_spectralAmpEnhance(currentp);
					speech = synthesizeSpeechf(currentp, prevpenh, 1);
					mbe_moveMbeParms(currentp, prevpenh);

				} else {

					mbe_synthesizeSilence(speech);
					mbe_initMbeParms(currentp, prevp, prevpenh);


				}
			} else {

				mbe_synthesizeSilence(speech);
				mbe_initMbeParms(currentp, prevp, prevpenh);

			}

			for (int b = 0; b < audioRate; b++) {

				response[(a * audioRate) + b] = speech[b];
			}

		}
		return errors;

	}


	public int parseRecord(String frame) {

		currentp.repeat = prevp.repeat;

		String dctblock = new String(frame).substring(9, 41) + new String(frame).substring(50, 69);

		int fundbase = Golay.fromBinary(frame.substring(0, 6));

		char[] b0 = new char[7];
		b0[0] = frame.charAt(0);
		b0[1] = frame.charAt(1);
		b0[2] = frame.charAt(2);
		b0[3] = frame.charAt(3);
		b0[4] = frame.charAt(4);
		b0[5] = frame.charAt(5);
		b0[6] = frame.charAt(71);

		int fundamental = Golay.fromBinary(new String(b0));

		char[] b1 = new char[8];
		b1[0] = frame.charAt(42);
		b1[1] = frame.charAt(43);
		b1[2] = frame.charAt(44);
		b1[3] = frame.charAt(45);
		b1[4] = frame.charAt(46);
		b1[5] = frame.charAt(47);
		b1[6] = frame.charAt(48);
		b1[7] = frame.charAt(49);
	
		int vuv = Golay.fromBinary(new String(b1));

		char[] b2 = new char[6];
		b2[0] = frame.charAt(6);
		b2[1] = frame.charAt(7);
		b2[2] = frame.charAt(8);
		b2[3] = frame.charAt(41);
		b2[4] = frame.charAt(69);
		b2[5] = frame.charAt(70);
		int gain = Golay.fromBinary(new String(b2));

		int silence = 0;

		if (fundbase == 0x35) {

		  silence = 3;
		}

		if ((fundbase > 0x34)) {

			silence = 1;
			currentp.w0 = ((2 * Math.PI) / 32);

			currentp.f0 = 1 / 32;
			currentp.L = 14;

			for (int l = 1; l <= currentp.L; l++) {
				currentp.Vl[l] = 0;
			}

			return 2;

		}
		if (silence == 0) {

			double w0 = ((float) (4 * Math.PI) / (double) ((double) (2 * fundamental) + 39.5));

			currentp.f0 = Math.pow(2, (-4.311767578125 - (3.7336e-2 * ((double) fundamental + 0.5))));
			currentp.w0 = w0;

			currentp.L = (int) (0.9254 * (10 + fundbase));
		}

		if (currentp.L < 9) {
			currentp.L = 9;
		} else if (currentp.L > 56) {
			currentp.L = 56;
		}

		double unvc = 0.2046 / Math.sqrt(currentp.w0);

		// Vl
		int j = 0;
		int c = currentp.L / 8;
		int k = 0;
		for (int i = 1; i <= currentp.L; i++) {
			currentp.Vl[i] = b1[k] - 48;
			if (j == c) {
				j = 0;
				if (k < 7) {
					k++;
				} else {
					k = 7;
				}
			} else {
				j++;
			}
		}

		double deltaGamma = (0.5) * Statics.AmbeDg[gain];
		currentp.gamma = deltaGamma + (0.5 * prevp.gamma);

		// read the bit allocation

		int[] alloc = Statics.bitalloc[currentp.L - 9];

		DecodeBlock[] blocks = new DecodeBlock[alloc.length];

		for (int a = 0; a < alloc.length; a++) {
			blocks[a] = new DecodeBlock(alloc[a]);

		}

		int fillbit = 0;

		outerloop: while (fillbit <= 51) {

			// cycle through the block lengths downward
			for (int b = 8; b > 0; b--) {

				for (int a = 0; a < blocks.length; a++) {

					if (blocks[a].max >= b && !blocks[a].full) {

						blocks[a].push(dctblock.charAt(fillbit));

						fillbit++;
						if (fillbit == 51) {
							fillbit++;
							continue outerloop;
						}

					}
				}
			}

		}

		double rho = 0.0;
		if (currentp.L <= 15) {
			rho = 0.3;
		} else if (currentp.L <= 24) {
			rho = 0.03 * ((double) currentp.L) - 0.05;

		} else {
			rho = 0.8;

		}

		double Gm[] = new double[9];

		double Cik[][] = new double[9][16];
		double sum = 0;

		Gm[1] = 0;
		for (int a = 0; a < 7; a++) {

			int rawcoeff = Golay.fromBinary(padZeroes(blocks[a].bitsfilled(), alloc[a]));

			Gm[a + 2] = scalePRBA(rawcoeff, alloc[a], a, rho);

		}

		int am = 0;

		for (int i = 1; i <= 8; i++) {
			sum = 0;
			for (int m = 1; m <= 8; m++) {
				if (m == 1) {
					am = 1;
				} else {
					am = 2;
				}
				sum = sum + (am * Gm[m] * Math.cos((Math.PI * (m - 1) * (i - 0.5)) / 8));
			}
			Cik[i][1] = sum;
		}

		// unpack the higher order coefficients

		int Ji[] = new int[9];

		for (int i = 1; i <= 8; i++) {
			// vector lengths for the higher order DCT coefficients
			Ji[i] = Statics.Aeroi[currentp.L - 9][i - 1];
		}

		int coeeffnum = 7;

		for (int i = 1; i <= 8; i++) {

			for (int a = 1; a < Ji[i]; a++) {

				int rawcoeff = Golay.fromBinary(padZeroes(blocks[coeeffnum].bitsfilled(), alloc[coeeffnum]));

				Cik[i][a + 1] = scaleHOC(rawcoeff, alloc[coeeffnum], i - 1, a - 1, rho);
				coeeffnum++;
			}

		}

		// inverse DCT each Ci,k to give ci,j (Tl)

		int l = 1;
		int ji = 0;
		int ak = 0;
		double[] Tl = new double[57];

		for (int i = 1; i <= 8; i++) {
			ji = Ji[i];
			for (j = 1; j <= ji; j++) {
				sum = 0;
				for (k = 1; k <= ji; k++) {
					if (k == 1) {
						ak = 1;
					} else {
						ak = 2;
					}
					sum = sum + (ak * Cik[i][k] * Math.cos((Math.PI * (k - 1) * (j - 0.5)) / ji));
				}
				Tl[l] = sum;

				l++;
			}
		}

		// determine log2Ml by applying ci,j to previous log2Ml

		// fix for when L > L(-1)
		if (currentp.L > prevp.L) {
			for (l = (prevp.L) + 1; l <= currentp.L; l++) {
				prevp.Ml[l] = prevp.Ml[prevp.L];
				prevp.log2Ml[l] = prevp.log2Ml[prevp.L];
			}
		}
		prevp.log2Ml[0] = prevp.log2Ml[1];
		prevp.Ml[0] = prevp.Ml[1];

		// Part 1
		double Sum43 = 0;

		double flokl[] = new double[57];
		double deltal[] = new double[57];
		int intkl[] = new int[57];

		for (l = 1; l <= currentp.L; l++) {

			// eq. 40
			flokl[l] = (double) ((double) prevp.L / (double) currentp.L) * (double) l;
			intkl[l] = (int) (flokl[l]);

			// eq. 41
			deltal[l] = flokl[l] - intkl[l];
			// eq 43
			Sum43 = Sum43 + (((1 - deltal[l]) * prevp.log2Ml[intkl[l]]) + (deltal[l] * prevp.log2Ml[intkl[l] + 1]));
		}
		Sum43 = ((rho / currentp.L) * Sum43);

		// Part 2
		double Sum42 = 0;
		for (l = 1; l <= currentp.L; l++) {
			Sum42 += Tl[l];
		}
		Sum42 = Sum42 / currentp.L;
		double BigGamma = currentp.gamma - (0.5 * (Math.log(currentp.L) / Math.log(2))) - Sum42;

		// Part 3
		for (l = 1; l <= currentp.L; l++) {
			double c1 = (rho * (1 - deltal[l]) * prevp.log2Ml[intkl[l]]);
			double c2 = (rho * deltal[l] * prevp.log2Ml[intkl[l] + 1]);
			currentp.log2Ml[l] = Tl[l] + c1 + c2 - Sum43 + BigGamma;
			;

			// inverse log to generate spectral amplitudes
			if (currentp.Vl[l] == 1) {
				currentp.Ml[l] = Math.pow(2, currentp.log2Ml[l]);
			} else {

				currentp.Ml[l] = unvc * Math.pow(2, currentp.log2Ml[l]);

			}

		}

		return silence;
	}

	private short[] synthesizeSpeechf(Parameters cur_mp, Parameters prev_mp, int uvquality) {

		double[] resultbuf = new double[audioRate];
		short[] result = new short[audioRate];

		int i, l, n, maxl;
		double loguvquality;
		double C1, C2, C3, C4;
		int numUv;
		double cw0, pw0, cw0l, pw0l;
		double uvsine, uvrand, uvthreshold, uvthresholdf;
		double uvstep, uvoffset;
		double qfactor;
		double rphase[] = new double[64];
		double rphase2[] = new double[64];

		int N = audioRate;

		uvthresholdf = 2700;
		uvthreshold = ((uvthresholdf * Math.PI) / 4000);

		// voiced/unvoiced/gain settings
		uvsine = (1.3591409 * Math.E);
		uvrand = (double)2.0;

		if ((uvquality < 1) || (uvquality > 64)) {
			System.out.println(
					"\nmbelib: Error - uvquality must be within the range 1 - 64, setting to default value of 3\n");
			uvquality = 3;
		}

		// calculate loguvquality
		if (uvquality == 1) {
			loguvquality = (double)1.0 / Math.E;
		} else {
			loguvquality = (Math.log((double)uvquality) / (double)uvquality);
		}

		// calculate unvoiced step and offset values
		uvstep = (double)1.0 / uvquality;
		qfactor = loguvquality;
		uvoffset = (uvstep * ((double)uvquality - 1)) / (double)2;

		// count number of unvoiced bands
		numUv = 0;
		for (l = 1; l <= cur_mp.L; l++) {
			if (cur_mp.Vl[l] == 0) {
				numUv++;
			}
		}

		cw0 = cur_mp.w0;
		pw0 = prev_mp.w0;

		// init aout_buf
		for (n = 0; n < N; n++) {
			resultbuf[n] = 0;

		}

		// eq 128 and 129
		if (cur_mp.L > prev_mp.L) {
			maxl = cur_mp.L;
			for (l = prev_mp.L + 1; l <= maxl; l++) {
				prev_mp.Ml[l] = 0;
				prev_mp.Vl[l] = 1;
			}
		} else {
			maxl = prev_mp.L;
			for (l = cur_mp.L + 1; l <= maxl; l++) {
				cur_mp.Ml[l] = 0;
				cur_mp.Vl[l] = 1;
			}
		}

		// update phil from eq 139,140
		for (l = 1; l <= 56; l++) {
			cur_mp.PSIl[l] = prev_mp.PSIl[l] + ((pw0 + cw0) * (((double) l * (double) N) / (double) 2));
			if (l <= (int) (cur_mp.L / 4)) {
				cur_mp.PHIl[l] = cur_mp.PSIl[l];
			} else {
				cur_mp.PHIl[l] = cur_mp.PSIl[l] + ((numUv * mbe_rand_phase()) / cur_mp.L);
			}
		}

		for (l = 1; l <= maxl; l++) {
			cw0l = (cw0 * (double) l);
			pw0l = (pw0 * (double) l);
			if ((cur_mp.Vl[l] == 0) && (prev_mp.Vl[l] == 1)) {

				// init random phase
				for (i = 0; i < uvquality; i++) {
					rphase[i] = mbe_rand_phase();
				}
				for (n = 0; n < N; n++) {
					C1 = 0;
					// eq 131
					C1 = Statics.Ws[n + N] * prev_mp.Ml[l] * Math.cos((pw0l * (double) n) + prev_mp.PHIl[l]);
					C3 = 0;
					// unvoiced multisine mix
					for (i = 0; i < uvquality; i++) {
						C3 = C3 + Math.cos((cw0 * (double) n * ((double) l + (i * uvstep) - uvoffset)) + rphase[i]);
						if (cw0l > uvthreshold) {
							C3 = C3 + ((cw0l - uvthreshold) * uvrand * mbe_rand());
						}
					}
					C3 = C3 * uvsine * Statics.Ws[n] * cur_mp.Ml[l] * qfactor;

					//
					resultbuf[n] = resultbuf[n] + C1 + C3;

				}
			} else if ((cur_mp.Vl[l] == 1) && (prev_mp.Vl[l] == 0)) {
				// init random phase
				for (i = 0; i < uvquality; i++) {
					rphase[i] = mbe_rand_phase();
				}
				for (n = 0; n < N; n++) {
					C1 = 0;
					// eq 132
					C1 = Statics.Ws[n] * cur_mp.Ml[l] * Math.cos((cw0l * (n - N)) + cur_mp.PHIl[l]);
					C3 = 0;
					// unvoiced multisine mix
					for (i = 0; i < uvquality; i++) {
						C3 = C3 + Math.cos((pw0 * n * (l + (i * uvstep) - uvoffset)) + rphase[i]);
						if (pw0l > uvthreshold) {
							C3 = C3 + ((pw0l - uvthreshold) * uvrand * mbe_rand());
						}
					}
					C3 = C3 * uvsine * Statics.Ws[n + N] * prev_mp.Ml[l] * qfactor;

					resultbuf[n] = resultbuf[n] + C1 + C3;

				}
			} 
			
	/*		
			else if ((cur_mp.Vl[l] == 1) || (prev_mp.Vl[l] == 1)) {

				for (n = 0; n < N; n++) {
					C1 = 0;
					// eq 133-1
					C1 = am2statics.Ws[n + N] * prev_mp.Ml[l] * Math.cos((pw0l * n) + prev_mp.PHIl[l]);
					C2 = 0;
					// eq 133-2
					C2 = am2statics.Ws[n] * cur_mp.Ml[l] * Math.cos((cw0l * (n - N)) + cur_mp.PHIl[l]);
					resultbuf[n] = resultbuf[n] + C1 + C2;

				}
			}

		*/	
			
			
		      // expensive and unnecessary?
			else if ((cur_mp.Vl[l] == 1) || (prev_mp.Vl[l] == 1)) {
		        {
		          // eq 137
		          float deltaphil = (float) (cur_mp.PHIl[l] - prev_mp.PHIl[l] - (((pw0 + cw0) * (float) (l * N)) / (float) 2));
		          // eq 138
		          float deltawl = (float) (((float) 1 / (float) N) * (deltaphil - ((float) 2 * Math.PI * (int) ((deltaphil + Math.PI) / (Math.PI * (float) 2)))));
		          for (n = 0; n < N; n++)
		            {
		              // eq 136
		              float thetaln = (float) (prev_mp.PHIl[l] + ((pw0l + deltawl) * (float) n) + (((cw0 - pw0) * ((float) (l * n * n)) / (float) (2 * N))));
		              // eq 135
		              float aln = (float) (prev_mp.Ml[l] + (((float) n / (float) N) * (cur_mp.Ml[l] - prev_mp.Ml[l])));
		              // eq 134
		              resultbuf[n] = resultbuf[n] + (aln* Math.cos(thetaln)); 
		          
		              
		            }
		        }
			}
		
			else {

				// init random phase
				for (i = 0; i < uvquality; i++) {
					rphase[i] = mbe_rand_phase();
				}
				// init random phase
				for (i = 0; i < uvquality; i++) {
					rphase2[i] = mbe_rand_phase();
				}
				for (n = 0; n < N; n++) {
					C3 = 0;
					// unvoiced multisine mix
					for (i = 0; i < uvquality; i++) {
						C3 = C3 + Math.cos((pw0 * n * (l + (i * uvstep) - uvoffset)) + rphase[i]);
						if (pw0l > uvthreshold) {
							C3 = C3 + ((pw0l - uvthreshold) * uvrand * mbe_rand());
						}
					}
					C3 = C3 * uvsine * Statics.Ws[n + N] * prev_mp.Ml[l] * qfactor;
					C4 = 0;
					// unvoiced multisine mix
					for (i = 0; i < uvquality; i++) {
						C4 = C4 + Math.cos((cw0 * n * (l + (i * uvstep) - uvoffset)) + rphase2[i]);
						if (cw0l > uvthreshold) {
							C4 = C4 + ((cw0l - uvthreshold) * uvrand * mbe_rand());
						}
					}
					C4 = C4 * uvsine * Statics.Ws[n] * cur_mp.Ml[l] * qfactor;
						resultbuf[n] = resultbuf[n] + C3 + C4;

				}
			}
		}

		for (int a = 0; a < audioRate; a++) {
			
		    result[a] = mbe_doubletoshort(lowpassFilter.update(highpassFilter.update(resultbuf[a])));

		}

		return result;
	}

	public double mbe_rand_phase() {

		double random = Math.random();

		random = random * ((Math.PI * 2.0) - (Math.PI));

		return random;
	}

	public double mbe_rand() {

		return Math.random();

	}

	public void mbe_moveMbeParms(Parameters cur_mp, Parameters prev_mp) {

		int l;

		prev_mp.w0 = cur_mp.w0;
		prev_mp.L = cur_mp.L;
		prev_mp.Ml[0] = 0;
		prev_mp.gamma = cur_mp.gamma;
		prev_mp.repeat = cur_mp.repeat;
		for (l = 0; l <= 56; l++) {
			prev_mp.Ml[l] = cur_mp.Ml[l];
			prev_mp.Vl[l] = cur_mp.Vl[l];
			prev_mp.log2Ml[l] = cur_mp.log2Ml[l];
			prev_mp.PHIl[l] = cur_mp.PHIl[l];
			prev_mp.PSIl[l] = cur_mp.PSIl[l];
		}
	}

	public void mbe_useLastMbeParms(Parameters cur_mp, Parameters prev_mp) {

		int l;

		cur_mp.w0 = prev_mp.w0;
		cur_mp.L = prev_mp.L;
		cur_mp.Ml[0] = 0;
		cur_mp.gamma = prev_mp.gamma;
		cur_mp.repeat = prev_mp.repeat;
		for (l = 0; l <= 56; l++) {
			cur_mp.Ml[l] = prev_mp.Ml[l];
			cur_mp.Vl[l] = prev_mp.Vl[l];
			cur_mp.log2Ml[l] = prev_mp.log2Ml[l];
			cur_mp.PHIl[l] = prev_mp.PHIl[l];
			cur_mp.PSIl[l] = prev_mp.PSIl[l];
		}
	}

	public void mbe_initMbeParms(Parameters cur_mp, Parameters prev_mp, Parameters prev_mp_enhanced) {

		int l;

		prev_mp.w0 = 0.09378;
		prev_mp.L = 30;
		prev_mp.gamma = 0;
		for (l = 0; l <= 56; l++) {
			prev_mp.Ml[l] = 0;
			prev_mp.Vl[l] = 0;
			prev_mp.log2Ml[l] = 0; // log2 of 1 == 0
			prev_mp.PHIl[l] = 0;
			prev_mp.PSIl[l] = (Math.PI / 2);
		}
		prev_mp.repeat = 0;
		mbe_moveMbeParms(prev_mp, cur_mp);
		mbe_moveMbeParms(prev_mp, prev_mp_enhanced);
	}

	public void mbe_spectralAmpEnhance(Parameters cur_mp) {

		double Rm0, Rm1, R2m0, R2m1;
		double[] Wl = new double[57];
		int l;
		double sum, gamma, M;

		Rm0 = 0;
		Rm1 = 0;
		for (l = 1; l <= cur_mp.L; l++) {
			Rm0 = Rm0 + (cur_mp.Ml[l] * cur_mp.Ml[l]);
			Rm1 = Rm1 + ((cur_mp.Ml[l] * cur_mp.Ml[l]) * Math.cos(cur_mp.w0 * l));
		}

		R2m0 = (Rm0 * Rm0);
		R2m1 = (Rm1 * Rm1);

		for (l = 1; l <= cur_mp.L; l++) {
			if (cur_mp.Ml[l] != 0) {
				Wl[l] = Math.sqrt(cur_mp.Ml[l])
						* Math.pow(((0.96 * Math.PI * ((R2m0 + R2m1) - (2 * Rm0 * Rm1 * Math.cos(cur_mp.w0 * l))))
								/ (cur_mp.w0 * Rm0 * (R2m0 - R2m1))), 0.25);

				if ((8 * l) <= cur_mp.L) {
				} else if (Wl[l] > 1.2) {
					cur_mp.Ml[l] = 1.2 * cur_mp.Ml[l];
				} else if (Wl[l] < 0.5) {
					cur_mp.Ml[l] = 0.5 * cur_mp.Ml[l];
				} else {
					cur_mp.Ml[l] = Wl[l] * cur_mp.Ml[l];
				}
			}
		}

		// generate scaling factor
		sum = 0;
		for (l = 1; l <= cur_mp.L; l++) {
			M = cur_mp.Ml[l];
			if (M < 0) {
				M = -M;
			}
			sum += (M * M);
		}
		if (sum == 0) {
			gamma = 1.0;
		} else {
			gamma = Math.sqrt(Rm0 / sum);
		}

		// apply scaling factor
		for (l = 1; l <= cur_mp.L; l++) {
			cur_mp.Ml[l] = gamma * cur_mp.Ml[l];
		}
	}

	public short mbe_doubletoshort(double din) {

		double again = 2;
		double audio;
		short output;

		audio = again * din;
		if (audio > 32760) {

			audio = 32760;
		} else if (audio < -32760) {
			audio = -32760;
		}
		output = (short) (audio);

		return output;

	}

	public String padZeroes(String in, int len) {

		if (len == 0)
			return "0";

		while (in.length() < len) {
			in += "0";
		}

		return in;
	}

	public double scalePRBA(int value, int bits, int pos, double rho) {

		double x = 0.0;

		if (bits == 4) {

			x = flookup4[value];
		} else if (bits == 3) {
			x = flookup3[value];

		} else if (bits == 2) {
			x = flookup2[value];
		} else if (bits == 1) {
			x = flookup1[value];

		} else {

			x = (double) value - Math.pow(2, (bits - 1));

			if (x < 0) {
				x -= 0.5;

			} else {
				x += 0.5;
			}
		}

		x = x * quantstep[bits - 1] * dq_prba_mul[pos] + (dq_prba_add[pos] * (1 - rho));

		return x;

	}

	public double scaleHOC(int value, int bits, int group, int pos, double rho) {

		double x = 0.0;

		if (bits == 4) {

			x = flookup4[value];
		} else if (bits == 3) {
			x = flookup3[value];

		} else if (bits == 2) {
			x = flookup2[value];
		} else if (bits == 1) {
			x = flookup1[value];

		} else if (bits == 0) {
			return 0.0;

		} else {

			x = (double) value - Math.pow(2, (bits - 1));

			if (x < 0) {
				x -= 0.5;

			} else {
				x += 0.5;
			}
		}
		x = x * quantstep[bits - 1] * dq_hoc_mul[group][pos] + (dq_hoc_add[group][pos] * (1 - rho));

		return x;

	}

	public int unfecRecord(byte[] readbuffer, StringBuilder result) {

		int errors = 0;

		String binary = leadingZeros(hexToBinary(bytesToHex(readbuffer)), 96);

		char[] reversed = new char[96];

		int bit = 0;
		int ibyte = 0;

		// reverse the bit order in each byte
		for (int a = 0; a < 96; a++) {

			reversed[(ibyte * 8) + (7 - bit)] = binary.charAt((ibyte * 8) + bit);

			if (bit == 7) {

				bit = 0;
				ibyte++;
			} else {

				bit++;
			}
		}

		// deinterleave
		char[] newleavered = new char[96];

		for (int a = 0; a < 96; a++) {
			newleavered[a] = reversed[new97leavertable[a]];

		}

		String golayblock = new String(newleavered).substring(1, 24);

		int cw = Golay.fromBinary(golayblock);

		if (Golay.parity(cw) != Character.getNumericValue(newleavered[0])) {

			// System.out.println("Golay parity does not match ");

		}

		char[] out = new char[23];

		errors += golay.mbe_golay2312(golayblock.toCharArray(), out);

		if (errors > 0) {
			// System.out.println("Golay errors: " + errors);
		}

		// work out the scramble key
		String golayblockreversed = "";
		golayblock = golayblock.substring(0, 12);
		for (int a = 22; a >= 11; a--) {

			golayblockreversed += out[a];

		}

		String rebuilt = golayblockreversed.substring(0, 12);

		Scrambler scrambler = new Scrambler(Golay.fromBinary(golayblockreversed));

		// unscramble
		char[] full_hamming = Arrays.copyOfRange(newleavered, 24, 69);

		for (int a = 0; a < 45; a++) {

			full_hamming[a] = (char) (full_hamming[a] ^ scrambler.pr[a + 1]);

		}

		String hamming1 = new String(Arrays.copyOfRange(full_hamming, 0, 15));
		String hamming2 = new String(Arrays.copyOfRange(full_hamming, 15, 30));
		String hamming3 = new String(Arrays.copyOfRange(full_hamming, 30, 45));

		char[] hout = new char[15];
		errors += golay.mbe_hamming1511(hamming1.toCharArray(), hout);
		rebuilt += new String(hout).substring(0, 11);

		errors += golay.mbe_hamming1511(hamming2.toCharArray(), hout);
		rebuilt += new String(hout).substring(0, 11);

		errors += golay.mbe_hamming1511(hamming3.toCharArray(), hout);
		rebuilt += new String(hout).substring(0, 11);

		rebuilt += new String(newleavered).substring(69, 96);

		result.append(rebuilt);

		return errors;

	}


	public void mbe_synthesizeSilence(short[] aout_buf) {

		int n;
		for (n = 0; n < audioRate; n++) {
			aout_buf[n] = (short) 0;

		}
	}


	public static short[] tone(double hz) {

		int n = audioRate;
		short[] result = new short[n];
		for (int i = 0; i < n; i++) {
			result[i] = (short) (2 * Math.PI * i * hz / 8000);
		}
		return result;
	}


	static void
	ambe_gen_random(int[] u_seq, int u_prev, int n)
	{
		int u = u_prev;
		int i;

		for (i=0; i<n; i++) {
			u = (u * 171 + 11213) % 53125;
			u_seq[i] = u;
		}
	}
	
	

}

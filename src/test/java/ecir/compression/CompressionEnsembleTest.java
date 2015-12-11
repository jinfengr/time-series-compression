package ecir.compression;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.BiMap;

import ecir.compression.*;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree;
import ecir.encoding.Simple16Encoding;
import ecir.encoding.HuffmanTree.Unit;
import ecir.encoding.VariableByteEncoding;
import ecir.encoding.WaveletEncoding;

public class CompressionEnsembleTest {
	
	private static int[][] count;
	@Before
	public void setUp() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("data/unigram_counts.txt"));
		String line = bf.readLine(); // read first line;
		int rows = Integer.parseInt(line.split(",")[0]);
		int columns = Integer.parseInt(line.split(",")[1]);
		count = new int[rows][columns];
		int linecounter = 0;
		while((line=bf.readLine())!=null){
			String[] groups = line.split(",");
			String word = groups[0]; 
			int day = Integer.parseInt(groups[1]);
			int[] daycount = new int[groups.length-2];
			for (int i = 2; i < groups.length; i++) {
				daycount[i-2] = Integer.parseInt(groups[i]);
			}
			count[linecounter++] = daycount;
		}
	}

	@Test
	public void testPForDelta() throws IOException {
		for (int i = 0; i < count.length; i++) {
			long beforeCompress = System.currentTimeMillis();
			byte[] compressData = RawCountCompression.PForDeltaCompression(count[i]);
			long endCompress = System.currentTimeMillis();
			int[] decompressData = RawCountCompression
					.PForDeltaDecompression(compressData);
			long endDecompress = System.currentTimeMillis();
			
			assertArrayEquals(decompressData, count[i]);
			long endJudge = System.currentTimeMillis();
		}
	}

	@Test
	public void testVariableByte() {
		for (int i = 0; i < count.length; i++) {
			byte[] compressData = VariableByteEncoding.encode(count[i]);
			int[] decompressData = VariableByteEncoding.decode(compressData);
			assertArrayEquals(decompressData, count[i]);
		}
	}
	
	@Test
	public void testSimple16() throws IOException {
		for (int i = 0; i < count.length; i++) {
			byte[] compressData = Simple16Encoding.encode(count[i]);
			int[] decompressData = Simple16Encoding.decode(compressData);
			assertArrayEquals(decompressData, count[i]);
		}
	}
	
	@Test
	public void testWaveletEncoding() {
		// generate random data
		/*int[] testData = new int[256];
		int max = 65536, min = 0;
		for (int i = 0; i < testData.length; i++) {
			testData[i] = rand.nextInt(max - min) + min;
		}*/
		for (int i = 0; i < count.length; i++) {
			// encoding and decoding
			int n = getLastPowerNumber(count[i].length);
			int[] cutCount = Arrays.copyOfRange(count[i], 0, n);
			int[] encodeData = WaveletEncoding.encode(cutCount);
			int[] decodeData = WaveletEncoding.decode(encodeData);
	
			int errorRange = (int) (Math.log(n) / Math.log(2));
			assertEquals(cutCount.length, decodeData.length);
			for (int j = 0; j < decodeData.length; j++) {
				assertTrue(cutCount[j] > decodeData[j] - errorRange);
				assertTrue(cutCount[j] < decodeData[j] + errorRange);
			}
		}
	}
	
	/*@Test
	public void testDWTPForDelta() throws IOException{
		for (int i = 0; i < count.length; i++) {
			byte[] compressData = WaveletCompression.PForDeltaCompression(count[i]);
			int[] decompressData = WaveletCompression.PForDeltaDecompression(compressData);
			int lastPowNum = getLastPowerNumber(count[i].length);
			int errorRange = (int) (Math.log(lastPowNum) / Math.log(2));
			for(int j=0; j<decompressData.length;j++){
				if(i<lastPowNum){
					assertTrue(count[i][j] >= decompressData[j] - errorRange);
					assertTrue(count[i][j] <= decompressData[j] + errorRange);
				}else{
					assertTrue(count[i][j] == decompressData[j]);
				}
			}
		}
	}*/
	
	@Test
	public void testDWTVariableByte(){
		for (int i = 0; i < count.length; i++) {
			byte[] compressData = WaveletCompression.VariableByteCompression(count[i]);
			int[] decompressData = WaveletCompression.VariableByteDecompression(compressData);
			int lastPowNum = getLastPowerNumber(count[i].length);
			int errorRange = (int) (Math.log(lastPowNum) / Math.log(2));
			for(int j=0; j<decompressData.length;j++){
				if(i<lastPowNum){
					assertTrue(count[i][j] >= decompressData[j] - errorRange);
					assertTrue(count[i][j] <= decompressData[j] + errorRange);
				}else{
					assertTrue(count[i][j] == decompressData[j]);
				}
			}
		}
	}
	
	@Test
	public void testHuffmanEncoding() throws IOException{
		HashMap<Unit, Integer> freqs = new HashMap<Unit, Integer>();
		int[] allcounts = new int[count.length*count[0].length];
		for (int i = 0; i < count.length; i++) {
			System.arraycopy(count[i], 0, allcounts, i*count[i].length, count[i].length);
		}
		HuffmanEncoding.GenerateFreqDict(allcounts, freqs);
		HuffmanTree tree = HuffmanTree.buildTree(freqs);
		BiMap<Unit, String> huffmanCodes = tree.getCodes();
		for (int i = 0; i < count.length; i++) {
			int[] codes = HuffmanEncoding.encode(count[i], huffmanCodes);
			byte[] compression = VariableByteEncoding.encode(codes);
			int [] decompression = VariableByteEncoding.decode(compression);
			int[] decodes = HuffmanEncoding.decode(decompression, huffmanCodes);
			assertArrayEquals(decodes, count[i]);
		}
	}
	
	public int getLastPowerNumber(int n){
		int num = 1;
		while(n > 1){
			num *= 2;
			n /= 2;
		}
		return num;
	}
}

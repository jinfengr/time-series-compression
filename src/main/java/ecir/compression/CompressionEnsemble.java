package ecir.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;

import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree.Unit;
import ecir.encoding.Simple16Encoding;
import ecir.encoding.VariableByteEncoding;

public class CompressionEnsemble {
	
	public static final int P4D = 0;
	public static final int VB = 1;
	public static final int S16 = 2;
	public static final int DWT_VB = 3;
	public static final int HUFFMAN = 4;
	public static final int HUFFMAN_VB = 5;
	
	public static enum ALG_ENUM {P4D, VB, S16, DWT_VB, HUFFMAN, HUFFMAN_VB, NOCOMPRESSION};
	public static Map<Integer, Algorithm> algorithmMap;
	public static CompressionEnsemble ensemble = new CompressionEnsemble();
	public static BiMap<Unit, String> huffmanTree;
	public static int counter = 0;
	
	public class Algorithm {
		int algorithm;
		long bytes;
		long decodeTime;
		
		public Algorithm(int algorithm) {
			this.algorithm = algorithm;
			this.bytes = 0;
			this.decodeTime = 0;
		}
		
		public void increment(long bytes, long decodeTime) {
			this.bytes += bytes;
			this.decodeTime += decodeTime;
		}
	}
	
	public static void init(BiMap<Unit, String> inputHuffmanTree) {
		huffmanTree = inputHuffmanTree;
		algorithmMap = new HashMap<Integer, Algorithm>();
		for (ALG_ENUM algo: ALG_ENUM.values()) {
			algorithmMap.put(algo.ordinal(), ensemble.new Algorithm(algo.ordinal()));
		}
	}
	
	public static void compression(int[] counts) throws IOException{
		counter++;
		byte[] compression = null;
		int[] decompression = null;
		int[] encodeData = null, originalData = null;
		long startTime = 0, endTime = 0;
		for (ALG_ENUM algo: ALG_ENUM.values()) {
			switch (algo.ordinal()){
				case 0: // PForDelta
					compression = RawCountCompression.PForDeltaCompression(counts);
					startTime = System.currentTimeMillis();
					decompression = RawCountCompression.PForDeltaDecompression(compression);
					endTime = System.currentTimeMillis();
					break;
				case 1: // Variable Byte
					compression = VariableByteEncoding.encode(counts);
					startTime = System.currentTimeMillis();
					decompression = VariableByteEncoding.decode(compression);
					endTime = System.currentTimeMillis();
					break;
				case 2: // Simple 16
					compression = Simple16Encoding.encode(counts);
					startTime = System.currentTimeMillis();
					decompression = Simple16Encoding.decode(compression);
					endTime = System.currentTimeMillis();
					break;
				case 3: // Variable Bytes after Wavelet Transformation
					compression = WaveletCompression.VariableByteCompression(counts);
					startTime = System.currentTimeMillis();
					decompression = WaveletCompression.VariableByteDecompression(compression);
					endTime = System.currentTimeMillis();
					break;
				case 4: // HuffmanTree
					encodeData = HuffmanEncoding.encode(counts, huffmanTree);
					startTime = System.currentTimeMillis();
					decompression = HuffmanEncoding.decode(encodeData, huffmanTree);
					endTime = System.currentTimeMillis();
					break;
				case 5: //HuffmanTree VB
					encodeData = HuffmanEncoding.encode(counts, huffmanTree);
					compression = VariableByteEncoding.encode(encodeData);
					startTime = System.currentTimeMillis();
					decompression = VariableByteEncoding.decode(compression);
					originalData = HuffmanEncoding.decode(decompression, huffmanTree);
					endTime = System.currentTimeMillis();
					break;	
				case 6: // No Compression
				  break;
			}
			int algoId = algo.ordinal();
			int bytes;
			if (algoId == 6) { // no compression;
			  bytes = counts.length*4;
			  startTime = endTime = 0;
			} else if (algoId == 4) { // Huffman
			  bytes = encodeData.length * 4;
			} else {
			  bytes = compression.length;
			}
			Algorithm algorithm = algorithmMap.get(algoId);
			algorithm.increment(bytes, endTime - startTime);
			algorithmMap.put(algoId, algorithm);
		}
	}
	
	public static void printResults() {
		System.out.println("Total " + counter + " Lines of Counts.");
		for (ALG_ENUM algo: ALG_ENUM.values()) {
			Algorithm algorithm = algorithmMap.get(algo.ordinal());
			int KB = 1024;
			System.out.print(algo.name() + " bytes(B): " + algorithm.bytes);
			System.out.println(" decode time(us): " + algorithm.decodeTime);
		}
	}
}

package ecir.wordcount;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.collect.BiMap;

import ecir.compression.CompressionEnsemble;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree;
import ecir.encoding.HuffmanTree.Unit;
import ecir.encoding.VariableByteEncoding;
import ecir.wordcount.MemoryCounts;

public class BigramComparison {
	
	public static MemoryCounts M;
	public static BiMap<Unit, String> bigramHuffmanTree;
	public static BiMap<Unit, String> unigramHuffmanTree;
	public static byte[] selector = {(byte)0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
	
	public static byte[] getBitVector(String word, int day, int size) {
		if (!M.termIdTable.contains(word, day)) {
			byte[] bitVector = new byte[size];
			for (int i = 0; i < bitVector.length; i += 8) {
				bitVector[i/8] = 0;
			}
			return bitVector;
		}
		int termId = M.termIdTable.get(word, day);
		int offset = M.offset.get(termId);
		int length = M.length.get(termId);
		byte[] compressData = M.data.subList(offset, offset+length).toByteArray();
		int[] decompressData = VariableByteEncoding.decode(compressData);
		int[] origData = HuffmanEncoding.decode(decompressData, unigramHuffmanTree);
		byte[] bitVector = new byte[origData.length/8];
		for (int i = 0; i < origData.length; i++) {
			bitVector[i/8] <<= 1;
			bitVector[i/8] |= (origData[i] != 0) ? 1 : 0;
		}
		return bitVector;
	}
	
	public static byte[] intersect(byte[] bitVector1, byte[] bitVector2) {
		byte[] intersectBitVector = new byte[bitVector1.length];
		for (int i = 0; i < bitVector1.length; i++) {
			intersectBitVector[i] = (byte) (bitVector1[i] & bitVector2[i]);
		}
		return intersectBitVector;
	}
	
	public static void LoadBigramCounts(String filePath) throws IOException {
		System.out.println("Processing "+filePath);
		BufferedReader bf = new BufferedReader(new FileReader(filePath));
		String line;
		byte[] bitVector1, bitVector2, intersectBitVector;
		
		while((line=bf.readLine())!=null){
			String[] groups = line.split(",");
			String bigram = groups[0]; 
			int day = Integer.parseInt(groups[1]);
			int[] count = new int[groups.length-2];
			for (int i = 2; i < groups.length; i++) {
				count[i-2] = Integer.parseInt(groups[i]);
			}
			String[] words = bigram.split("\\s+");
			bitVector1 = BigramComparison.getBitVector(words[0], day, groups.length-2);
			bitVector2 = BigramComparison.getBitVector(words[1], day, groups.length-2);
			intersectBitVector = BigramComparison.intersect(bitVector1, bitVector2);
			IntArrayList bigramCounts = new IntArrayList();
			for (int i = 0; i < count.length; i++) { 
				if ((intersectBitVector[i/8] & BigramComparison.selector[i%8]) != 0) {
					bigramCounts.add(count[i]);
				}
			}
			if (bigramCounts.size() > 0) {
				CompressionEnsemble.compression(bigramCounts.toIntArray());
			}
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String bigramCountPath = args[0];
		String bigramHuffmanTreePath = args[1];
		String unigramCountPath = args[2];
		String unigramHuffmanTreePath = args[3];
		M = CompressUnigramCount.load(unigramCountPath);
		unigramHuffmanTree = HuffmanEncoding.loadHuffmanTree(unigramHuffmanTreePath);
		bigramHuffmanTree = HuffmanEncoding.loadHuffmanTree(bigramHuffmanTreePath);
		CompressionEnsemble.init(bigramHuffmanTree);
		LoadBigramCounts(bigramCountPath);
		CompressionEnsemble.printResults();
	}
}

package ecir.wordcount;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.google.common.collect.BiMap;

import ecir.compression.CompressionEnsemble;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree;
import ecir.encoding.HuffmanTree.Unit;

public class BuildBigramHuffmanTree {
	
	public static HashMap<Unit, Integer> dict = new HashMap<Unit, Integer>();
	
	public static void LoadBigramCounts(String filePath) throws IOException {
		System.out.println("Processing "+filePath);
		BufferedReader bf = new BufferedReader(new FileReader(filePath));
		String line;
		byte[] bitVector1, bitVector2, intersectBitVector;
		
		while((line=bf.readLine())!=null){
			if (line.length() == 0) continue;
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
				HuffmanEncoding.GenerateFreqDict(bigramCounts.toIntArray(), dict);
			}
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String bigramCountPath = args[0];
		String unigramCountPath = args[1];
		String unigramHuffmanTreePath = args[2];
		String bigramHuffmanPrefix = args[3];
		// Load Unigram Counts and Huffman Tree
		BigramComparison.M = CompressUnigramCount.load(unigramCountPath);
		BigramComparison.unigramHuffmanTree = HuffmanEncoding.loadHuffmanTree(unigramHuffmanTreePath);
		System.out.println("Load Unigram Counts and Huffman Tree successfully.");
		LoadBigramCounts(bigramCountPath);
		HuffmanEncoding.saveDict(dict, bigramHuffmanPrefix);
		HuffmanTree tree = HuffmanTree.buildTree(dict);
		BiMap<Unit, String> huffmanCodes = tree.getCodes();
		HuffmanEncoding.saveHuffmanTree(huffmanCodes, bigramHuffmanPrefix);
	}

}

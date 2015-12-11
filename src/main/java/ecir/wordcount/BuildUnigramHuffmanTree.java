package ecir.wordcount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.common.collect.BiMap;

import ecir.compression.CompressionEnsemble;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree;
import ecir.encoding.HuffmanTree.Unit;

public class BuildUnigramHuffmanTree {
	
	static HashMap<Unit, Integer> dict = new HashMap<Unit, Integer>();
	
	public static void LoadCounts(String filePath) throws IOException {
		System.out.println("Processing "+filePath);
		BufferedReader bf = new BufferedReader(new FileReader(filePath));
		String line = bf.readLine(); // read first line;
		
		while((line=bf.readLine())!=null){
			String[] groups = line.split(",");
			String word = groups[0]; 
			int day = Integer.parseInt(groups[1]);
			int[] count = new int[groups.length-2];
			for (int i = 2; i < groups.length; i++) {
				count[i-2] = Integer.parseInt(groups[i]);
			}
			HuffmanEncoding.GenerateFreqDict(count, dict);
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String wordCountPath = args[0];
		String huffmanFilePrefix = args[1];
		LoadCounts(wordCountPath);
		HuffmanEncoding.saveDict(dict, huffmanFilePrefix);
		HuffmanTree tree = HuffmanTree.buildTree(dict);
		BiMap<Unit, String> huffmanCodes = tree.getCodes();
		HuffmanEncoding.saveHuffmanTree(huffmanCodes, huffmanFilePrefix);
	}
}

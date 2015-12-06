package ecir.wordcount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ecir.compression.CompressionEnsemble;
import ecir.compression.HuffmanCompression;
import ecir.compression.RawCountCompression;
import ecir.compression.WaveletCompression;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree;
import ecir.encoding.Simple16Encoding;
import ecir.encoding.VariableByteEncoding;
import ecir.encoding.HuffmanTree.Unit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class UnigramComparison {
			
	static int recordCnt = 0;
	static long length;
	static BiMap<Unit, String> huffmanTree;
	static int Threshold = 1;
	
	public static void CompressCounts(String filePath) throws IOException {
		System.out.println("Processing "+filePath);
		BufferedReader bf = new BufferedReader(new FileReader(filePath));
		String line;
		while((line=bf.readLine())!=null){
			String[] groups = line.split(",");
			String word = groups[0]; 
			int day = Integer.parseInt(groups[1]);
			int[] count = new int[groups.length-2];
			for (int i = 2; i < groups.length; i++) {
				count[i-2] = Integer.parseInt(groups[i]);
			}
			CompressionEnsemble.compression(count);
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String wordCountPath = args[0];
		String huffmanTreePath = args[1];
		huffmanTree = HuffmanEncoding.loadHuffmanTree(huffmanTreePath);
		CompressionEnsemble.init(huffmanTree);
		CompressCounts(wordCountPath);
		CompressionEnsemble.printResults();
	}

}

package ecir.wordcount;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ecir.compression.CompressionEnsemble;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.HuffmanTree.Unit;
import ecir.encoding.VariableByteEncoding;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class CompressUnigramCount {
	
	private static MemoryCounts M = new MemoryCounts();
	private static BiMap<Unit, String> huffmanTree;
	public static int termId = 0;
	public static int byteCounter = 0;
	
	public static void CompressCounts(String filePath) throws IOException {
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
			// Using our best Huffman-based approach to compress the counts.
			int[] huffmanEncoding = HuffmanEncoding.encode(count, huffmanTree);
			byte[] compressData = VariableByteEncoding.encode(huffmanEncoding);
			M.termIdTable.put(word, day, termId);
			M.offset.add(byteCounter);
			M.length.add((short) compressData.length);
			M.data.addAll(new ByteArrayList(compressData));
			byteCounter += compressData.length;
			termId++;
		}
	}
	
	public static MemoryCounts LoadWordCountToMemory(String wordCountPath,
			String huffmanTreePath) {
		try {
			huffmanTree = HuffmanEncoding.loadHuffmanTree(huffmanTreePath);
			CompressCounts(wordCountPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return M;
	}
	
	public static void save(String fileAddr) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileAddr));
	    out.writeObject(M.termIdTable);
	    out.writeObject(M.data);
	    out.writeObject(M.offset);
	    out.writeObject(M.length);
	    out.close();
	}
	
	public static MemoryCounts load(String fileAddr) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileAddr));
		MemoryCounts M = new MemoryCounts();
		M.termIdTable = (Table<String, Integer, Integer>) in.readObject();
		M.data = (ByteArrayList) in.readObject();
		M.offset = (IntArrayList) in.readObject();
		M.length = (ShortArrayList) in.readObject();
		in.close();
		return M;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String wordCountPath = args[0];
		String huffmanTreePath = args[1];
		String outputPath = args[2];
		huffmanTree = HuffmanEncoding.loadHuffmanTree(huffmanTreePath);
		CompressCounts(wordCountPath);
		save(outputPath);
	}

}

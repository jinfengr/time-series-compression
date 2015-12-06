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
import java.util.concurrent.TimeUnit;

import ecir.compression.CompressionEnsemble;
import ecir.encoding.HuffmanEncoding;
import ecir.encoding.VariableByteEncoding;
import ecir.encoding.HuffmanTree.Unit;

import com.google.common.collect.BiMap;
import com.google.common.collect.Table;

public class CompressBigramCount {
	private static MemoryCounts bigramM = new MemoryCounts();
	private static BiMap<Unit, String> bigramHuffmanTree;
	private static int termId = 0;
	private static int byteCounter = 0;
	
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
				int[] huffmanEncoding = HuffmanEncoding.encode(bigramCounts.toIntArray(), bigramHuffmanTree);
				byte[] compressData = VariableByteEncoding.encode(huffmanEncoding);
				bigramM.termIdTable.put(bigram, day, termId);
				bigramM.offset.add(byteCounter);
				bigramM.length.add((short) compressData.length);
				bigramM.data.addAll(new ByteArrayList(compressData));
				byteCounter += compressData.length;
				termId++;
			}
		}
		System.out.println("Load Bigrams Done, Number of Terms: " + termId);
	}
	
	public static void save(String fileAddr) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileAddr));
	    out.writeObject(bigramM.termIdTable);
	    out.writeObject(bigramM.data);
	    out.writeObject(bigramM.offset);
	    out.writeObject(bigramM.length);
	    out.close();
	}
	
	public static MemoryCounts load(String fileAddr) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileAddr));
		MemoryCounts bigramM = new MemoryCounts();
		bigramM.termIdTable = (Table<String, Integer, Integer>) in.readObject();
		bigramM.data = (ByteArrayList) in.readObject();
		bigramM.offset = (IntArrayList) in.readObject();
		bigramM.length = (ShortArrayList) in.readObject();
		in.close();
		return bigramM;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String bigramRawCountPath = args[0];
		String bigramHuffmanTreePath = args[1];
		String unigramCompressedCountPath = args[2];
		String unigramHuffmanTreePath = args[3];
		String outputPath = args[4];
		BigramComparison.M = CompressUnigramCount.load(unigramCompressedCountPath);
		BigramComparison.unigramHuffmanTree = HuffmanEncoding.loadHuffmanTree(unigramHuffmanTreePath);
		bigramHuffmanTree = HuffmanEncoding.loadHuffmanTree(bigramHuffmanTreePath);
		LoadBigramCounts(bigramRawCountPath);
		save(outputPath);
	}
}

package ecir.wordcount;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GenrateSampleData {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String[] unigrams = {"a", "b", "c", "d"};
		String[] bigrams = {"a b", "a c", "a d", "b c", "b d", "c d"};
		int days = 50;
		int size = 408;
		BufferedWriter bw = new BufferedWriter(new FileWriter("data/unigram_counts.txt"));
		bw.write(days*unigrams.length+","+size+"\n");
		for (String unigram : unigrams) {
			for (int day = 1; day <= days; day++) {
				bw.write(unigram+","+day+",");
				for (int i = 0; i < size; i++) {
					int randomNum = 10 + (int)(Math.random()*10);
					bw.write(randomNum+",");
				}
				bw.write("\n");
			}
		}
		bw.close();
		bw = new BufferedWriter(new FileWriter("data/bigram_counts.txt"));
		bw.write(days*bigrams.length+","+size+"\n");
		for (String bigram : bigrams) {
			for (int day = 1; day <= days; day++) {
				bw.write(bigram+","+day+",");
				for (int i = 0; i < size; i++) {
					int randomNum = 0 + (int)(Math.random()*10);
					bw.write(randomNum+",");
				}
				bw.write("\n");
			}
		}
		bw.close();
	}

}

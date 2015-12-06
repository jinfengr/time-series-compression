# ecir-compression
This repository is open-source implementation for our ECIR'16 paper: Compressing and Decoding Term Statistics Time Series [1].

Data Format
-----------
First, the input word count file should follow certain format: 

word1, day1, count, count, ..., count, 

word1, day2, count, count, ..., count, 

word2, day1, count, count, ..., count, 

The word count input file contains multiple words, and each word can have multiple days of counts. Each row represents the word counts for one word in one way. The number of word counts per day is unlimited. Consistent to our paper, we have two word count input files: one for unigram and the other for bigram. For example, check data/unigram_counts.txt and data/bigram_counts.txt respectively.

Getting Started
---------------
First, checkout our repo:
```
$ git clone git://github.com/Jeffyrao/ecir-compression.git
``` 
Then, build the package with Maven:
```
$ cd ecir-compression
$ mvn clean package appassembler:assemble
``` 

Compress Unigram Counts
-----------------------
1. To compress the unigram counts, first we need to build huffman tree over the unigram counts:
```
$ sh target/appassembler/bin/BuildUnigramHuffmanTree data/unigram_counts.txt unigram
``` 
The first input argument is the input unigram counts file, the second argument is a prefix for storing the built unigram huffman tree. After this command, the corresponding huffman frequency file will be stored in unigram-freq.txt, and the huffman code mapping file will be stored in unigram-huffmantree.txt

2. Then you can compare the performance of different compression algorithms(i.e, Variable Byte encoding, PForDelta, Simple16) on the unigram counts data. This step can be skiped if you don't want to see the comparison of different algorithms.
```
$ sh target/appassembler/bin/UnigramComparison data/unigram_counts.txt unigram-huffmantree.txt
``` 
The second input argument is the built huffman tree of unigram counts. This commmand should print some log showing the compression size(Bytes) and the average decoding time(us) for each compression algorithm. 

3. To actually compress the unigram counts and store it to disk, you need run:
```
sh target/appassembler/bin/CompressUnigramCount data/unigram_counts.txt unigram-huffmantree.txt unigram_compressed_counts.txt
```
The third argument is the output file to store the compressed unigram counts. This compressed unigram counts will be used for compressing and decoding bigram counts later.

Compress Bigram Counts
-----------------------
1. To compress the bigram counts, first we need to build huffman tree over the bigram counts:
```
$ sh target/appassembler/bin/BuildBigramHuffmanTree data/bigram_counts.txt unigram_compressed_counts.txt unigram-huffmantree.txt bigram
``` 
The first input argument is the input bigram counts file, the second and third argument are the huffman tree and compressed counts files generated in compressing unigram counts. The fourth argument is a prefix for storing the built bigram huffman tree. After this command, the corresponding huffman frequency file will be stored in bigram-freq.txt, and the huffman code mapping file will be stored in bigram-huffmantree.txt

2. Then you can compare the performance of different compression algorithms(i.e, Variable Byte encoding, PForDelta, Simple16) on the bigram counts data. Similarly, this step can be skiped if you don't care the performance comparison.
```
$ sh target/appassembler/bin/bigramComparison data/unigram_counts.txt bigram-huffmantree.txt unigram_compressed_counts.txt unigram-huffmantree.txt
``` 
Similarly, this commmand should print some log showing the compression size(Bytes) and the average decoding time(us) for each compression algorithm. 

3. To actually compress the bigram counts and store it to disk, you need run:
```
sh target/appassembler/bin/CompressBigramCount data/bigram_counts.txt bigram-huffmantree.txt unigram_compressed_counts.txt unigram-huffmantree.txt bigram_compressed_counts.txt
```
The third and fourth argument are the huffman tree and compressed counts files for unigram. The fifth argument is the output file to store the compressed bigram counts. 

package bio.pih.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RichSequence;

public class DbTextIndex {
	
	private static int nextSequenceId = 0;

	public static void main(String[] args) throws IOException, IllegalSymbolException, NoSuchElementException, ParseException {
		Directory indexDir = FSDirectory.open(new File("./index"));
		
		
		if (new File("./index").exists()) {
			IndexSearcher is = new IndexSearcher(indexDir);
			
			Query q = new TermQuery(new Term("header", "100"));
			TopDocs search = is.search(q, 20);
			
			System.out.println(search.totalHits);
			
			System.out.println(search.scoreDocs[0]);
			
		} else { final boolean forceFormatting = true;
		

		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31));		
		IndexWriter indexWriter = new IndexWriter(indexDir, indexWriterConfig);
		
		BufferedReader is = new BufferedReader(new FileReader("/Users/albrecht/genoogle/files/fasta/ecoli.nt"));
		RichSequenceStreamReader readFastaDNA = IOTools.readFasta(is, DNAAlphabet.SINGLETON);
		while (readFastaDNA.hasNext()) {
			RichSequence s;
			
			try {
				s = readFastaDNA.nextRichSequence();
			} catch (IllegalSymbolException e) {
				if (forceFormatting) {
					continue;
				} else {
					throw e;
				}
			}
					
			int id = getNextSequenceId();			
			String gi = s.getGi();
			String name = s.getName();
			String type = s.getType();
			String accession = s.getAccession();
			String description = s.getDescription();
			String header = s.getHeader();
			
			System.out.println(id);
			System.out.println(gi);
			System.out.println(name);
			System.out.println(type);
			System.out.println(accession);
			System.out.println(description);
			
			Document doc = new Document();
			doc.add(new Field("header", header, Store.YES, Index.ANALYZED));
			doc.add(new Field("gi", gi, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("name", name, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("type", type, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("accession", accession, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("description", description, Store.YES, Index.ANALYZED));
			doc.add(new Field("id", Integer.toString(id), Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("file", "ecoli.nt", Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("db", "ECOLI_DB", Store.YES, Index.NOT_ANALYZED));
			indexWriter.addDocument(doc);
		}
		indexWriter.optimize();
		indexWriter.close();
		}
		
		
	}
	
	protected static int getNextSequenceId() {
		int id = nextSequenceId;
		nextSequenceId++;
		return id;
	}
	
}

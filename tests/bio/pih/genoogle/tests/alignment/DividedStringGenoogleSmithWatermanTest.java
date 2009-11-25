package bio.pih.genoogle.tests.alignment;


import junit.framework.Assert;

import org.junit.Test;

import bio.pih.genoogle.alignment.DividedStringGenoogleSmithWaterman;

public class DividedStringGenoogleSmithWatermanTest {

	DividedStringGenoogleSmithWaterman d;
	DividedStringGenoogleSmithWaterman d1;
	DividedStringGenoogleSmithWaterman d2;
	DividedStringGenoogleSmithWaterman d3;
	DividedStringGenoogleSmithWaterman d4;
	DividedStringGenoogleSmithWaterman d5;
		
	@Test
	public void testSameSequencesAligneds() {
		d = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 20);
		String query = "ACTGGGCCCTAGTCA";
		d.pairwiseAlignment(query, query);
		System.out.println("Equals 20:");
		System.out.println(d.getQueryAligned());
		System.out.println(d.getPath());
		System.out.println(d.getTargetAligned());
		System.out.println();
		
		d1 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 15);
		d1.pairwiseAlignment(query, query);
		System.out.println("Equals 15:");
		System.out.println(d1.getQueryAligned());
		System.out.println(d1.getPath());
		System.out.println(d1.getTargetAligned());		
		System.out.println();
		
		Assert.assertEquals(d.getQueryAligned(), d1.getQueryAligned());
		Assert.assertEquals(d.getPath(), d1.getPath());
		Assert.assertEquals(d.getTargetAligned(), d1.getTargetAligned());
		Assert.assertEquals(d.getScore(), d1.getScore());
		Assert.assertEquals(d.getIdentitySize(), d1.getIdentitySize());
		
		d2 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 10);
		d2.pairwiseAlignment(query, query);
		System.out.println("Equals 10:");
		System.out.println(d2.getQueryAligned());
		System.out.println(d2.getPath());
		System.out.println(d2.getTargetAligned());
		System.out.println();
		
		Assert.assertEquals(d.getQueryAligned(), d2.getQueryAligned());
		Assert.assertEquals(d.getPath(), d2.getPath());
		Assert.assertEquals(d.getTargetAligned(), d2.getTargetAligned());
		Assert.assertEquals(d.getScore(), d2.getScore());
		Assert.assertEquals(d.getIdentitySize(), d2.getIdentitySize());
		
		d3 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 7);
		d3.pairwiseAlignment(query, query);
		System.out.println("Equals 7:");
		System.out.println(d3.getQueryAligned());
		System.out.println(d3.getPath());
		System.out.println(d3.getTargetAligned());
		System.out.println();
		
		Assert.assertEquals(d.getQueryAligned(), d3.getQueryAligned());
		Assert.assertEquals(d.getPath(), d3.getPath());
		Assert.assertEquals(d.getTargetAligned(), d3.getTargetAligned());
		Assert.assertEquals(d.getScore(), d3.getScore());
		Assert.assertEquals(d.getIdentitySize(), d3.getIdentitySize());
		
		d4 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 5);
		d4.pairwiseAlignment(query, query);
		System.out.println("Equals 5:");
		System.out.println(d4.getQueryAligned());
		System.out.println(d4.getPath());
		System.out.println(d4.getTargetAligned());
		System.out.println();
		
		Assert.assertEquals(d.getQueryAligned(), d4.getQueryAligned());
		Assert.assertEquals(d.getPath(), d4.getPath());
		Assert.assertEquals(d.getTargetAligned(), d4.getTargetAligned());
		Assert.assertEquals(d.getScore(), d4.getScore());
		Assert.assertEquals(d.getIdentitySize(), d4.getIdentitySize());
		
		d5 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 3);
		d5.pairwiseAlignment(query, query);
		System.out.println("Equals 3:");
		System.out.println(d5.getQueryAligned());
		System.out.println(d5.getPath());
		System.out.println(d5.getTargetAligned());
		System.out.println();
		
		Assert.assertEquals(d.getQueryAligned(), d5.getQueryAligned());
		Assert.assertEquals(d.getPath(), d5.getPath());
		Assert.assertEquals(d.getTargetAligned(), d5.getTargetAligned());
		Assert.assertEquals(d.getScore(), d5.getScore());
		Assert.assertEquals(d.getIdentitySize(), d5.getIdentitySize());
				
		//--------------------------
		
		query = "ACTGGGCCCTAGTCA";
		String target = "CTGGGCCCTAGTCA";
		d = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 20);
				
		d.pairwiseAlignment(query, target);
		System.out.println("Gap first position  20:");
		System.out.println(d.getQueryAligned());
		System.out.println(d.getPath());
		System.out.println(d.getTargetAligned());
		
		d1 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 15);
		d1.pairwiseAlignment(query, target);
		System.out.println("Gap first position  15:");
		System.out.println(d1.getQueryAligned());
		System.out.println(d1.getPath());
		System.out.println(d1.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d1.getQueryAligned());
		Assert.assertEquals(d.getPath(), d1.getPath());
		Assert.assertEquals(d.getTargetAligned(), d1.getTargetAligned());
		Assert.assertEquals(d.getScore(), d1.getScore());
		Assert.assertEquals(d.getIdentitySize(), d1.getIdentitySize());
		
		d2 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 10);
		d2.pairwiseAlignment(query, target);
		System.out.println("Gap first position  10:");
		System.out.println(d2.getQueryAligned());
		System.out.println(d2.getPath());
		System.out.println(d2.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d2.getQueryAligned());
		Assert.assertEquals(d.getPath(), d2.getPath());
		Assert.assertEquals(d.getTargetAligned(), d2.getTargetAligned());
		Assert.assertEquals(d.getScore(), d2.getScore());
		Assert.assertEquals(d.getIdentitySize(), d2.getIdentitySize());
		
		d3 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 7);
		d3.pairwiseAlignment(query, target);
		System.out.println("Gap first position  7:");
		System.out.println(d3.getQueryAligned());
		System.out.println(d3.getPath());
		System.out.println(d3.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d3.getQueryAligned());
		Assert.assertEquals(d.getPath(), d3.getPath());
		Assert.assertEquals(d.getTargetAligned(), d3.getTargetAligned());
		Assert.assertEquals(d.getScore(), d3.getScore());
		Assert.assertEquals(d.getIdentitySize(), d3.getIdentitySize());
		
		d4 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 5);
		d4.pairwiseAlignment(query, target);
		System.out.println("Gap first position  5:");
		System.out.println(d4.getQueryAligned());
		System.out.println(d4.getPath());
		System.out.println(d4.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d4.getQueryAligned());
		Assert.assertEquals(d.getPath(), d4.getPath());
		Assert.assertEquals(d.getTargetAligned(), d4.getTargetAligned());
		Assert.assertEquals(d.getScore(), d4.getScore());
		Assert.assertEquals(d.getIdentitySize(), d4.getIdentitySize());
		
		d5 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 3);
		d5.pairwiseAlignment(query, target);
		System.out.println("Gap first position  3:");
		System.out.println(d5.getQueryAligned());
		System.out.println(d5.getPath());
		System.out.println(d5.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d5.getQueryAligned());
		Assert.assertEquals(d.getPath(), d5.getPath());
		Assert.assertEquals(d.getTargetAligned(), d5.getTargetAligned());
		Assert.assertEquals(d.getScore(), d5.getScore());
		Assert.assertEquals(d.getIdentitySize(), d5.getIdentitySize());
		
		//--------------------------
		
		query =  "ACTGGGCCCTAGTCA";
		target = "CATGGGCCCTAGTAC";
		d = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 20);
				
		d.pairwiseAlignment(query, target);
		System.out.println("Gap first position  20:");
		System.out.println(d.getQueryAligned());
		System.out.println(d.getPath());
		System.out.println(d.getTargetAligned());
		
		d1 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 15);
		d1.pairwiseAlignment(query, target);
		System.out.println("Gap first position  15:");
		System.out.println(d1.getQueryAligned());
		System.out.println(d1.getPath());
		System.out.println(d1.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d1.getQueryAligned());
		Assert.assertEquals(d.getPath(), d1.getPath());
		Assert.assertEquals(d.getTargetAligned(), d1.getTargetAligned());
		Assert.assertEquals(d.getScore(), d1.getScore());
		Assert.assertEquals(d.getIdentitySize(), d1.getIdentitySize());
		
		d2 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 10);
		d2.pairwiseAlignment(query, target);
		System.out.println("Gap first position  10:");
		System.out.println(d2.getQueryAligned());
		System.out.println(d2.getPath());
		System.out.println(d2.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d2.getQueryAligned());
		Assert.assertEquals(d.getPath(), d2.getPath());
		Assert.assertEquals(d.getTargetAligned(), d2.getTargetAligned());
		Assert.assertEquals(d.getScore(), d2.getScore());
		Assert.assertEquals(d.getIdentitySize(), d2.getIdentitySize());
		
		d3 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 7);
		d3.pairwiseAlignment(query, target);
		System.out.println("Gap first position  7:");
		System.out.println(d3.getQueryAligned());
		System.out.println(d3.getPath());
		System.out.println(d3.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d3.getQueryAligned());
		Assert.assertEquals(d.getPath(), d3.getPath());
		Assert.assertEquals(d.getTargetAligned(), d3.getTargetAligned());
		Assert.assertEquals(d.getScore(), d3.getScore());
		Assert.assertEquals(d.getIdentitySize(), d3.getIdentitySize());
		
		d4 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 5);
		d4.pairwiseAlignment(query, target);
		System.out.println("Gap first position  5:");
		System.out.println(d4.getQueryAligned());
		System.out.println(d4.getPath());
		System.out.println(d4.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d4.getQueryAligned());
		Assert.assertEquals(d.getPath(), d4.getPath());
		Assert.assertEquals(d.getTargetAligned(), d4.getTargetAligned());
		Assert.assertEquals(d.getScore(), d4.getScore());
		Assert.assertEquals(d.getIdentitySize(), d4.getIdentitySize());
		
		d5 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 3);
		d5.pairwiseAlignment(query, target);
		System.out.println("Gap first position  3:");
		System.out.println(d5.getQueryAligned());
		System.out.println(d5.getPath());
		System.out.println(d5.getTargetAligned());	
		
		Assert.assertEquals("ACTGGGCCCTAGT", d5.getQueryAligned());
		Assert.assertEquals("| |||||||||||", d5.getPath());
		Assert.assertEquals("A-TGGGCCCTAGT", d5.getTargetAligned());
		Assert.assertEquals(d.getScore(), d5.getScore());
		Assert.assertEquals(12, d5.getIdentitySize());
		
		//--------------------------
		
		query =  "ACTGGGCTAGTCA";
		target = "ACTGGGCCCTAGTCA";
		d = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 20);
				
		d.pairwiseAlignment(query, target);
		System.out.println("Midle  20:");
		System.out.println(d.getQueryAligned());
		System.out.println(d.getPath());
		System.out.println(d.getTargetAligned());
		
		d1 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 15);
		d1.pairwiseAlignment(query, target);
		System.out.println("Midle  15:");
		System.out.println(d1.getQueryAligned());
		System.out.println(d1.getPath());
		System.out.println(d1.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d1.getQueryAligned());
		Assert.assertEquals(d.getPath(), d1.getPath());
		Assert.assertEquals(d.getTargetAligned(), d1.getTargetAligned());
		Assert.assertEquals(d.getScore(), d1.getScore());
		Assert.assertEquals(d.getIdentitySize(), d1.getIdentitySize());
		
		d2 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 10);
		d2.pairwiseAlignment(query, target);
		System.out.println("Midle  10:");
		System.out.println(d2.getQueryAligned());
		System.out.println(d2.getPath());
		System.out.println(d2.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d2.getQueryAligned());
		Assert.assertEquals(d.getPath(), d2.getPath());
		Assert.assertEquals(d.getTargetAligned(), d2.getTargetAligned());
		Assert.assertEquals(d.getScore(), d2.getScore());
		Assert.assertEquals(d.getIdentitySize(), d2.getIdentitySize());
		
		d3 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 7);
		d3.pairwiseAlignment(query, target);
		System.out.println("Midle  7:");
		System.out.println(d3.getQueryAligned());
		System.out.println(d3.getPath());
		System.out.println(d3.getTargetAligned());
		
		Assert.assertEquals(d.getQueryAligned(), d3.getQueryAligned());
		Assert.assertEquals(d.getPath(), d3.getPath());
		Assert.assertEquals(d.getTargetAligned(), d3.getTargetAligned());
		Assert.assertEquals(d.getScore(), d3.getScore());
		Assert.assertEquals(d.getIdentitySize(), d3.getIdentitySize());
		
		d4 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 5);
		d4.pairwiseAlignment(query, target);
		System.out.println("Midle  5:");
		System.out.println(d4.getQueryAligned());
		System.out.println(d4.getPath());
		System.out.println(d4.getTargetAligned());
		
		Assert.assertEquals("ACTGGGC--TAGTCA", d4.getQueryAligned());
		Assert.assertEquals("|||||||  ||||||", d4.getPath());
		Assert.assertEquals("ACTGGGCCCTAGTCA", d4.getTargetAligned());
		Assert.assertEquals(d.getScore(), d4.getScore());
		Assert.assertEquals(d.getIdentitySize(), d4.getIdentitySize());
		
		d5 = new DividedStringGenoogleSmithWaterman(1, -1, -1, -1, -1, 3);
		d5.pairwiseAlignment(query, target);
		System.out.println("Midle  3:");
		System.out.println(d5.getQueryAligned());
		System.out.println(d5.getPath());
		System.out.println(d5.getTargetAligned());
		
		Assert.assertEquals("ACTGGG--CTAGTCA", d5.getQueryAligned());
		Assert.assertEquals("||||||  |||||||", d5.getPath());
		Assert.assertEquals("ACTGGGCCCTAGTCA", d5.getTargetAligned());	
		Assert.assertEquals(d.getScore(), d5.getScore());
		Assert.assertEquals(d.getIdentitySize(), d5.getIdentitySize());
	}	
}
package de.tuda.sdm.dmdb.access.exercise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.tuda.sdm.dmdb.access.AbstractBitmapIndex;
import de.tuda.sdm.dmdb.access.AbstractTable;
import de.tuda.sdm.dmdb.storage.AbstractRecord;
import de.tuda.sdm.dmdb.storage.types.AbstractSQLValue;
import de.tuda.sdm.dmdb.storage.types.exercise.SQLInteger;

/**
 * Bitmap index that uses the vanilla/naive bitmap approach (one bitmap for each
 * distinct value)
 * 
 * @author melhindi
 *
 ** @param <T>
 *            Type of the key index by the index. While all abstractSQLValues
 *            subclasses can be used, the implementation currently only support
 *            for SQLInteger type is guaranteed.
 */
public class NaiveBitmapIndex<T extends AbstractSQLValue> extends AbstractBitmapIndex<T> {

	/*
	 * Constructor of NaiveBitmapIndex
	 * 
	 * @param table Table for which the bitmap index will be build
	 * 
	 * @param keyColumnNumbner: index of the column within the passed table that
	 * should be indexed
	 */
	public NaiveBitmapIndex(AbstractTable table, int keyColumnNumber) {
		super(table, keyColumnNumber);
		this.bitMaps = new HashMap<T, BitSet>();
		this.bitmapSize = this.getTable().getRecordCount();
		this.bulkLoadIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void bulkLoadIndex() {
		Iterator<AbstractRecord> it = this.getTable().iterator();
		while (it.hasNext()) {
			T key = (T) it.next().getValue(keyColumnNumber);
			bitMaps.put(key, new BitSet(bitmapSize));
		}
		it = this.getTable().iterator();
		int i = 1; // bitset indexed from 1
		while (it.hasNext()) {
			bitMaps.get(it.next().getValue(keyColumnNumber)).set(i);
			i++;
		}
	}
	
//	void printbitset(BitSet b) {
//		for (int i = 1; i <= b.size(); i++) {
//			if (b.get(i) == true) {
//				System.out.print(i + " ");
//			}
//		}
//		System.out.println();
//	}

	@Override
	public List<AbstractRecord> rangeLookup(T startKey, T endKey) {
		BitSet bitset = new BitSet(bitmapSize); // no bits are set
		
		List<AbstractRecord> result = new ArrayList<AbstractRecord>();
		for (HashMap.Entry<T, BitSet> entry : bitMaps.entrySet()) {
			T key = entry.getKey();
			if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
				bitset.or(entry.getValue());	// OR 
			}
		}

		for (int i = 1; i <= bitmapSize; i++) {
			if (bitset.get(i)==true) {
				AbstractRecord ar = this.getTable().getRecordFromRowId(i-1);
				result.add(ar);
			}
		}
		return result;
	}

}

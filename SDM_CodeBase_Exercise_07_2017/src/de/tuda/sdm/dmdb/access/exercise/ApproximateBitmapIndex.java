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

/**
 * Bitmap that uses the approximate bitmap index (compressed) approach
 * 
 * @author melhindi
 *
 * @param <T>
 *            Type of the key index by the index. While all abstractSQLValues
 *            subclasses can be used, the implementation currently only support
 *            for SQLInteger type is guaranteed.
 */
public class ApproximateBitmapIndex<T extends AbstractSQLValue> extends AbstractBitmapIndex<T> {

	/*
	 * Constructor of ApproximateBitmapIndex This implementation uses modulo as hash
	 * function and only supports SQLInteger as data type
	 * 
	 * @param table Table for which the bitmap index will be build
	 * 
	 * @param keyColumnNumbner: index of the column within the passed table that
	 * should be indexed
	 * 
	 * @param bitmapSize Size of for each bitmap, i.e., use (% bitmapSize) as
	 * hashfunction
	 */
	public ApproximateBitmapIndex(AbstractTable table, int keyColumnNumber, int bitmapSize) {
		super(table, keyColumnNumber);
		this.bitMaps = new HashMap<T, BitSet>();
		this.bitmapSize = bitmapSize;
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
		int i = 0; // bitset indexed from 1
		while (it.hasNext()) {
			bitMaps.get(it.next().getValue(keyColumnNumber)).set((i % bitmapSize) + 1);
			i++;
		}
	}

	@Override
	public List<AbstractRecord> rangeLookup(T startKey, T endKey) {
		BitSet bitset = new BitSet(bitmapSize); // no bits are set

		for (HashMap.Entry<T, BitSet> entry : bitMaps.entrySet()) {
			T key = entry.getKey();
			if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
				bitset.or(entry.getValue()); // OR
			}
		}
		
		HashSet<Integer> setBitIndexes = new HashSet<Integer>(); // set containing all set bits
		for (int i = 1; i <= bitmapSize; i++) {
			if (bitset.get(i) == true) {
				setBitIndexes.add(i-1);
			}
		}
		
		int tableSize = this.getTable().getRecordCount();
		List<AbstractRecord> result = new ArrayList<AbstractRecord>();
		for (int j = 0; j < tableSize; ++j) {
			if (setBitIndexes.contains(j % bitmapSize)) {
				AbstractRecord ar = this.getTable().getRecordFromRowId(j);
				T key = (T) ar.getValue(keyColumnNumber);
				if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) { // avoid false positives
					result.add(ar);
				}
			}
		}
		return result;
	}

}

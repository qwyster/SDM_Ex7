package de.tuda.sdm.dmdb.access.exercise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

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
		int i = 0;
		while (it.hasNext()) {
			bitMaps.get(it.next().getValue(keyColumnNumber)).set(i % bitmapSize);
			i++;
		}
	}
	
	class AbstractRecordComparator implements Comparator<AbstractRecord> {
		@Override
		public int compare(AbstractRecord arg0, AbstractRecord arg1) {
			return arg0.getValue(keyColumnNumber).compareTo(arg1.getValue(keyColumnNumber));
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
		for (int i = 0; i < bitmapSize; i++) {
			if (bitset.get(i) == true) {
				setBitIndexes.add(i);
			}
		}

		int tableSize = this.getTable().getRecordCount();
		List<AbstractRecord> result = new ArrayList<AbstractRecord>();
		for (Integer i : setBitIndexes) {
			for (int j = 0; j < Math.floor(tableSize / bitmapSize); ++j) {
				AbstractRecord ar = this.getTable().getRecordFromRowId(i + j * bitmapSize);
				T key = (T) ar.getValue(keyColumnNumber);
				if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) { // avoid false positives
					result.add(ar);
				}
			}
		}
		Collections.sort(result, new AbstractRecordComparator()); // sort by rowId
		return result;
	}

}

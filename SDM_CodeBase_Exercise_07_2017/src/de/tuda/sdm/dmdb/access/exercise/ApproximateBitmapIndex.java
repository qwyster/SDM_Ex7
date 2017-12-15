package de.tuda.sdm.dmdb.access.exercise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.tuda.sdm.dmdb.access.AbstractBitmapIndex;
import de.tuda.sdm.dmdb.access.AbstractTable;
import de.tuda.sdm.dmdb.storage.AbstractRecord;
import de.tuda.sdm.dmdb.storage.types.AbstractSQLValue;

/**
 * Bitmap that uses the approximate bitmap index (compressed) approach
 * @author melhindi
 *
 * @param <T> Type of the key index by the index. While all abstractSQLValues subclasses can be used,
 * the implementation currently only support for SQLInteger type is guaranteed.
 */
public class ApproximateBitmapIndex<T extends AbstractSQLValue> extends AbstractBitmapIndex<T> {

	/*
	 * Constructor of ApproximateBitmapIndex
	 * This implementation uses modulo as hash function and only supports SQLInteger as data type
	 * @param table Table for which the bitmap index will be build
	 * @param keyColumnNumbner: index of the column within the passed table that should be indexed
	 * @param bitmapSize Size of for each bitmap, i.e., use (% bitmapSize) as hashfunction
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
		while(it.hasNext()){
			bitMaps.put((T) it.next().getValue(keyColumnNumber), new BitSet(bitmapSize));
			//it = (Iterator<AbstractRecord>) it.next();
		}
		it = this.getTable().iterator();
		int i = 0;
		while(it.hasNext()){
			bitMaps.get(it.next().getValue(keyColumnNumber)).set(i % bitmapSize);
			i++;
			//it = (Iterator<AbstractRecord>) it.next();
		}
	}

	@Override
	public List<AbstractRecord> rangeLookup(T startKey, T endKey) {
		Iterator<AbstractRecord> it = this.getTable().iterator();
		List<AbstractRecord> result = new ArrayList<AbstractRecord>();
		
		while(it.hasNext()){
			AbstractRecord ar= it.next();
			if(ar.getValue(keyColumnNumber).compareTo(startKey)>=0 && ar.getValue(keyColumnNumber).compareTo(endKey)<=0){
				result.add(ar);
			}
		}
		return result;
	}

}

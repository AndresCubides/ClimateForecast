package utils;

import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListMultidataSetIterator<T extends MultiDataSet> implements MultiDataSetIterator {

    private static final long serialVersionUID = -7569201667767185411L;
    private int curr = 0;
    private int batch = 10;
    private List<T> list;
    private MultiDataSetPreProcessor preProcessor;

    public ListMultidataSetIterator(Collection<T> coll, int batch) {
        list = new ArrayList<>(coll);
        this.batch = batch;
    }

    /**
     * Initializes with a batch of 5
     *
     * @param coll the collection to iterate over
     */
    public ListMultidataSetIterator(Collection<T> coll) {
        this(coll, 5);
    }

    @Override
    public synchronized boolean hasNext() {
        return curr < list.size();
    }

    @Override
    public synchronized MultiDataSet next() {
        return next(batch);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int totalExamples() {
        return list.size();
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        //Already in memory -> doesn't make sense to prefetch
        return false;
    }

    @Override
    public synchronized void reset() {
        curr = 0;
    }

    public int batch() {
        return batch;
    }

    public synchronized int cursor() {
        return curr;
    }

    public int numExamples() {
        return list.size();
    }

    @Override
    public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
        this.preProcessor = (MultiDataSetPreProcessor) preProcessor;
    }

    @Override
    public MultiDataSetPreProcessor getPreProcessor() {
        return preProcessor;
    }

    public List<String> getLabels() {
        return null;
    }


    @Override
    public MultiDataSet next(int num) {
        int end = curr + num;

        List<MultiDataSet> r = new ArrayList<>();
        if (end >= list.size())
            end = list.size();
        for (; curr < end; curr++) {
            r.add(list.get(curr));
        }

        MultiDataSet d = MultiDataSet.merge(r);

        return d;
    }


}

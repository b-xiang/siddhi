package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

public class NotCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor notCollectionExecutor;
    private final ExhaustiveCollectionExecutor exhaustiveCollectionExecutor;

    public NotCollectionExecutor(CollectionExecutor notCollectionExecutor, ExhaustiveCollectionExecutor exhaustiveCollectionExecutor) {

        this.notCollectionExecutor = notCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        } else if (notStreamEvents.size() == 0) {
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            Set<StreamEvent> candidateEventSet = indexedEventHolder.getAllEventSet();

            for (StreamEvent candidateEvent : candidateEventSet) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEvent));
                } else {
                    returnEventChunk.add(candidateEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            Set<StreamEvent> returnSet = indexedEventHolder.getAllEventSet();
            for (StreamEvent aStreamEvent : notStreamEvents) {
                returnSet.remove(aStreamEvent);
            }
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            for (StreamEvent resultEvent : returnSet) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(resultEvent));
                } else {
                    returnEventChunk.add(resultEvent);
                }
            }
            return returnEventChunk.getFirst();
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return null;
        } else if (notStreamEvents.size() == 0) {
            return indexedEventHolder.getAllEventSet();
        } else {
            Set<StreamEvent> returnSet = indexedEventHolder.getAllEventSet();
            for (StreamEvent aStreamEvent : notStreamEvents) {
                returnSet.remove(aStreamEvent);
            }
            return returnSet;
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        } else {
            return notStreamEvents.size() == 0;
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(deletingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        } else if (notStreamEvents.size() != 0) {
            Set<StreamEvent> returnSet = indexedEventHolder.getAllEventSet();
            if (returnSet.size() == notStreamEvents.size()) {
                indexedEventHolder.deleteAll();
            } else {
                for (StreamEvent aStreamEvent : notStreamEvents) {
                    returnSet.remove(aStreamEvent);
                }
                indexedEventHolder.deleteAll(returnSet);
            }
        }
    }

}

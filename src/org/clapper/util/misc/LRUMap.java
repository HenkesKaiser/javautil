/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.util.misc;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.Serializable;

/**
 * <p>An <tt>LRUMap</tt> implements a <tt>Map</tt> of a fixed maximum size
 * that enforces a least recently used discard policy. When the
 * <tt>LRUMap</tt> is full (i.e., contains the maximum number of entries),
 * any attempt to insert a new entry causes one of the least recently used
 * entries to be discarded.</p>
 *
 * <p>Note:</p>
 *
 * <ul>
 *   <li>The <tt>get()</tt> method "touches" (or "refreshes") an object,
 *       making it "new" again, even if it simply replaces the value for
 *       an existing key.
 *   <li>The <tt>get()</tt> method also refreshes the retrieved object,
 *       but the <tt>iterator()</tt>, <tt>containsValue()</tt> and
 *       <tt>containsKey()</tt> methods do not refresh the objects in the
 *       cache.
 *   <li>This implementation is <b>not</b> synchronized. If multiple threads
 *       access this map concurrently, and at least one of the threads
 *       modifies the map structurally, it must be synchronized externally.
 *       Unlike other <tt>Map</tt> implementations, even a simple
 *       <tt>get()</tt> operation structurally modifies an <tt>LRUMap</tt>.
 *       Synchronization can be accomplished by synchronizing on some
 *       object that naturally encapsulates the map. If no such object
 *       exists, the map should be "wrapped" using the
 *       <tt>Collections.synchronizedMap()</tt> method. This is best done
 *       at creation time, to prevent accidental unsynchronized access to
 *       the map:
 *       <pre>Map m = Collections.synchronizedMap (new LRUMap (...));</pre>
 * </ul>
 *
 * <p>There are other, similar implementations. For instance, see the
 * {@link <a href="http://jakarta.apache.org/commons/collections/apidocs/org/apache/commons/collections/LRUMap.html">LRUMap</a>}
 * class in the 
 * {@link <a href="http://jakarta.apache.org/commons/collections/">Apache Jakarta Commons Collection</a>}
 * API. (This leads to the obvious question: Why write another one? The primary
 * answer is that I did not want to add another third-party library dependency.
 * Plus, I wanted to experiment with this algorithm.)</p>
 * 
 * @version <tt>$Revision$</tt>
 *
 * @author Copyright &copy; 2004 Brian M. Clapper
 */
public class LRUMap extends AbstractMap implements Cloneable, Serializable
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The default load factor, if one isn't specified to the constructor
     */
    public static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /**
     * The default initial capacity, if one isn't specified to the
     * constructor
     */
    public static final int   DEFAULT_INITIAL_CAPACITY = 16;

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Set of Map.Entry (really, LRULinkedListEntry) objects returned by
     * the LRUMap.entrySet() method.
     */
    private class EntrySet extends AbstractSet
    {
        public Iterator iterator()
        {
            return new EntryIterator();
        }

        public boolean contains (Object o)
        {
            boolean has = false;

            if (o instanceof Map.Entry)
            {
                Map.Entry e = (Map.Entry) o;
                Object key = e.getKey();

                has = LRUMap.this.containsKey (key);
            }

            return has;
        }

        public boolean remove (Object o)
        {
            return (LRUMap.this.remove (o) != null);
        }

        public int size()
        {
            return LRUMap.this.size();
        }

        public void clear()
        {
            LRUMap.this.clear();
        }
    }

    /**
     * Iterator returned by EntrySet.iterator()
     */
    private class EntryIterator implements Iterator
    {
        private LRULinkedListEntry current;

        EntryIterator()
        {
            current = lruQueue.head;
        }

        public Object next()
        {
            LRULinkedListEntry result = current;
            current = current.next;
            return result;
        }

        public boolean hasNext()
        {
            return (current != null);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Set of key objects returned by the LRUMap.keySet() method.
     */
    private class KeySet extends AbstractSet
    {
        public Iterator iterator()
        {
            return new KeySetIterator();
        }

        public boolean contains (Object o)
        {
            return LRUMap.this.containsKey (o);
        }

        public boolean remove (Object o)
        {
            return (LRUMap.this.remove (o) != null);
        }

        public int size()
        {
            return LRUMap.this.size();
        }

        public void clear()
        {
            LRUMap.this.clear();
        }
    }

    /**
     * Iterator returned by KeySet.iterator()
     */
    private class KeySetIterator implements Iterator
    {
        private LRULinkedListEntry current;

        KeySetIterator()
        {
            current = lruQueue.head;
        }

        public Object next()
        {
            LRULinkedListEntry result = current;
            current = current.next;
            return result.key;
        }

        public boolean hasNext()
        {
            return (current != null);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Entry in the internal linked list (queue) of LRU entries. Implements
     * Map.Entry for convenience.
     */
    private class LRULinkedListEntry implements Map.Entry
    {
        LRULinkedListEntry  previous = null;
        LRULinkedListEntry  next     = null;
        Object              key      = null;
        Object              value    = null;

        LRULinkedListEntry (Object key, Object value)
        {
            setKeyValue (key, value);
        }

        public boolean equals (Object o)
        {
            return (o instanceof LRULinkedListEntry);
        }

        public int hashCode()
        {
            return key.hashCode();
        }

        public Object getKey()
        {
            return key;
        }

        public Object getValue()
        {
            return value;
        }

        void setKeyValue (Object key, Object value)
        {
            this.key   = key;
            this.value = value;
        }

        public Object setValue (Object value)
        {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    /**
     * Internal linked list that implements the LRU queue. Each entry in the
     * hash map of objects also points to one of these, allowing the hash map
     * to remain untouched even as the linked list entries get reordered.
     */
    private class LRULinkedList
    {
        LRULinkedListEntry  head = null;
        LRULinkedListEntry  tail = null;
        int                 size = 0;

        LRULinkedList()
        {
        }

        protected void finalize()
        {
            clear();
        }

        void addToTail (LRULinkedListEntry entry)
        {
            entry.next = null;
            entry.previous = tail;

            if (head == null)
            {
                head = entry;
                tail = entry;
            }

            else
            {
                entry.previous = tail;
                tail.next = entry;
            }

            size++;
        }

        void addToHead (LRULinkedListEntry entry)
        {
            entry.next = null;
            entry.previous = null;

            if (head == null)
            {
                assert (tail == null);
                head = entry;
                tail = entry;
            }

            else
            {
                entry.next = head;
                head.previous = entry;
                head = entry;
            }

            size++;
        }

        void remove (LRULinkedListEntry entry)
        {
            if (entry.next != null)
                entry.next.previous = entry.previous;

            if (entry.previous != null)
                entry.previous.next = entry.next;

            if (entry == head)
                head = entry.next;

            if (entry == tail)
                tail = entry.previous;

            entry.next = null;
            entry.previous = null;

            size--;
            assert (size >= 0);
        }

        LRULinkedListEntry removeTail()
        {
            LRULinkedListEntry result = tail;

            if (result != null)
                remove (result);

            return result;
                
        }

        void moveToHead (LRULinkedListEntry entry)
        {
            remove (entry);
            addToHead (entry);
        }

        void clear()
        {
            while (head != null)
            {
                LRULinkedListEntry next = head.next;

                head.next = null;
                head.previous = null;
                head.key = null;
                head.value = null;

                head = next;
            }

            tail = null;
            size = 0;
        }
    }

    /**
     * Wraps any ObjectRemovalListener passed into addRemovalListener().
     * Keeps track of both the listener and its "automaticOnly" status
     */
    private class RemovalListenerWrapper implements ObjectRemovalListener
    {
        boolean                automaticOnly;
        ObjectRemovalListener  realListener;

        RemovalListenerWrapper (ObjectRemovalListener realListener,
                                boolean               automaticOnly)
        {
            this.realListener  = realListener;
            this.automaticOnly = automaticOnly;
        }

        public void objectRemoved (ObjectRemovalEvent event)
        {
            realListener.objectRemoved (event);
        }
    }

    /*----------------------------------------------------------------------*\
                             Private Variables
    \*----------------------------------------------------------------------*/

    private int            maxCapacity;
    private float          loadFactor;
    private int            initialCapacity;
    private Map            hash;
    private LRULinkedList  lruQueue;
    private Map            removalListeners = null;

    /*----------------------------------------------------------------------*\
                                Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new empty map with a default capacity and load factor,
     * and the specified maximum capacity.
     *
     * @param maxCapacity the maximum number of entries permitted in the
     *                    map. Must not be negative.
     */
    public LRUMap (int maxCapacity)
    {
	this (DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, maxCapacity);
    }

    /**
     * Construct a new empty map with the specified initial capacity,
     * a default load factor, and the specified maximum capacity.
     *
     * @param initialCapacity  the initial capacity
     * @param maxCapacity      the maximum number of entries permitted in the
     *                         map. Must not be negative.
     */
    public LRUMap (int initialCapacity, int maxCapacity)
    {
	this (initialCapacity, DEFAULT_LOAD_FACTOR, maxCapacity);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity,
     * load factor, and maximum capacity.
     *
     * @param initialCapacity  the initial capacity
     * @param loadFactor       the load factor
     * @param maxCapacity      the maximum number of entries permitted in the
     *                         map. Must not be negative.
     */
    public LRUMap (int initialCapacity, float loadFactor, int maxCapacity)
    {
        assert (maxCapacity > 0);
        assert (loadFactor > 0.0);
        assert (initialCapacity > 0);

        this.maxCapacity     = maxCapacity;
        this.loadFactor      = loadFactor;
        this.initialCapacity = initialCapacity;
        this.hash     = new HashMap (initialCapacity, loadFactor);
        this.lruQueue    = new LRULinkedList();
    }

    /**
     * Constructs a new map with the same mappings and parameters as the
     * given <tt>LRUMap</tt>. The initial capacity and load factor is
     * the same as for the parent <tt>HashMap</tt> class. The insertion
     * order of the keys is preserved.
     *
     * @param map  the map whose mappings are to be copied
     */
    public LRUMap (LRUMap map)
    {
        this (map.initialCapacity, map.loadFactor, map.maxCapacity);
        putAll (map);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Add an <tt>EventListener</tt> that will be called whenever an
     * object is removed from the cache. If <tt>automaticOnly</tt> is
     * <tt>true</tt>, then the listener is only notified for objects that
     * are removed automatically when the cache needs to be cleared to make
     * room for new objects. If <tt>automaticOnly</tt> is <tt>false</tt>,
     * then the listener is notified whenever an object is removed for any
     * reason, include a call to the {@link #remove remove()} method.</p>
     *
     * <p>Note that when this map announces the removal of an object, it
     * passes an {@link ObjectRemovalEvent} that contains a
     * <tt>java.util.Map.Entry</tt> object that wraps the actual object
     * that was removed. That way, both the key and the value are available
     * for the removed object.</p>
     *
     * @param listener      the listener to add
     * @param automaticOnly see above
     *
     * @see #removeRemovalListener
     */
    public synchronized void
    addRemovalListener (ObjectRemovalListener listener, boolean automaticOnly)
    {
        if (removalListeners == null)
            removalListeners = new HashMap();

        removalListeners.put (listener,
                              new RemovalListenerWrapper (listener,
                                                          automaticOnly));
    }

    /**
     * Remove an <tt>EventListener</tt> from the set of listeners to be invoked
     * when an object is removed from the cache.
     *
     * @param listener the listener to add
     *
     * @return <tt>true</tt> if the listener was in the list and was removed,
     *         <tt>false</tt> otherwise
     *
     * @see #addRemovalListener
     */
    public synchronized boolean
    removeRemovalListener (ObjectRemovalListener listener)
    {
        boolean removed = false;

        if (removalListeners != null)
        {
            if (removalListeners.remove (listener) != null)
                removed = true;
        }

        return removed;
    }
     
    /**
     * Remove all mappings from this map.
     */
    public void clear()
    {
        hash.clear();
        lruQueue.clear();
    }

    /**
     * Determine whether this map contains a mapping for a given key. Note
     * that this implementation of <tt>containsKey()</tt> does not refresh
     * the object in the cache.
     *
     * @param key  the key to find
     *
     * @return <tt>true</tt> if the key is in the map, <tt>false</tt> if not
     */
    public boolean containsKey (Object key)
    {
        return hash.containsKey (key);
    }

    /**
     * Determine whether this map contains a given value. Note that this
     * implementation of <tt>containsValue()</tt> does not refresh the
     * objects in the cache.
     *
     * @param value the value to find
     *
     * @return <tt>true</tt> if the value is in the map, <tt>false</tt> if not
     */
    public boolean containsValue (Object value)
    {
        return hash.containsValue (value);
    }

    /**
     * Get a set view of the mappings in this map. Each element in this set
     * is a <tt>Map.Entry</tt>. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa.
     * The collection supports element removal, which removes the
     * corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return nothing
     *
     * @throws UnsupportedOperationException unconditionally
     */
    public Set entrySet()
    {
        return new EntrySet();
    }
     
    /**
     * Retrieve an object from the map. Retrieving an object from an
     * LRU map "refreshes" the object so that it is among the most recently
     * used objects.
     *
     * @param key  the object's key in the map.
     *
     * @return the associated object, or null if not found
     */
    public Object get (Object key)
    {
        Object              value = null;
        LRULinkedListEntry  entry  = (LRULinkedListEntry) hash.get (key);

        if (entry != null)
        {
            // It's there. It's just been accessed, so move it to the
            // top of the linked list.

            assert (entry.key.equals (key)) :
                   "entry.key=" + entry.key + ", key=" + key;

            lruQueue.moveToHead (entry);
            value = entry.value;
        }

        return value;
    }

    /**
     * Get the initial capacity of this <tt>LRUMap</tt>.
     *
     * @return the initial capacity, as passed to the constructor
     *
     * @see #getLoadFactor
     * @see #getMaximumCapacity
     */
    public int getInitialCapacity()
    {
        return initialCapacity;
    }

    /**
     * Get the load factor for this <tt>LRUMap</tt>.
     *
     * @return the load factor, as passed to the constructor
     *
     * @see #getInitialCapacity
     * @see #getMaximumCapacity
     */
    public float getLoadFactor()
    {
        return loadFactor;
    }

    /**
     * Get the maximum capacity of this <tt>LRUMap</tt>.
     *
     * @return the maximum capacity, as passed to the constructor
     *
     * @see #setMaximumCapacity
     * @see #getLoadFactor
     * @see #getInitialCapacity
     */
    public int getMaximumCapacity()
    {
        return maxCapacity;
    }

    /**
     * Determine whether this map is empty or not.
     *
     * @return <tt>true</tt> if the map has no mappings, <tt>false</tt>
     *          otherwise
     */
    public boolean isEmpty()
    {
        return hash.isEmpty();
    }

    /**
     * <p>Return a <tt>Set</tt> view of the keys in the map. The set is
     * backed by the map, so changes to the map are reflected in the set,
     * and vice-versa. The set does supports element removal, which removes
     * the corresponding mapping from this map; however, the
     * <tt>Iterator</tt> returned by the set currently does <b>not</b>
     * support element removal. The set does not support the <tt>add</tt>
     * or <tt>addAll</tt> operations.
     *
     * @return the set of keys in this map
     */
    public Set keySet()
    {
        return new KeySet();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the key already has a value in this map, the existing value is
     * replaced by the new value, and the old value is replaced. If the key
     * already exists in the map, it is moved to the end of the key
     * insertion order list.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to associate with the specified key
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     */
    public Object put (Object key, Object value)
    {
        // If the total number of entries is at capacity, then we need to
        // remove one of them to make room. The linked list is a priority
        // queue, of sorts, with least recently used items at the end. So
        // remove the tail entries.

        Object              oldValue = null;
        LRULinkedListEntry  entry    = (LRULinkedListEntry) hash.get (key);

        if (entry == null)
        {
            // Must add a new one. Clear out the cruft. Reuse the last
            // cleared entry, though, rather than allocate a new object.

            entry = clearTo (this.maxCapacity - 1);
            if (entry == null)
                entry = new LRULinkedListEntry (key, value);
            else
                entry.setKeyValue (key, value);

            lruQueue.addToHead (entry);
            hash.put (key, entry);
        }

        else
        {
            // We're replacing the value with a new one. Move the entry to
            // the head of the list.

            oldValue = entry.value;
            entry.value = value;
            lruQueue.moveToHead (entry);
        }

        return oldValue;
    }

    /**
     * Copies all of the mappings from a specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys.
     *
     * @param map  the map whose mappings are to be copied
     *
     * @see #putAll(LRUMap)
     */
    public void putAll (Map map)
    {
        for (Iterator it = map.keySet().iterator(); it.hasNext(); )
        {
            Object key = it.next();
            Object value = map.get (key);

            this.put (key, value);
        }
    }

    /**
     * Removes the mapping for a key, if there is one.
     *
     * @param key the key to remove
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     */
    public Object remove (Object key)
    {
        Object              value = null;
        LRULinkedListEntry  entry = (LRULinkedListEntry) hash.remove (key);

        if (entry != null)
        {
            value = entry.value;
            lruQueue.remove (entry);

            callRemovalListeners (key, value, false);
        }

        assert (hash.size() == lruQueue.size);

        return value;
    }

    /**
     * Set or change the maximum capacity of this <tt>LRUMap.</tt> If the
     * maximum capacity is reduced to less than the map's current size,
     * then the map is reduced in size by discarding the oldest entries.
     *
     * @param newCapacity  the new maximum capacity
     *
     * @return the old maximum capacity
     *
     * @see #getMaximumCapacity
     */
    public int setMaximumCapacity (int newCapacity)
    {
        assert (newCapacity > 0);

        int oldCapacity = this.maxCapacity;
        clearTo (newCapacity);
        this.maxCapacity = newCapacity;
        return oldCapacity;
    }

    /**
     * Get the number of entries in the map. Note that this value can
     * temporarily exceed the maximum capacity of the map. See the class
     * documentation for details.
     *
     * @return the number of entries in the map
     */
    public int size()
    {
        return lruQueue.size;
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Returns a shallow copy of this instance. The keys and values themselves
     * are not cloned.
     *
     * @return a shallow copy of this map
     */
    protected Object clone()
    {
        return new LRUMap (this);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private LRULinkedListEntry clearTo (int size)
    {
        assert (hash.size() == lruQueue.size);
        LRULinkedListEntry oldTail = null;

        while (lruQueue.size > size)
        {
            oldTail = lruQueue.removeTail();

            assert (oldTail != null);

            Object              key = oldTail.key;
            LRULinkedListEntry  rem = (LRULinkedListEntry) hash.remove (key);

            assert (rem != null);
            assert (rem.key == key);

            callRemovalListeners (key, rem.value, true);
        }

        assert (lruQueue.size <= size);
        assert (hash.size() == lruQueue.size);

        return oldTail;
    }

    private synchronized void callRemovalListeners (final Object  key,
                                                    final Object  value,
                                                    boolean       automatic)
    {
        if (removalListeners != null)
        {
            for (Iterator it = removalListeners.values().iterator();
                 it.hasNext(); )
            {
                RemovalListenerWrapper l = (RemovalListenerWrapper) it.next();

                if ((! automatic) && (l.automaticOnly))
                    continue;

                Map.Entry entry = new Map.Entry()
                                  {
                                      public boolean equals (Object o)
                                      {
                                          return false;
                                      }

                                      public Object getKey()
                                      {
                                          return key;
                                      }

                                      public Object getValue()
                                      {
                                          return value;
                                      }

                                      public int hashCode()
                                      {
                                          return key.hashCode();
                                      }

                                      public Object setValue (Object val)
                                      {
                                          return null;
                                      }
                                  };

                l.objectRemoved (new ObjectRemovalEvent (entry));
            }
        }
    }
}
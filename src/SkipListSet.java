//Aviel Dahaman
//COP3503, Summer 25
//Final Project
//Due Date: 7/29/25
/* Description: This class implements a collections framework for a SkipList. It accepts as data any object that extends
Comparable. The skip list acts as a linked list at the base layer but has higher layers that enable skipping parts of the
list. This allows the list to perform in loglinear time rather than the linear time of a linked list.
 */

import java.lang.reflect.Array;
import java.util.*;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {
    private int maxHeight; //the maximum height of the skiplist
    private SkipListSetItem head; //the top-left most node of the skiplist
    private ArrayList<SkipListSetItem> tail; //the bottom-right most node of the skiplist


    //Class Node holds the links between the Items in the skiplist
    private class Node{
        SkipListSetItem owner;
        Node next;
        Node prev;

        private Node(SkipListSetItem owner){
            this.owner = owner;
            next = null;
            prev = null;
        }
    }

    //SkipListSetItem is a node in the skiplist. It holds the payload, links forwards, back
    private class SkipListSetItem {
        private final T payload;
        private ArrayList<Node> column;
        private static int count = 0;

        //Constructor, requires a comparable object
        private SkipListSetItem(T payload){
            this.payload = payload;
            column = new ArrayList<>(20);
            randomizeHeight();
            count++;
        }

        //getPayload returns the payload
        private T getPayload(){
            return payload;
        }

        private void randomizeHeight(){
            Random rand = new Random(System.currentTimeMillis());
            if (column.size() < 1){
                column.add(new Node(this));
            }
            while (rand.nextBoolean() && column.size() <= maxHeight){
                column.add(new Node(this));
            }
        }
    } //End SkipListSetItem class

    /* Since an Iterator needs to proceed through every element of the list, it should only access nodes
    * along the base layer. The iterator will not use the higher tiers of the skiplist. */
    private class SkipListSetIterator implements Iterator<T> {
        //Naming the iterator "itrtr" to avoid later confusion with itr
        private SkipListSetItem itrtr;
        private SkipListSetItem lastReturned;

        //Iterator Constructor
        private SkipListSetIterator(){
            //itrtr should always start at the head
            //making sure the head and it's next are not null
            if (head != null && head.column.get(0).next != null){
                itrtr = head.column.get(0).next.owner;
            }
            lastReturned = null;

        }

        //Checks if the iterator is at null
        public boolean hasNext(){
            return (itrtr != null);
        }

        /** This method saves the iterator's current payload and it's current Item and then moves the iterator forward
         * to the next item in the skiplist. It then returns the saved payload. The method throws a NoSuchElementException
         *  if the iterator is null.
         *
         * @return T payload
         */
        public T next(){
            if (itrtr == null){
                throw new NoSuchElementException();
            }
            lastReturned = itrtr;
            T currentPayload = itrtr.payload;
            if (itrtr.column.get(0).next != null){
                itrtr = itrtr.column.get(0).next.owner;
            } else {
                itrtr = null;
            }
            return currentPayload;
        }

        /** This method removes the last Item returned by next() from the skiplist. After removal, it sets lastReturned
         * to null since remove should only be called once per next().
         * This method throws an IllegalStateException if the iterator is null.
         */
        public void remove(){
            if (itrtr == null){
                throw new IllegalStateException("SkipListSetIterator is null");
            }

            //To remove a node, start at the bottom of its stack, adjust links around it, and isolate it as we go up
            for (int i = 0; i < lastReturned.column.size(); i++) {
                if (lastReturned.column.get(i).prev != null){
                    lastReturned.column.get(i).prev.next = lastReturned.column.get(i).next;
                }
                if (lastReturned.column.get(i).next != null){
                    lastReturned.column.get(i).next.prev = lastReturned.column.get(i).prev;
                }
            }

            lastReturned = null;
            SkipListSetItem.count--;
        }
    }//End Iterator class

    //Constructor
    public SkipListSet(){
        //Minimum height for a new list is 4
        maxHeight = 8;
        head = null;
        tail = new ArrayList<>(20);
        for (int i = 0; i < maxHeight; i++){
            tail.add(null);
        }
    }

    /** This method rebalances the list so that it still works in O(log n) time. After adding and deleting, the
     * list might no longer have sufficient items at higher levels that allow for skipping. This method randomizes
     * the height of each item except the head (which is always set to maxHeight). Then reconnects the higher levels
     * of the list.
     *
     * @return true if any nodes were rebalanced
     */
    public boolean reBalance() {
        if (head.column.size() != maxHeight) {
            if (head.column.size() > maxHeight) {
                while (head.column.size() > maxHeight) {
                    head.column.removeLast();
                }
            } else {
                while (head.column.size() < maxHeight) {
                    head.column.add(new Node(head));
                }
            }
            if (tail.size() != maxHeight) {
                if (tail.size() > maxHeight) {
                    while (tail.size() > maxHeight) {
                        tail.removeLast();
                    }
                } else {
                    while (tail.size() < maxHeight) {
                        tail.add(head);
                    }
                }
            }
        }

        //Creating an ArrayList that will hold all the SkipListSetItems in order
        ArrayList<SkipListSetItem> items = new ArrayList<>();
        SkipListSetItem cursor;
        if (head.column.get(0).next != null) {
            cursor = head.column.get(0).next.owner;
        } else {
            cursor = null;
        }
        while (cursor != null) {
            items.add(cursor);
            if (cursor.column.get(0).next != null){
                cursor = cursor.column.get(0).next.owner;
            }  else {
                cursor = null;
            }
        }

        //Restructuring each layer from the collected list
        //Since the list is in order, there's no fear of skipping a node
        for (int height = 1; height < maxHeight; height++) {
            Node prev = head.column.get(height);
            for (SkipListSetItem item : items) {
                if (item.column.size() > height) {
                    Node n = item.column.get(height);
                    prev.next = n;
                    n.prev = prev;
                    prev = n;
                }
            }
            prev.next = null;
            tail.set(height, prev.owner);
        }

        return true;
    } //end reBalance()


    /** This method grows the list vertically. It executes whenever the size of the list reaches a power of 2.
     * The maxHeight should grow with the floor of log size() (log in base 2). For example, if the list had 63 items,
     * the maxHeight would have been set to 5. When the next item is added to the list, the size becomes 64 and the
     * list should be grown so the new maxHeight is 6. The list is rebalanced after it is grown.
     */
    private void grow(){
        if (Math.floor(Math.log(size()) / Math.log(2)) >= maxHeight){
            head.column.add(new Node(head));
            maxHeight++;
            tail.add(head);
            //reBalance();
        }
    }

    /** This method shrinks the list vertically. It executes whenever the size of the list shrinks below a power of 2.
     * The maxHeight is adjusted downwards and the list is then rebalanced. The skiplist's minimum maxHeight is always 8
     * to avoid unnecessary rebalancing under 256 items.
     */
    private void shrink(){
        if (maxHeight > 8){
            if (Math.floor(Math.log(size()) / Math.log(2)) <= maxHeight){
                head.column.removeLast();
                maxHeight--;
                tail.removeLast();
                //reBalance();
            }
        }
    }

    //Implementing SortedSet interface methods

    /** This set uses the object's natural ordering. The comparator returns null.
     *
     * @return null
     */
    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    /** Returns the first (lowest) element currently in this set.
     *
     * @return T element, the first element in this set
     */
    @Override
    public T first() {
        return head.column.get(0).next.owner.payload;
    }

    /** Returns the last (highest) element currently in this set
     *
     * @return T element, the last (highest) element in the set
     */
    @Override
    public T last() {
        return tail.get(0).payload;
    }

    /** This method returns a subset from the head to the given element (exclusive)
     *
     * @param toElement high endpoint (exclusive) of the returned set
     * @return SortedSet<T>
     */
    @Override
    public SortedSet<T> headSet(T toElement){
        SortedSet<T> set = new SkipListSet<>();
        SkipListSetIterator itr = new SkipListSetIterator();
        while (itr.hasNext()){
            T current = itr.next();
            if (current.equals(toElement)){
                break;
            }
            set.add(current);
        }
        return set;
    }

    /** This method returns a subset from the given starting point (inclusive) to the given ending point (exclusive)
     *
     * @param fromElement low endpoint (inclusive) of the returned set
     * @param toElement high endpoint (exclusive) of the returned set
     * @return Set of T Elements from the starting object up to the ending object
     */
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        SortedSet<T> set = new SkipListSet<>();
        SkipListSetIterator itr = new SkipListSetIterator();
        while (itr.hasNext()){
            T current = itr.next();
            if (current.compareTo(fromElement) >= 0  && current.compareTo(toElement) < 0){
                set.add(current);
            }
            if (current.equals(toElement)){
                break;
            }
        }
        return set;
    }

    /** This method returns a subset from the given starting point (inclusive) to the Tail (exclusive)
     *
     * @param fromElement low endpoint (inclusive) of the returned set
     * @return
     */
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        SortedSet<T> set = new SkipListSet<>();
        SkipListSetIterator itr = new SkipListSetIterator();
        while (itr.hasNext()){
            T current = itr.next();
            if (current.compareTo(fromElement) >= 0){
                set.add(current);
            }
        }
        return set;
    }

    //Implementing Set's interface methods

    /** This method adds an element to the skiplist. It uses findNext() to take advantage of the skiplist's properties
     * to find the next largest node in log n time.
     *
     * @param element element whose presence in this collection is to be ensured
     * @return returns true if successfully added, false if not added
     */
    @Override
    public boolean add(T element) {
        if (element == null){
            return false;
        }
        //Special Case: If the SkipListSet is empty, create the set
        if (head == null){
            //Create a dummy head that is just a sentinel
            head = new SkipListSetItem(null);

            for (int i = 0; i < maxHeight; i ++){
                head.column.add(new Node(head));
                tail.set(i, head);
            }

            //Create the first node
            SkipListSetItem newNode = new SkipListSetItem(element);
            head.column.get(0).next = newNode.column.get(0);
            newNode.column.get(0).prev = head.column.get(0);
            tail.set(0, newNode);

            for (int i = 1; i < newNode.column.size(); i++){
                newNode.column.get(i).prev = head.column.get(i);
                head.column.get(i).next = newNode.column.get(i);
                tail.set(i, newNode);
            }

            return true;
        }

        for (int i = 0; i < tail.size(); i++){
            if (tail.get(i).payload != null && element.compareTo(tail.get(i).payload) > 0){
                return addToTail(element, tail.get(i), i);
            }
        }


        //Traversing the list to find the new node's spot
        //As it goes, it saves the drop points so the list doesn't need to be traversed again to connect nodes
        SkipListSetItem current = head;
        ArrayList<SkipListSetItem> dropList = new ArrayList<>(20);

        for (int height = maxHeight - 1; height >= 0; height--) {
            while (current.column.get(height).next != null && current.column.get(height).next.owner.payload.compareTo(element) < 0) {
                current = current.column.get(height).next.owner;
            }
            dropList.add(current);
        }

        //Making sure the element is not a duplicate
        if (current.column.get(0).next != null){
            if (current.column.get(0).next.owner.payload.equals(element)){
                return false;
            }
        }

        //Creating the new node
        SkipListSetItem newNode = new SkipListSetItem(element);
        //Connecting all the layers of the node through the dropList

        //Node drops are saved in the dropList to make connections easier
        //Using the dropPoints to reconnect the list
        for (int i = 0; i < newNode.column.size(); i++){
            SkipListSetItem dropPoint = dropList.removeLast();
            newNode.column.get(i).next = dropPoint.column.get(i).next;
            if (dropPoint.column.get(i).next != null){
                dropPoint.column.get(i).next.prev = newNode.column.get(i);
            }
            newNode.column.get(i).prev = dropPoint.column.get(i);
            dropPoint.column.get(i).next = newNode.column.get(i);
        }

        for (int i = 0; i < newNode.column.size(); i++){
            if (newNode.column.get(0).next == null){
                tail.set(i, newNode);
            }
        }

        //Grow checks to see if the list needs to be grown and then rebalanced
        grow();
        return true;
    }//end add()

    /** This method adds elements to the tail. It uses the stored tail ArrayList to help skip portions of the list,
     * cutting down list traversal.
     *
     * @param element T, the element to be added
     * @param item SkipListSetItem, the tail node used to skip part of the list traversal
     * @param index int, the index of the tail node in the tail ArrayList
     * @return boolean, true if successfully added, false if it's a duplicate
     */
    private boolean addToTail(T element, SkipListSetItem item, int index){
        //If the element to be added is greater than the tail at the base level, it is greater than all other Items
        if (index == 0){
            //Create the new node and connect it to the old tail,
            SkipListSetItem newNode = new SkipListSetItem(element);
            SkipListSetItem oldTail = tail.get(0);
            oldTail.column.get(0).next = newNode.column.get(0);
            newNode.column.get(0).prev = oldTail.column.get(0);
            tail.set(0, newNode);
            for (int i = 1; i < newNode.column.size(); i++){
                newNode.column.get(i).prev = tail.get(i).column.get(i);
                tail.get(i).column.get(i).next = newNode.column.get(i);
                tail.set(i, newNode);
            }
            return true;
        }

        //We can cut down traversal times by using interior tail nodes
        //Traversing the list to find the new node's spot
        //As it goes, it saves the drop points so the list doesn't need to be traversed again to connect nodes
        SkipListSetItem current = item;
        ArrayList<SkipListSetItem> dropList = new ArrayList<>(20);

        for (int height = current.column.size()-1; height >= 0; height--) {
            while (current.column.get(height).next != null && current.column.get(height).next.owner.payload.compareTo(element) < 0) {
                current = current.column.get(height).next.owner;
            }
            dropList.add(current);
        }

        //Making sure the element is not a duplicate
        if (current.column.get(0).next != null){
            if (current.column.get(0).next.owner.payload.equals(element)){
                return false;
            }
        }

        //Creating the new node
        SkipListSetItem newNode = new SkipListSetItem(element);
        //Connecting all the layers of the node through the dropList

        //Node drops are saved in the dropList to make connections easier
        //Using the dropPoints to reconnect the list
        if (newNode.column.size() <= dropList.size()){
            //if the newNode's height is the limiting factor of how to add the node, use the dropList to attach the
            //newNode's layers to other nodes
            for (int i = 0; i < newNode.column.size(); i++){
                SkipListSetItem dropPoint = dropList.removeLast();
                newNode.column.get(i).next = dropPoint.column.get(i).next;
                if (dropPoint.column.get(i).next != null){
                    dropPoint.column.get(i).next.prev = newNode.column.get(i);
                }
                newNode.column.get(i).prev = dropPoint.column.get(i);
                dropPoint.column.get(i).next = newNode.column.get(i);
            }
        } else {
            //if the droplist's height is the limiting factor, supplement with the tail array to finish connecting the
            //newNode to other nodes
            for (int i = 0; i < newNode.column.size(); i++){
                if (i < dropList.size()-1){
                    SkipListSetItem dropPoint = dropList.removeLast();
                    newNode.column.get(i).next = dropPoint.column.get(i).next;
                    if (dropPoint.column.get(i).next != null){
                        dropPoint.column.get(i).next.prev = newNode.column.get(i);
                    }
                    newNode.column.get(i).prev = dropPoint.column.get(i);
                    dropPoint.column.get(i).next = newNode.column.get(i);
                } else {
                    SkipListSetItem dropPoint = tail.get(i);
                    newNode.column.get(i).next = dropPoint.column.get(i).next;
                    if (dropPoint.column.get(i).next != null){
                        dropPoint.column.get(i).next.prev = newNode.column.get(i);
                    }
                    newNode.column.get(i).prev = dropPoint.column.get(i);
                    dropPoint.column.get(i).next = newNode.column.get(i);
                }
            }
        }

        //Check if the newNode is now a tail
        for (int i = 0; i < newNode.column.size(); i++){
            if (newNode.column.get(0).next == null){
                tail.set(i, newNode);
            }
        }

        return true;
    }


    /** addAll adds a collection of objects to the skiplist. If even 1 item is added, the method returns true.
     * The method returns false if none of the items were added.
     *
     * @param c collection containing elements to be added to this collection
     * @return true if at least 1 item is added
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        Iterator<? extends T> itr = c.iterator();
        while (itr.hasNext()){
            result |= add(itr.next());
        }
        //System.out.println("Add all result is: " + containsAll(c));
        return result;
    }

    /** Clears the list by setting the head and tail to null.
     */
    @Override
    public void clear(){
        head = null;
        tail = null;
    }

    /** This method searches the skiplist for the given object. The object is checked if it is comparable and then
     * cast to T. If either the check or the cast fail, method returns false.
     *
     * @param o element whose presence in this set is to be tested
     * @return true if present in skiplist, false if not
     */
    @Override
    public boolean contains(Object o){
        //If the object is not of type T in the skiplist, return false
        if (!(o instanceof Comparable<?>)){
            return false;
        }
        try {
            T target = (T) o;
            SkipListSetItem current = head;
            int height = maxHeight-1;
            //Searching from the top down. At each level, it moves as far to the right as it can before encountering
            //a payload that's larger than the target.
            while (height >= 0){
                while (current.column.get(height).next != null && current.column.get(height).next.owner.payload.compareTo(target) < 0){
                    current = current.column.get(height).next.owner;
                }
                height--;
            }
            //After traversal, current should either hold the target or be as close to the target as it can be.
            if (current.column.get(0).next != null && current.column.get(0).next.owner.payload.equals(target)){
                return true;
            } else {
                return false;
            }
        } catch (ClassCastException e){
            return false;
        }

    }

    /** containsAll checks if a collection of objects are present in the skiplist. If even 1 item is missing, the method
     * returns false.
     *
     * @param c collection to be checked for containment in this set
     * @return true if all are present, false if any are missing
     */
    @Override
    public boolean containsAll(Collection<?> c){
        boolean result = true;
        Iterator<?> itr = c.iterator();
        while (itr.hasNext()){
            result &= contains(itr.next());
        }
        return result;
    }

    /** This method compares the skiplistset with another set by checking if they both start at the same head
     *
     * @param o object to be compared for equality with this set
     * @return
     */
    @Override
    public boolean equals(Object o){
        //Checking that o is also a SkipListSet
        if (!(o instanceof SkipListSet<?>)){
            return false;
        }
        //Checking that they're both the same size
        if (((SkipListSet<?>)o).size() != size()){
            return false;
        }
        //Every element of this skiplist must be the same as the corresponding element in the other list
        Iterator<T> itr = new SkipListSetIterator();
        Iterator<?> oItr = ((SkipListSet<T>)o).iterator();
        while (itr.hasNext() && oItr.hasNext()){
            T current = itr.next();
            if (!current.equals(oItr.next())){
                return false;
            }
        }
        //Finally, both must be done at the same time
        return !itr.hasNext() && !oItr.hasNext();
    }

    /** hashCode takes the sum of the hashCodes of each element in the set and returns it.
     *
     * @return int sum of each element in the set's hash code
     */
    @Override
    public int hashCode(){
        int hash = 0;
        for (T e : this){
            hash += (e == null ? 0 : e.hashCode());
        }
        return hash;
    }

    /** Checks if the list is empty by checking the head's reference
     *
     * @return true if the list is empty, false if not
     */
    @Override
    public boolean isEmpty(){
        return head == null;
    }

    /** Creates an iterator to traverse the list and return elements in it
     *
     * @return new SkipListSetIterator object
     */
    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    /** This method attempts to remove an element from the skiplist. If the object is not the correct reference type,
     * the method returns false. It also returns false if the object is not present in the skiplist. Otherwise, it
     * searches the skiplist in the same manner as contains() in log n time. The method returns true if the object
     * is successfully removed.
     *
     * @param o object to be removed from this set, if present
     * @return boolean true for successful deletion, false if not
     */
    @Override
    public boolean remove(Object o){
        if (!(o instanceof Comparable<?>)){
            return false;
        }
        try {
            //Searching for the target using the same method as contains()
            T target = (T) o;
            SkipListSetItem current;
            current = head;
            for (int i = 0; i < tail.size(); i++){
                if (tail.get(i).payload != null && target.compareTo(tail.get(i).payload) > 0){
                    current = tail.get(i);
                }
            }
            int height = current.column.size()-1;
            while (height >= 0){
                while (current.column.get(height).next != null && current.column.get(height).next.owner.payload.compareTo(target) < 0){
                    current = current.column.get(height).next.owner;
                }
                height--;
            }
            //Current should now either be right before the target or be null
            if (current.column.get(0).next != null){
                current = current.column.get(0).next.owner;
            } else {
                current = null;
            }

            if (current == null || !current.payload.equals(target)){
                return false;
            }


            //Current is in the middle of the skiplist
            //Deleting the element by changing the list around it
            for (int i = current.column.size()-1; i >= 0; i--){
                if (current.column.get(i).next != null){
                    current.column.get(i).next.prev = current.column.get(i).prev;
                }
                if (current.column.get(i).prev != null){
                    current.column.get(i).prev.next = current.column.get(i).next;
                }
            }
            for (int i = current.column.size()-1; i >= 0; i--){
                if (current == tail.get(i)){
                    tail.set(i, current.column.get(0).prev.owner);
                }
            }
            shrink();
            SkipListSetItem.count--;
            return true;
        } catch (ClassCastException e){
            return false;
        }
    }

    /** This method attempts to remove from the skiplist all the elements of a given collection. If at least one
     * is successfully removed, the method returns true. If none of the elements are removed, the method returns false.
     *
     * @param c collection containing elements to be removed from this set
     * @return true if at least one element is removed, false if none are removed
     */
    @Override
    public boolean removeAll(Collection<?> c){
        boolean result = false;
        Iterator<?> itr = c.iterator();
        while (itr.hasNext()){
            result |= remove(itr.next());
        }
        return result;
    }

    /** This method removes any element in the skiplist that is not also in Collection c. It compares each element
     * of the skiplist (using the iterator) to the collection by using c.contains(element). If c.contains(element)
     * returns false, then the element is removed from the skiplist. If even one element is removed, the method
     * returns true.
     *
     * @param c collection containing elements to be retained in this set
     * @return returns true if at least one element is removed
     */
    @Override
    public boolean retainAll(Collection<?> c){
        boolean result = false;
        Iterator<T> itr = iterator();

        while (itr.hasNext()){
            if (!c.contains(itr.next())){
                itr.remove();
                result = true;
            }
        }
        return result;
    }

    /** Gets the size of the SkipList by returning the static int count (number of payload wrappers)
     *
     * @return int, number of payloads in the SkipList
     */
    @Override
    public int size(){
        return SkipListSetItem.count;
    }

    /** This method outputs the SkipListSet as an array of type Objects. The Object is a wrapper on the payload since
     * the generic T cannot be cloned without enforcing Cloneable (which limits the applications of this Set).
     * If the actual payload were accessed, it would affect the SkipListSet's data. This is in effect a shallow copy.
     *
     * @return Object[] Array of the payloads in SkipListSet
     */
    @Override
    public Object[] toArray(){
        Object[] arr = new Object[size()];
        Iterator<T> itr = iterator();
        for (int i = 0; i < size(); i++){
            arr[i] = itr.next(); //shallow copy
        }
        return arr;
    }

    /** This method produces an array of T[] objects that holds the set of payload objects that the SkipListSet
     * uses. Since T cannot be cloned without enforcing Cloneable and losing usability, modifying the array may
     * modify the SkipListSet.
     *
     * @param arr the array into which the elements of this set are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return T[] array
     * @param <T>
     */
    @Override
    public <T> T[] toArray(T[] arr){
        int size = size();
        if (arr.length < size) {
            arr = (T[]) Array.newInstance(arr.getClass().getComponentType(), size);
        } else if (arr.length > size) {
            arr[size] = null;
        }

        Iterator<T> itr = (Iterator<T>) iterator();
        for (int i = 0; i < size; i++){
            arr[i] = itr.next();
        }

        return arr;
    }
}

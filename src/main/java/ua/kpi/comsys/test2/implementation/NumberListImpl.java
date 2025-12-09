package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ua.kpi.comsys.test2.NumberList;

public class NumberListImpl implements NumberList {

    private final int base = 3;
    private final int additionalBase = 8;

    private static class Node {
        byte digit;
        Node prev;
        Node next;

        Node(byte d) { this.digit = d; }
    }

    private Node head;
    private Node tail;
    private int size;

    public NumberListImpl() {
        head = tail = null;
        size = 0;
    }

    public NumberListImpl(File file) {
        this();
        if (file == null) return;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String s = r.readLine();
            if (s != null) {
                s = s.trim();
                initFromDecimalString(s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NumberListImpl(String value) {
        this();
        initFromDecimalString(value);
    }

    private void initFromDecimalString(String decimal) {
        if (decimal == null) return;
        decimal = decimal.trim();
        if (decimal.isEmpty()) return;
        BigInteger bi = new BigInteger(decimal);
        if (bi.signum() < 0) throw new IllegalArgumentException("Only positive numbers allowed");
        if (bi.equals(BigInteger.ZERO)) {
            add((byte)0);
            return;
        }
        java.util.ArrayList<Byte> digits = new java.util.ArrayList<>();
        BigInteger b = bi;
        BigInteger bigBase = BigInteger.valueOf(base);
        while (!b.equals(BigInteger.ZERO)) {
            BigInteger[] qr = b.divideAndRemainder(bigBase);
            digits.add(qr[1].byteValue());
            b = qr[0];
        }
        for (int i = digits.size()-1; i >= 0; --i) {
            add(digits.get(i));
        }
    }

    public void saveList(File file) {
        if (file == null) throw new IllegalArgumentException("file == null");
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            w.write(toDecimalString());
            w.newLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getRecordBookNumber() {
        return 6;
    }

    public NumberListImpl changeScale() {
        BigInteger dec = toBigIntegerFromBase(base);
        if (dec.equals(BigInteger.ZERO)) {
            NumberListImpl out = new NumberListImpl();
            out.add((byte)0);
            return out;
        }
        BigInteger bigBase = BigInteger.valueOf(additionalBase);
        java.util.ArrayList<Byte> digits = new java.util.ArrayList<>();
        BigInteger b = dec;
        while (!b.equals(BigInteger.ZERO)) {
            BigInteger[] qr = b.divideAndRemainder(bigBase);
            digits.add(qr[1].byteValue());
            b = qr[0];
        }
        NumberListImpl out = new NumberListImpl();
        for (int i = digits.size()-1; i >= 0; --i) out.add(digits.get(i));
        return out;
    }

    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null) throw new IllegalArgumentException("arg==null");
        BigInteger a = this.toBigIntegerFromBase(base);
        BigInteger b = toBigIntegerFromNumberList(arg);
        BigInteger res = a.or(b);
        return new NumberListImpl(res.toString());
    }

    private BigInteger toBigIntegerFromBase(int bbase) {
        BigInteger res = BigInteger.ZERO;
        BigInteger bpow = BigInteger.ONE;
        Node cur = tail;
        while (cur != null) {
            res = res.add(bpow.multiply(BigInteger.valueOf(cur.digit)));
            bpow = bpow.multiply(BigInteger.valueOf(bbase));
            cur = cur.prev;
        }
        return res;
    }

    private static BigInteger toBigIntegerFromNumberList(NumberList nl) {
        if (nl instanceof NumberListImpl) {
            return ((NumberListImpl) nl).toBigIntegerFromBase(((NumberListImpl) nl).base);
        }
        BigInteger res = BigInteger.ZERO;
        for (Byte d : nl) {
            res = res.multiply(BigInteger.TEN).add(BigInteger.valueOf(d));
        }
        return res;
    }

    public String toDecimalString() {
        BigInteger dec = toBigIntegerFromBase(base);
        return dec.toString();
    }

    @Override
    public String toString() {
        if (isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        Node cur = head;
        while (cur != null) {
            sb.append(Byte.toString(cur.digit));
            cur = cur.next;
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberList)) return false;
        NumberList other = (NumberList) o;
        if (this.size() != other.size()) return false;
        Iterator<Byte> it1 = this.iterator();
        Iterator<Byte> it2 = other.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!it1.next().equals(it2.next())) return false;
        }
        return true;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) return false;
        byte v = (Byte) o;
        Node cur = head;
        while (cur != null) {
            if (cur.digit == v) return true;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            Node cur = head;
            @Override public boolean hasNext() { return cur != null; }
            @Override public Byte next() { byte v = cur.digit; cur = cur.next; return v; }
            @Override public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i=0; for (Byte b : this) arr[i++] = b; return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] arr = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            int i=0; for (Byte b : this) arr[i++] = (T) Byte.valueOf(b);
            return arr;
        } else {
            int i=0; for (Byte b : this) a[i++] = (T) Byte.valueOf(b);
            if (a.length > size) a[size] = null;
            return a;
        }
    }

    @Override
    public boolean add(Byte e) {
        if (e == null) throw new NullPointerException();
        if (e < 0 || e >= base) throw new IllegalArgumentException("Digit out of range for base " + base);
        Node n = new Node(e);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n; n.prev = tail; tail = n;
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) return false;
        byte v = (Byte)o;
        Node cur = head;
        while (cur != null) {
            if (cur.digit == v) {
                unlink(cur); return true;
            }
            cur = cur.next;
        }
        return false;
    }

    private Byte unlink(Node n) {
        Byte val = n.digit;
        Node p = n.prev; Node q = n.next;
        if (p == null) head = q; else p.next = q;
        if (q == null) tail = p; else q.prev = p;
        size--;
        return val;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) if (!contains(o)) return false; return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean changed = false;
        for (Byte b : c) changed |= add(b);
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        checkPositionIndex(index);
        boolean changed = false;
        int i = index;
        for (Byte b : c) { add(i++, b); changed = true; }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        Iterator<Byte> it = iterator();
        java.util.List<Byte> toRemove = new java.util.ArrayList<>();
        for (Byte b : this) if (c.contains(b)) toRemove.add(b);
        for (Byte b : toRemove) changed |= remove(b);
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Node cur = head;
        while (cur != null) {
            Node next = cur.next;
            if (!c.contains(cur.digit)) { unlink(cur); changed = true; }
            cur = next;
        }
        return changed;
    }

    @Override
    public void clear() { head = tail = null; size = 0; }

    @Override
    public Byte get(int index) { return node(index).digit; }

    @Override
    public Byte set(int index, Byte element) {
        if (element == null) throw new NullPointerException();
        if (element < 0 || element >= base) throw new IllegalArgumentException();
        Node n = node(index);
        byte old = n.digit; n.digit = element; return old;
    }

    @Override
    public void add(int index, Byte element) {
        if (element == null) throw new NullPointerException();
        if (element < 0 || element >= base) throw new IllegalArgumentException();
        checkPositionIndex(index);
        if (index == size) { add(element); return; }
        Node succ = node(index);
        Node pred = succ.prev;
        Node newNode = new Node(element);
        newNode.next = succ; succ.prev = newNode; newNode.prev = pred;
        if (pred == null) head = newNode; else pred.next = newNode;
        size++;
    }

    @Override
    public Byte remove(int index) { Node n = node(index); return unlink(n); }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte v = (Byte)o; int idx = 0; Node cur = head;
        while (cur != null) { if (cur.digit == v) return idx; idx++; cur = cur.next; }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte v = (Byte)o; int idx = size-1; Node cur = tail;
        while (cur != null) { if (cur.digit == v) return idx; idx--; cur = cur.prev; }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() { return listIterator(0); }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        checkPositionIndex(index);
        return new ListIterator<Byte>() {
            Node next = (index==size)? null : node(index);
            Node lastReturned = null;
            int nextIndex = index;
            @Override public boolean hasNext() { return nextIndex < size; }
            @Override public Byte next() { lastReturned = next; next = next.next; nextIndex++; return lastReturned.digit; }
            @Override public boolean hasPrevious() { return nextIndex > 0; }
            @Override public Byte previous() { if (next==null) { next = tail; } else { next = next.prev; } lastReturned = next; nextIndex--; return lastReturned.digit; }
            @Override public int nextIndex() { return nextIndex; }
            @Override public int previousIndex() { return nextIndex-1; }
            @Override public void remove() { if (lastReturned==null) throw new IllegalStateException(); unlink(lastReturned); lastReturned = null; }
            @Override public void set(Byte e) { if (lastReturned==null) throw new IllegalStateException(); lastReturned.digit = e; }
            @Override public void add(Byte e) { Node pred = (next==null)? tail : next.prev; Node newNode = new Node(e); newNode.next = next; newNode.prev = pred; if (pred==null) head = newNode; else pred.next = newNode; if (next==null) tail = newNode; else next.prev = newNode; nextIndex++; lastReturned = null; size++; }
        };
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        NumberListImpl nl = new NumberListImpl();
        Node cur = node(fromIndex);
        for (int i = fromIndex; i < toIndex; ++i) { nl.add(cur.digit); cur = cur.next; }
        return nl;
    }

    private Node node(int index) {
        checkElementIndex(index);
        if (index < (size >> 1)) {
            Node x = head; for (int i=0;i<index;i++) x = x.next; return x;
        } else {
            Node x = tail; for (int i=size-1;i>index;i--) x = x.prev; return x;
        }
    }

    private void checkElementIndex(int index) { if (index < 0 || index >= size) throw new IndexOutOfBoundsException(Integer.toString(index)); }
    private void checkPositionIndex(int index) { if (index < 0 || index > size) throw new IndexOutOfBoundsException(Integer.toString(index)); }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;
        Node n1 = node(index1); Node n2 = node(index2);
        byte t = n1.digit; n1.digit = n2.digit; n2.digit = t; return true;
    }

    @Override
    public void sortAscending() {
        // простий сортувальний алгоритм — збираємо в масив, сортуємо, записуємо назад
        byte[] arr = new byte[size]; int i=0; for (Byte b : this) arr[i++] = b;
        java.util.Arrays.sort(arr);
        i = 0; Node cur = head; while (cur != null) { cur.digit = arr[i++]; cur = cur.next; }
    }

    @Override
    public void sortDescending() {
        byte[] arr = new byte[size]; int i=0; for (Byte b : this) arr[i++] = b;
        java.util.Arrays.sort(arr);
        // reverse
        int l = 0, r = arr.length-1; while (l<r) { byte t = arr[l]; arr[l]=arr[r]; arr[r]=t; l++; r--; }
        i = 0; Node cur = head; while (cur != null) { cur.digit = arr[i++]; cur = cur.next; }
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) return;
        // move head to tail
        Node oldHead = head;
        head = oldHead.next; head.prev = null;
        oldHead.next = null; oldHead.prev = tail; tail.next = oldHead; tail = oldHead;
    }

    @Override
    public void shiftRight() {
        if (size <= 1) return;
        Node oldTail = tail;
        tail = oldTail.prev; tail.next = null;
        oldTail.prev = null; oldTail.next = head; head.prev = oldTail; head = oldTail;
    }

}


/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package util.objects;

import memory.IEnvironment;
import memory.IStateInt;
import solver.variables.Variable;
import util.Indexable;

/**
 * [<--inactive-->|<--active--->|<---entailed-->]<br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
@Deprecated
public final class BacktrackableArrayList<V extends Variable, E extends Indexable<V>>
        implements IList<V, E> {

    protected E[] elements;
    protected int size;

    protected IStateInt firstActive;
    protected IStateInt firstPassive;

    protected final V parent;

    public BacktrackableArrayList(V variable, IEnvironment environment) {
        this(variable, environment, 16);
    }

    public BacktrackableArrayList(V variable, IEnvironment environment, int size) {
        this.parent = variable;
        elements = (E[]) new Indexable[size];
        size = 0;
        firstActive = environment.makeInt();
        firstPassive = environment.makeInt();
    }

    @Override
    public void setActive(E element) {
        int first = this.firstActive.get();
        int i = element.getIdx(parent);
        if (first > i) {
            // swap element at pos "first" with element at pos "i"
            E tmp1 = elements[--first];
            elements[first] = elements[i];
            elements[first].setIdx(parent, first);
            elements[i] = tmp1;
            elements[i].setIdx(parent, i);
            firstActive.add(-1);
        }
    }

    @Override
    public void setPassive(E element) {
        int last = this.firstPassive.get();
        int i = element.getIdx(parent);
        if (last > i) {
            // swap element at pos "last" with element at pos "i"
            E tmp1 = elements[--last];
            elements[last] = elements[i];
            elements[last].setIdx(parent, last);
            elements[i] = tmp1;
            elements[i].setIdx(parent, i);
            firstPassive.add(-1);
        }
    }


    @Override
    public void add(E element, boolean dynamic, boolean activeSilently) {
        if (firstActive.get() != firstPassive.get()) {
            throw new UnsupportedOperationException("Can not add an element: activation has already started");
        }
        if (size == elements.length - 1) {
            E[] tmp = elements;
            elements = (E[]) new Indexable[size * 3 / 2 + 1];
            System.arraycopy(tmp, 0, elements, 0, size);
        }
        elements[size] = element;
        element.setIdx(parent, size++);
        if (!activeSilently) this.firstActive.add(1);
        this.firstPassive.add(1);
    }

    @Override
    public void remove(E element) {
        int i = 0;
        for (; i < size && elements[i] != element; i++) {
        }
        if (i == size) return;
        System.arraycopy(elements, i + 1, elements, i, size - i - 1);
        size--;
        for (int j = i; j < size; j++) {
            elements[j].setIdx(parent, j);
        }
        assert (this.firstPassive.getEnvironment().getWorldIndex() == 0);
        if (i < firstActive.get()) {
            this.firstActive.add(-1);
        }
        this.firstPassive.add(-1);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int cardinality() {
        return firstPassive.get();
    }

    @Override
    public E get(int i) {
        return elements[i];
    }
}

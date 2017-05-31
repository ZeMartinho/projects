package enigma;

import static enigma.EnigmaException.*;
import java.util.ArrayList;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Ze Martinho
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters not
     *  included in any cycle map to themselves. Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
        _cyclesList = new ArrayList<ArrayList<Character>>();
        char[] cyclesChar = _cycles.toCharArray();
        int i = 0;
        while (i < cyclesChar.length) {
            if (cyclesChar[i] == '(') {
                int k = i + 1;
                for ( ; cyclesChar[k] != ')'; k += 1) {
                    if (!alphabet.contains(cyclesChar[k])) {
                        throw new EnigmaException("Permutation not closed.");
                    }
                }
                char[] cycle = new char[k - i - 1];
                System.arraycopy(cyclesChar, i + 1, cycle, 0, k - i - 1);
                addCycle(new String(cycle));
                i = k;
            } else {
                i += 1;
            }
        }
    }
    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        char[] chars = cycle.toCharArray();
        ArrayList<Character> entry = new ArrayList<Character>();
        for (int i = 0; i < chars.length; i += 1) {
            entry.add(chars[i]);
        }
        _cyclesList.add(_cyclesList.size(), entry);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int remainder;
        if (p >= 0) {
            remainder = p % size();
        } else {
            remainder = size() + (p % size());
        }
        char permuted = permute(_alphabet.toChar(remainder));
        return _alphabet.toInt(permuted);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int remainder;
        if (c >= 0) {
            remainder = c % size();
        } else {
            remainder = size() + (c % size());
        }
        char inverted = invert(_alphabet.toChar(remainder));
        return _alphabet.toInt(inverted);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index = -1;
        ArrayList<Character> correctList = new ArrayList<Character>();
        int i = 0;
        while (i < _cyclesList.size()) {
            index = _cyclesList.get(i).indexOf(p);
            if (index != -1) {
                correctList = _cyclesList.get(i);
                break;
            } else {
                i += 1;
            }
        }
        if (index == -1) {
            return p;
        } else if (index == correctList.size() - 1) {
            return correctList.get(0);
        } else {
            return correctList.get(index + 1);
        }
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int index = -1;
        ArrayList<Character> correctList = new ArrayList<Character>();
        int i = 0;
        while (i < _cyclesList.size()) {
            index = _cyclesList.get(i).indexOf(c);
            if (index != -1) {
                correctList = _cyclesList.get(i);
                break;
            } else {
                i += 1;
            }
        }
        if (index == -1) {
            return c;
        } else if (index == 0) {
            return correctList.get(correctList.size() - 1);
        } else {
            return correctList.get(index - 1);
        }
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (ArrayList<Character> list : _cyclesList) {
            for (int i = 0; i < list.size(); i += 1) {
                if (permute(list.get(i)) == list.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }


    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** A two-dimensional ArrayList that stores permutations. */
    private ArrayList<ArrayList<Character>> _cyclesList;

    /** The string of many permutations. */
    private String _cycles;
}


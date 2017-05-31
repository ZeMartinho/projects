package enigma;

import static enigma.EnigmaException.*;

/* Extra Credit Only */

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Ze Martinho
 */
class Alphabet {
    /*  A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */

    /** I didn't do the extra credit, so the Alphabet class
     *  is unchanged.
     *  @param chars */
    Alphabet(String chars) {
    }

    /** Returns the size of the alphabet. */
    int size() {
        return 0;
    }

    /** Returns true if C is in this alphabet. */
    boolean contains(char c) {
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return 'A';
    }

    /** Returns the index of character C, which must be in the alphabet. */
    int toInt(char c) {
        return 0;
    }
}

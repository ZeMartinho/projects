package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Ze Martinho
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches.toCharArray();
    }

    @Override
    void advance() {
        if (setting() == size() - 1) {
            set(0);
        } else {
            set(setting() + 1);
        }
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length; i += 1) {
            if (setting() == alphabet().toInt(_notches[i])) {
                return true;
            }
        }
        return false;
    }

    /** An array of characters where the notches are.
     */
    private char[] _notches;
}

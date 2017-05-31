package enigma;
import java.util.Collection;

import static enigma.EnigmaException.*;

import java.util.ArrayList;

/** Class that represents a complete enigma machine.
 *  @author Ze Martinho
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (numRotors <= 1 || pawls < 0 || pawls >= numRotors) {
            throw new EnigmaException("wrong number of rotors and/or pawls");
        }
        if (allRotors.size() < numRotors) {
            throw new EnigmaException("Not enough rotors");
        } else {
            _alphabet = alpha;
            _numRotors = numRotors;
            _pawls = pawls;
            _allRotors = allRotors;
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _usedRotors = new ArrayList<Rotor>();
        Object[] allRotorsArr = _allRotors.toArray();
        ArrayList<String> rotorNames = new ArrayList<String>();
        for (int i = 0; i < allRotorsArr.length; i += 1) {
            rotorNames.add(i, ((Rotor) allRotorsArr[i]).name().toUpperCase());
        }
        int i = 0;
        while (i < rotors.length) {
            int j = 0;
            while (j < allRotorsArr.length) {
                if (rotors[i].equals(rotorNames.get(j))) {
                    if (_usedRotors.contains((Rotor) allRotorsArr[j])) {
                        throw new EnigmaException("Repeated rotor used");
                    } else {
                        _usedRotors.add(i, (Rotor) allRotorsArr[j]);
                        break;
                    }
                } else if (!rotorNames.contains(rotors[i])) {
                    throw new EnigmaException("Not a real rotor.");
                } else {
                    j += 1;
                }
            }
            i += 1;
        }
        if (!_usedRotors.get(0).reflecting()) {
            throw new EnigmaException("Missing reflector in machine!");
        } else if (_usedRotors.size() < rotors.length) {
            throw new EnigmaException("Not enough rotors accounted for.");
        } else if (_usedRotors.get(1).rotates()) {
            throw new EnigmaException("Missing fixed rotor.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of four
     *  upper-case letters. The first letter refers to the leftmost
     *  rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        try {
            char[] settingArray = setting.toCharArray();
            if (settingArray.length != _numRotors - 1) {
                throw new EnigmaException("Wrong number of settings");
            }
            for (int i = 1; i < _usedRotors.size(); i += 1) {
                _usedRotors.get(i).set(_alphabet.toInt(settingArray[i - 1]));
            }
        } catch (EnigmaException excp) {
            throw error("Wrong number of rotor settings");
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
        _setUp = true;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int lastIndex = _usedRotors.size() - 1;
        ArrayList<Rotor> advancingRotors = new ArrayList<Rotor>();
        advancingRotors.add(_usedRotors.get(lastIndex));
        for (int i = lastIndex; i > 0; i -= 1) {
            Rotor currRotor = _usedRotors.get(i);
            Rotor nextRotor = _usedRotors.get(i - 1);
            if (currRotor.atNotch() && nextRotor.rotates()) {
                advancingRotors.add(advancingRotors.size(), nextRotor);
                if (!advancingRotors.contains(currRotor)) {
                    advancingRotors.add(advancingRotors.size(), currRotor);
                }
            }
        }
        for (Rotor advancesRotor : advancingRotors) {
            advancesRotor.advance();
        }
        int result = _plugboard.permute(c);
        for (int j = lastIndex; j >= 0; j -= 1) {
            Rotor forwardRot = _usedRotors.get(j);
            result = forwardRot.convertForward(result);
        }
        for (int k = 1; k < lastIndex + 1; k += 1) {
            Rotor backwardRot = _usedRotors.get(k);
            result = backwardRot.convertBackward(result);
        }
        return _plugboard.permute(result);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char[] msgArray = msg.toCharArray();
        ArrayList<Character> resultList = new ArrayList<Character>();
        for (int i = 0; i < msgArray.length; i += 1) {
            if (_alphabet.contains(msgArray[i])) {
                char addedChar = _alphabet.toChar(
                    convert(_alphabet.toInt(msgArray[i])));
                resultList.add(addedChar);
            }
        }
        String result = "";
        for (char letter : resultList) {
            result += letter;
        }
        return result;
    }

    /** A simple boolean instance variable
     *  to determine whether or not the machine
     *  has been set up.
     *  @return a boolean indicating the machine's
     *  history. */
    boolean setUp() {
        return _setUp;
    }

    /** A String of the rotors used after inserting rotors.
     *  This method is used for only Unit testing, really.
     *  @return a string with the usedRotors names. */
    String usedRotorsString() {
        String result = "";
        for (Rotor x : _usedRotors) {
            result = result + " " + x.name();
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** The number of rotors for a particular machine. */
    private final int _numRotors;

    /** The number of pawls for a particular machine. */
    private final int _pawls;

    /** The permutation specific to the plugboard
     *  of the machine. */
    private Permutation _plugboard;

    /** A saved ArrayList of rotors in the correct
     *  order to be used. */
    private ArrayList<Rotor> _usedRotors;

    /** A collection of all rotors read from
     *  the .conf file. */
    private Collection<Rotor> _allRotors;

    /** The boolean instance variable indicating
     *  whether the machine has been set up or not. */
    private boolean _setUp;
}

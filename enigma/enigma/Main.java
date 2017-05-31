package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Ze Martinho
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine usedMachine = readConfig();
        String next = _input.nextLine();
        while (true) {
            if (!usedMachine.setUp()) {
                String settings = next;
                setUp(usedMachine, settings);
                if (_input.hasNextLine()) {
                    next = _input.nextLine();
                } else {
                    _input.close();
                    break;
                }
            } else if (next.startsWith("*") && usedMachine.setUp()) {
                String settings = next;
                setUp(usedMachine, settings);
                if (_input.hasNextLine()) {
                    next = _input.nextLine();
                } else {
                    _input.close();
                    break;
                }
            } else if (!_input.hasNextLine()) {
                printMessageLine(usedMachine.convert(next.toUpperCase()));
                _input.close();
                break;
            } else {
                printMessageLine(usedMachine.convert(next.toUpperCase()));
                next = _input.nextLine();
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config.
     *  Essentially, configure all ROTORS and make a ROTORS collection here.
     *  Then set up a machine with these rotors, and the correct number of pawls
     *  and rotors
     *  Use readRotors() method to set up rotors with their permutations.
     */
    private Machine readConfig() {

        try {
            String alphab = _config.next();
            if (!alphab.equals("ABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
                throw new EnigmaException("Wrong alphabet format.");
            }
            String stringRotors = _config.next();
            String stringPawls = _config.next();
            int numRotors = Integer.parseInt(stringRotors);
            int numPawls = Integer.parseInt(stringPawls);
            if (numRotors < numPawls || numPawls <= 0) {
                throw new EnigmaException("Wrong number of pawls and rotors.");
            }
            ArrayList<Rotor> rotors = new ArrayList<Rotor>();
            _config.nextLine();
            String nextLine = _config.nextLine();
            String secondLine = _config.nextLine().trim();
            while (true) {
                if (!_config.hasNextLine()) {
                    if (secondLine.startsWith("(")) {
                        rotors.add(
                            rotors.size(), readRotor(nextLine + secondLine));
                    } else {
                        rotors.add(rotors.size(), readRotor(nextLine));
                        rotors.add(rotors.size(), readRotor(secondLine));
                    }
                    _config.close();
                    break;
                } else if (secondLine.startsWith("(")) {
                    nextLine += secondLine;
                    secondLine = _config.nextLine().trim();
                } else {
                    rotors.add(rotors.size(), readRotor(nextLine));
                    nextLine = secondLine;
                    secondLine = _config.nextLine().trim();
                }
            }
            _alphabet = new UpperCaseAlphabet();
            return new Machine(_alphabet, numRotors, numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        } catch (NumberFormatException e) {
            throw new EnigmaException(
                "Number of rotors and/or pawls was not an integer.");
        }
    }

    /** Return a rotor, reading its description from _config.
     *  Instead of scanning _input, readRotor takes in a string
     *  @param rotorString
     *  This has the complete rotor description on it. */
    private Rotor readRotor(String rotorString) {
        try {
            Scanner line = new Scanner(rotorString);
            String name = line.next();
            String type = line.next();
            String notchesString = "";
            if (type.startsWith("M")) {
                char[] notchesArray = type.toCharArray();
                for (int i = 1; i < notchesArray.length; i += 1) {
                    notchesString += notchesArray[i];
                }
            }
            ArrayList<String> permsList = new ArrayList<String>();
            String nextToken = line.next();
            while (nextToken.startsWith("(")) {
                if (!nextToken.endsWith(")")) {
                    throw new EnigmaException("Incorrect permutation format!!");
                } else if (!line.hasNext()) {
                    permsList.add(permsList.size(), nextToken);
                    line.close();
                    break;
                } else {
                    permsList.add(permsList.size(), nextToken);
                    nextToken = line.next();
                }
            }
            String allPerms = "";
            for (String perm : permsList) {
                allPerms += perm;
            }
            if (type.equals("R")) {
                return new Reflector(
                    name, new Permutation(allPerms, _alphabet));
            } else if (type.equals("N")) {
                return new FixedRotor(
                    name, new Permutation(allPerms, _alphabet));
            } else {
                return new MovingRotor(
                    name, new Permutation(
                        allPerms, _alphabet), notchesString);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment.
     *  Set up plugboard and rotors order here
     *  Get SETTINGS from process
     */
    private void setUp(Machine M, String settings) {
        Scanner settingScan = new Scanner(settings);
        if (!settingScan.next().equals("*")) {
            _input.close();
            throw new EnigmaException("Bad settings description");
        } else {
            String[] rotorsArray = new String[M.numRotors()];
            for (int i = 0; i < M.numRotors(); i += 1) {
                String newRotor = settingScan.next();
                rotorsArray[i] = newRotor;
            }
            if (rotorsArray.length == 0) {
                throw new EnigmaException("No rotors detected");
            }
            M.insertRotors(rotorsArray);
            String rotorSetting = settingScan.next();
            M.setRotors(rotorSetting);
            String plugPerms = "";
            while (settingScan.hasNext()) {
                plugPerms += settingScan.next();
            }
            M.setPlugboard(new Permutation(plugPerms, _alphabet));
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        char[] msgArray = msg.toCharArray();
        int totalChar = 0;
        while (totalChar < msgArray.length) {
            int charCount = 0;
            while ((charCount < 5) && (totalChar < msgArray.length)) {
                if (msgArray.equals(" ")) {
                    totalChar += 1;
                } else {
                    _output.print(msgArray[totalChar]);
                    charCount += 1;
                    totalChar += 1;
                }
            }
            if (charCount < msgArray.length) {
                _output.print(" ");
            }
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet = new UpperCaseAlphabet();

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}

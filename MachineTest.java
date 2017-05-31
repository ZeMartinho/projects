package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    /** The machine made in this test. Its methods will be tested. */
    private Machine machine;

    /** A few rotors to be used in this test. */
    private ArrayList<Rotor> allRotors;

    /* ***** TESTS ***** */
    public void startMachine() {
        allRotors = new ArrayList<Rotor>();
        allRotors.add(new MovingRotor(
              "I", new Permutation(
                  "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)",
                  UPPER), "Q"));
        allRotors.add(new Reflector(
              "B", new Permutation(
                  "(AE)(BN)(CK)(DQ)(FU)(GY)(HW)(IJ)(LO)(MP)(RX)(SZ)(TV)",
                  UPPER)));
        allRotors.add(new FixedRotor(
              "BETA", new Permutation(
                  "(ALBEVFCYODJWUGNMQTZSKPR) (HIX)", UPPER)));
        allRotors.add(new MovingRotor(
              "IV", new Permutation(
                  "(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)", UPPER), "MJ"));
        allRotors.add(new MovingRotor(
              "VI", new Permutation(
                  "(AJQDVLEOZWIYTS) (CGMNHFUX) (BPRK) ", UPPER), "ZM"));
        allRotors.add(new MovingRotor(
              "III", new Permutation(
                  "(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)", UPPER), "V"));
        allRotors.add(new MovingRotor(
              "VIII", new Permutation(
                  "(AFLSETWUNDHOZVICQ) (BKJ) (GXY) (MPR)", UPPER), "ZM"));
        machine = new Machine(UPPER, 5, 3, allRotors);
        machine.setPlugboard(new Permutation("", UPPER));
    }

    @Test
    public void checkInitMachine() {
        startMachine();
        assertEquals(5, machine.numRotors());
        assertEquals(3, machine.numPawls());
    }

    @Test
    public void checkinsertRotors() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        assertTrue(machine.usedRotorsString().equals(
              " B BETA I III VIII"));
    }
    @Test
    public void checksetRotors() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        String setting = "GLEX";
        machine.setRotors(setting);
        assertEquals(allRotors.get(2).setting(),
            UPPER.toInt(setting.charAt(0)));
        assertEquals(allRotors.get(0).setting(),
            UPPER.toInt(setting.charAt(1)));
        assertEquals(allRotors.get(5).setting(),
            UPPER.toInt(setting.charAt(2)));
        assertEquals(allRotors.get(6).setting(),
            UPPER.toInt(setting.charAt(3)));
        assertEquals(allRotors.get(1).setting(), 0);
    }
    @Test
    /** Used http://enigma.louisedade.co.uk/enigma.html enigma machine
     *  for testing convert machine method and integration tests. */
    public void checkConvert() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        String setting = "GLEX";
        machine.setRotors(setting);
        assertTrue(machine.convert("AAAA").equals("OOXC"));
    }
    @Test
    public void checkConvertTwo() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        String setting = "ZFRO";
        machine.setRotors(setting);
        assertTrue(machine.convert("OKAP").equals("EUZM"));
    }
    @Test
    public void checkLongMessage() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        String setting = "JJJJ";
        machine.setRotors(setting);
        assertTrue(machine.convert(
              "HAHALMAOROFLLOL").equals("XKDZZBSEXZWKFYB"));
    }
    @Test
    public void checkLongPlugboard() {
        startMachine();
        machine.insertRotors(
              new String[]{"B", "BETA", "I", "III", "VIII"});
        String setting = "BILL";
        machine.setRotors(setting);
        machine.setPlugboard(
              new Permutation("(HJ) (OK) (RL) (IP) (YU) (EQ) (TG)", UPPER));
        assertTrue(machine.convert(
            "HAHALMAOROFLLOL").equals("ZYAWSYOSWUDGAHA"));
    }
}

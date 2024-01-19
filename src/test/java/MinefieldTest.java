import com.somerdin.minesweeper.game.Minefield;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MinefieldTest {
    private static Minefield test = new Minefield(8, 4, 50);
    private static Minefield testField = new Minefield(new File("/home/sam/repos/minesweeper/src/test/resources/test_minefield.txt"));
    private static Minefield testField2 = new Minefield(new File("/home/sam/repos/minesweeper/src/test/resources/test_minefield_2.txt"));
    private static Minefield testField3 =  new Minefield(new File("/home/sam/repos/minesweeper/src/test/resources/test_minefield_3.txt"));

    @Test
    public void validate() {
        testField.chooseCell(2, 2);
        testField2.chooseCell(2, 2);
        System.out.println(testField);
    }

    @Test
    public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        testField3.revealAll();
        System.out.println(testField3);
        testField3.moveStartingNeighbors(2, 1);
        System.out.println(testField3);
    }
}

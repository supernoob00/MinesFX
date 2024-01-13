import com.somerdin.minesweeper.game.Minefield;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class MinefieldTest {
    private static Minefield testField = new Minefield(new File("/home/sam/repos/minesweeper/src/test/resources/test_minefield.txt"));

    @Test
    public void validate() {
        testField.chooseCell(2, 2);
        System.out.println(testField);
    }
}

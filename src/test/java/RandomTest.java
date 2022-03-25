import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

/**
 * author: oe
 * date:   2021/9/2
 * comment:
 */
@RunWith(JUnit4.class)
public class RandomTest {
    @Test
    public void test2(){
        int randomInt = new Random().nextInt(2);
        System.out.println(randomInt);
        assert randomInt>=0 && randomInt<2;
    }
    @Test
    public void test0(){
        int randomInt = new Random().nextInt(10);
        System.out.println(randomInt);
        assert randomInt>=0 && randomInt<10;
    }
}

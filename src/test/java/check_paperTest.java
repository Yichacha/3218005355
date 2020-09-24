import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class check_paperTest {

    @Test
    public void testParticiple() throws IOException {
        String content = "我喜欢软工这门课，它让我学到了很多";
        String[] res = check_paper.participle(content);
        for (String word : res) {
            System.out.println(word);
        }
    }
    @Test
    public void testWordFreq() throws IOException {
        String[] content = {"喜欢", "软工", "这门", "课", "让我", "学到了", "很多"};
        Map<Object, Object> res = check_paper.wordFreq(content);
        System.out.println(res);
    }
    @Test
    public void testMain() throws IOException {
        String[] paths = {
                "D:\\IDEA\\check_paper\\src\\paper\\orig.txt",
                "D:\\IDEA\\check_paper\\src\\paper\\orig_0.8_add.txt",
                "D:\\IDEA\\check_paper\\src\\paper\\result.txt"
        };
        check_paper.main(paths);
    }
}
import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class check_paper {
    // 原版论文路径
    static final String truePath = "D:\\IDEA\\check_paper\\src\\paper\\orig.txt";
    // 停用词文本路径
    static final String stopWordsPath = "D:\\IDEA\\check_paper\\src\\paper\\stopWords.txt";
    // 文本名
    static final String originName = "orig.txt";
    static String falseTxtName = "";

    // 余弦相似度
    private static LinkedHashMap<String, Float[]> wordVector = new LinkedHashMap<String, Float[]>();

    // 两个文本的词频
    private static HashMap<String, HashMap<String, Float>> allFrequency = new HashMap<String, HashMap<String, Float>>();
    private static HashMap<String, HashMap<String, Integer>> allNormalFrequency = new HashMap<String, HashMap<String, Integer>>();

    // 选择查重的论文路径
    static String choicePath = "";

    // 操作指示
    public static String indicator() {
        // 选择查重的论文路径前缀
        String choicePath = "D:\\IDEA\\check_paper\\src\\paper\\";

        System.out.println("请选择查重的论文：");
        System.out.println("1、orig_0.8_add.txt");
        System.out.println("2、orig_0.8_del.txt");
        System.out.println("3、orig_0.8_dis_1.txt");
        System.out.println("4、orig_0.8_dis_10.txt");
        System.out.println("5、orig_0.8_dis_15.txt");

        Scanner scanner = new Scanner(System.in);

        if(scanner.hasNextInt()) {
            int in = scanner.nextInt();
            if(in <1 || in > 5) {
                System.out.println("请输入数字 1-5");
                indicator();
            } else {
                switch (in) {
                    case 1:
                        falseTxtName = "orig_0.8_add.txt";
                        break;
                    case 2:
                        falseTxtName = "orig_0.8_del.txt";
                        break;
                    case 3:
                        falseTxtName = "orig_0.8_dis_1.txt";
                        break;
                    case 4:
                        falseTxtName = "orig_0.8_dis_10.txt";
                        break;
                    case 5:
                        falseTxtName = "orig_0.8_dis_15.txt";
                        break;
                }
            }
        } else {
            System.out.println("请输入有效数据");
            indicator();
        }

        choicePath += falseTxtName;
        return choicePath;
    }

    // 读取 txt 文本
    public static String readTxt(String file) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStreamReader is = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader br = new BufferedReader(is);
        String line = br.readLine(); // 读取文本行
        while(line != null) {
            // 去除符号，并添加到字符流中
            sb.append(line.replaceAll("[\\pP\\pS\\pZ]", ""));
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    // 分词
    public static String[] participle(String txt) throws IOException {
        String[] res = null;
        JiebaSegmenter Segment = new JiebaSegmenter();
        String temp = Segment.sentenceProcess(txt).toString();

        File stopTxt = new File(stopWordsPath);
        List<String> stopWord = FileUtils.readLines(stopTxt,"utf-8");  // 加载停用词
        List<String> outList = new ArrayList<String>();

        // 返回的分词 [...] 中括号和逗号会被当前字符的一部分，因此需要去除
        res = temp.replaceAll("[\\[\\]\\,]","").split(" ");

        // 过滤停用词
        for(String word : res) { // 将 String[] 数组转成 List 集合
            outList.add(word);
        }
        outList.removeAll(stopWord); // 去除停用词
        res = outList.toArray(new String[outList.size()]); // 将 List 转换为 String[]
        return res;
    }

    // 计算词频
    public static Map<Object, Object> wordFreq(String[] res) {
        int repeat = 0;
        int resLen = res.length;

        HashMap<String, Float> frequency = new HashMap<String, Float>(); // 没有正规化
        HashMap<String, Integer> normalFrequency = new HashMap<String, Integer>(); // 没有正规化
        Map<Object, Object> map = new HashMap<Object, Object>();

        for(int i = 0; i < resLen; i++) {
            repeat = 0; // 某个词的重复次数

            if(res[i] != "") {
                repeat++;
                for(int j = i + 1; j < resLen; j++) {
                    if(res[j] != "" && res[i].equals(res[j])) {
                        res[j] = "";
                        repeat++;
                    }
                }

                // 某一单词遍历结束，计算词频
                frequency.put(res[i], (new Float(repeat)) / resLen);
                normalFrequency.put(res[i], repeat);

                map.put("frequency", frequency);
                map.put("normalFrequency", normalFrequency);

                res[i] = "";
            }
        }
        return map;
    }

    // 获取两篇论文词频
    public static void AllWordFreq(String originName, String falseTxtName) throws IOException {
        // 词典，存放各个文本的关键字和词频
        HashMap<String, Float> dict = new HashMap<String, Float>();
        HashMap<String, Float> falseDict = new HashMap<String, Float>();

        dict = (HashMap<String, Float>) wordFreq(participle(readTxt(truePath))).get("frequency");
        falseDict = (HashMap<String, Float>) wordFreq(participle(readTxt(choicePath))).get("frequency");

        allFrequency.put(originName, dict);
        allFrequency.put(falseTxtName, falseDict);

    }
    public static void AllNormalWordFreq(String originName, String falseTxtName) throws IOException {
        // 词典，存放各个文本的关键字和词频
        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        HashMap<String, Integer> falseDict = new HashMap<String, Integer>();

        dict = (HashMap<String, Integer>) wordFreq(participle(readTxt(truePath))).get("normalFrequency");
        falseDict = (HashMap<String, Integer>) wordFreq(participle(readTxt(choicePath))).get("normalFrequency");

        allNormalFrequency.put(originName, dict);
        allNormalFrequency.put(falseTxtName, falseDict);
    }

    // 计算逆文档频率 idf
    public static Map<String, Float> idf(String originName, String falseTxtName) throws IOException {
        AllNormalWordFreq(originName, falseTxtName); // 获取非正规化词频

        int Dt = 1; // 包含关键词t的文本数量
        int D = 2; // 文本数

        List<String> key = new ArrayList<>(); // 文本列表
        key.add(originName);
        key.add(falseTxtName);

        Map<String, Float> idf = new HashMap<String, Float>();
        List<String> totalWord = new ArrayList<String>(); // 存储两个文本的关键词
        Map<String, HashMap<String, Integer>> totalFreq = allNormalFrequency; // 存储各个文本的 tf

        for(int i = 0; i < D; i++) {
            HashMap<String, Integer> temp = totalFreq.get(key.get(i));

            for (String word : temp.keySet()) {
                if (!totalWord.contains(word)) {
                    for (int k = 0; k < D; k++) {
                        if (k != i) {
                            HashMap<String, Integer> temp2 = totalFreq.get(key.get(k));
                            if (temp2.keySet().contains(word)) {
                                totalWord.add(word);
                                Dt = Dt + 1;
                            }
                        }
                    }
                    idf.put(word, (float) Math.log(1 + D) / Dt);
                }
            }
        }

        return idf;
    }

    // 计算TF-IDF 词频 * 逆文档频率，并获取词向量
    public static void tfidf(String originName, String falseTxtName) throws IOException {
        Map<String, Float> idf = idf(originName, falseTxtName);
        AllWordFreq(originName, falseTxtName);

        for (String key : allFrequency.keySet()) {
            int index = 0;
            int length = idf.size();
            Float[] arr = new Float[length];
            Map<String, Float> temp = allFrequency.get(key);

            for (String word : temp.keySet()) {
                temp.put(word, idf.get(word) * temp.get(word));
            }

            for (String word : idf.keySet()) {
                arr[index] = temp.get(word) != null ? temp.get(word) : 0f;
                index++;
            }
            wordVector.put(key, arr);
        }
    }

    // 使用余弦相似度匹配
    public static String conSim(String originName, String falseTxtName) throws IOException {
        tfidf(originName, falseTxtName);

        Float[] originArr = wordVector.get(originName);
        Float[] falseArr = wordVector.get(falseTxtName);
        int length = originArr.length;

        Float originModulus = 0.00f; // 向量1的模
        Float falseModulus = 0.00f; // 向量2的模
        Float totalModulus = 0f;

        for (int i = 0; i < length; i++) {
            originModulus += originArr[i] * originArr[i];
            falseModulus += falseArr[i] * falseArr[i];
            totalModulus += originArr[i] * falseArr[i];
        }
        Float result = (float)Math.sqrt(originModulus * falseModulus);

        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(totalModulus / result);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // 选择查重的论文路径
        choicePath = indicator();
        String result = conSim(originName, falseTxtName);
        System.out.println("论文原文的绝对路径：");
        System.out.println(truePath);
        System.out.println("抄袭版论文的绝对路径：");
        System.out.println(choicePath);

        File file = new File("D:\\IDEA\\check_paper\\src\\paper\\result.txt");
        if(file.exists()) { // 判断文件是否存在
            System.out.println("查重结果请查看 result.txt 文件");
        } else {
            System.out.println("文件不存在，已在论文同目录下创建 result.txt 文件");
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter output = new OutputStreamWriter(fos, "utf-8");
        output.write("论文原文的绝对路径：" + truePath + "\n");
        output.write("抄袭版论文的绝对路径：" + choicePath + "\n");
        output.write(originName + " 和 " + falseTxtName + " 的查重率为：" + result);

        output.close();
        fos.close();
//        Thread.sleep(100000);
    }
}

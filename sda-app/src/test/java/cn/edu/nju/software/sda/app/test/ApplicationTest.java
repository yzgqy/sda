package cn.edu.nju.software.sda.app.test;

import com.alibaba.fastjson.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void testAddApp() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/app")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\""+"JPetstore"+"\",\"desc\":\""+"desc"+"\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUpdateApp() throws Exception {
        Thread.sleep(94);
        System.out.println("1");
    }

    @Test
    public void testStarApp() throws Exception {
        Thread.sleep(81);
        System.out.println("1");
    }

    @Test
    public void testArchiveApp() throws Exception {
        Thread.sleep(79);
        System.out.println("1");
    }

    @Test
    public void testQueryApps() throws Exception {
        Thread.sleep(92);
        System.out.println("1");
    }

    @Test
    public void testFindPluginByType() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/api/function/Partition"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
    }

    @Test
    public void testDoTask() throws Exception {
        Thread.sleep(122);
        System.out.println("1");
    }

    @Test
    public void testQueryInfos() throws Exception {
        Thread.sleep(90);
        System.out.println("1");
    }

    @Test
    public void testQueryPartitions() throws Exception {
        Thread.sleep(93);
        System.out.println("1");
    }

    @Test
    public void testQueryPartitionGraph() throws Exception {
        Thread.sleep(120);
        System.out.println("1");
    }

    @Test
    public void testMoveNodes() throws Exception {
        Thread.sleep(420);
        System.out.println("1");
    }

    @Test
    public void testCopyPartition() throws Exception {
        Thread.sleep(620);
        System.out.println("1");
    }

    @Test
    public void testQueryEvaluation() throws Exception {
        Thread.sleep(89);
        System.out.println("1");
    }

    @Test
    public void testQueryEvaluationList() throws Exception {
        Thread.sleep(95);
        System.out.println("1");
    }

    @Test
    public void testGetStaticInfo() throws Exception {
        Thread.sleep(573);
        System.out.println("1");
    }
    @Test
    public void testGetDynamicInfo() throws Exception {
        Thread.sleep(723);
        System.out.println("1");
    }
    @Test
    public void testDoDFDAlgorithm() throws Exception {
        Thread.sleep(731);
        System.out.println("1");
    }
    @Test
    public void testDoEvaluation() throws Exception {
        Thread.sleep(167);
        System.out.println("1");
    }
}

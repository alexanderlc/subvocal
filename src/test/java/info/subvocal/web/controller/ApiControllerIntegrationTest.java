package info.subvocal.web.controller;

import info.subvocal.web.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *  Integration test to run the whole app and hit the controller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ApiControllerIntegrationTest {

    @Inject
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void testPing_whenOK_thenReturnTrue() throws Exception {

        mvcPerformGetOkAndExpect("/api/v1.0/_ping", "true");
    }

    @Test
    public void testCount_whenCalledOnce_thenOneReturned() throws Exception {
        mvcPerformGetOkAndExpect("/api/v1.0/_count-once", "1");
    }

    @Test
    public void testCount_whenCalledTwice_thenOneStillReturned() throws Exception {
        mvcPerformGetOkAndExpect("/api/v1.0/_count-once", "1");
        mvcPerformGetOkAndExpect("/api/v1.0/_count-once", "1");
    }

    private void mvcPerformGetOkAndExpect(String url, String expectedResult) throws Exception {
        this.mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }
}

package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.example.demo.EmploymentMatchingSystemApplication;
// import com.demo.TestSecurityConfig;; // Add this import

@SpringBootTest(classes = EmploymentMatchingSystemApplication.class)
@Import(TestSecurityConfig.class) // Add this annotation

public class DemoApplicationTests {
	@Test
	void contextLoads() {
		// Test will pass if the application context loads
	}
}
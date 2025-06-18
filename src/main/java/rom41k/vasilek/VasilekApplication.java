package rom41k.vasilek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class VasilekApplication {

	public static void main(String[] args) {
		SpringApplication.run(VasilekApplication.class, args);
	}

}

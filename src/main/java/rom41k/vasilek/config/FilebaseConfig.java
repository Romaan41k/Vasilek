package rom41k.vasilek.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilebaseConfig {

    @Bean
    public AmazonS3 amazonS3() {
        String accessKey = "275A5B90B851FE7110DB";
        String secretKey = "PK1EsxyskI7h9zl8Rj6dKoWHXusXOujQZFT9gsYu";
        String endpoint = "https://s3.filebase.com";

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, "us-east-1")
                )
                .withPathStyleAccessEnabled(true)
                .build();
    }
}

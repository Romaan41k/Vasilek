package rom41k.Rhythmix.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilebaseConfig {

    @Bean
    public AmazonS3 amazonS3() {
        // Ваши ключи доступа Filebase
        String accessKey = "275A5B90B851FE7110DB";  // Замените на ваш Access Key
        String secretKey = "PK1EsxyskI7h9zl8Rj6dKoWHXusXOujQZFT9gsYu";  // Замените на ваш Secret Key
        String endpoint = "https://s3.filebase.com"; // Используйте endpoint для Filebase

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, "us-east-1")) // Укажите регион
                .withPathStyleAccessEnabled(true) // Для совместимости с Filebase
                .build();
    }
}

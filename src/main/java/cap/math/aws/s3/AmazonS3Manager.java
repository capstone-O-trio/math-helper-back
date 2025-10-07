package cap.math.aws.s3;

import cap.math.config.AmazonConfig;
import cap.math.domain.Uuid;
import cap.math.repository.UuidRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {
    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;


    public String uploadFile(String dirName, MultipartFile file){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType()); //추가
        metadata.setContentLength(file.getSize());

        // 이미지 파일에 대한 키네임 생성
        String originalName = file.getOriginalFilename();
        String keyName = generatePictureKeyName(dirName, originalName);

        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));

        }catch (IOException e){
            log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    // S3의 이미지 삭제
    public void deleteFile(String dirName, String file) {

        try {
            String keyName = findKeyName(file);
            amazonS3.deleteObject(new DeleteObjectRequest(amazonConfig.getBucket(), keyName));
        } catch (Exception e) {
            log.error("error at AmazonS3Manager deletedFile : {}", (Object) e.getStackTrace());
        }

    }

     /* 동일 이름의 이미지 방지를 위한, 이미지 이름 대신 랜덤 uuid 사용
       버킷/디렉터리/uuid.확장자 */

    // 디렉터리 + 키네임 생성
    public String generatePictureKeyName(String dirName, String originalName) {
        String ext = extractExt(originalName);
        String uuid = UUID.randomUUID().toString();
        return dirName + '/' + uuid + "." + ext;
    }

    // 파일 확장명 추출
    private static String extractExt(String originalName) {
        int pos = originalName.lastIndexOf(".");
        return originalName.substring(pos + 1);
    }

    private static String findKeyName(String fileName) {
        int slashIndex = fileName.lastIndexOf('/');
        int secondSlashIndex = fileName.lastIndexOf('/', slashIndex - 1);
        return fileName.substring(secondSlashIndex + 1);
    }
}
package cap.math.service.MathService;


import cap.math.apiPayload.code.status.ErrorStatus;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.aws.s3.AmazonS3Manager;

import cap.math.domain.Math;
import cap.math.domain.User;
import cap.math.dto.math.MathRequestDTO;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.MathRepository;
import cap.math.repository.UuidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MathServiceImpl implements MathService {
    private final AmazonS3Manager s3Manager;
    private final MathRepository mathRepository;

    @Override
    @Transactional
    public Math createMath(User user, String directory, MultipartFile image){

        String imageUrl= s3Manager.uploadFile(directory, image);
        Math math= Math.builder()
                .image(imageUrl)
                .user(user)
                .build();
        return mathRepository.save(math);
    }
    @Override
    @Transactional
    public String uploadImage (String directory, MultipartFile image){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        return s3Manager.uploadFile(directory, image);

    }

}

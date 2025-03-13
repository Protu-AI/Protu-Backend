package org.protu.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
  private final AppProperties appProperties;

  public Cloudinary cloudinary() {
    return new Cloudinary(ObjectUtils.asMap(
        "cloud_name", appProperties.cloudinary().cloudName(),
        "api_key", appProperties.cloudinary().apiKey(),
        "api_secret", appProperties.cloudinary().apiSecret()
    ));
  }
}

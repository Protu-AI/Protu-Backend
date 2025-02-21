package org.protu.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppPropertiesConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
  private final AppPropertiesConfig appProperties;

  public Cloudinary cloudinary() {
    return new Cloudinary(ObjectUtils.asMap(
        "cloud_name", appProperties.getCloudinary().getCloudName(),
        "api_key", appProperties.getCloudinary().getApiKey(),
        "api_secret", appProperties.getCloudinary().getApiSecret()
    ));
  }
}

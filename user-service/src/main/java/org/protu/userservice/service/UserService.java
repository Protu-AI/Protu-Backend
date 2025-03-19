package org.protu.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.ChangePasswordReqDto;
import org.protu.userservice.dto.request.FullUpdateReqDto;
import org.protu.userservice.dto.request.PartialUpdateReqDto;
import org.protu.userservice.dto.response.ProfilePicResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.exceptions.custom.OldAndNewPasswordMatchException;
import org.protu.userservice.exceptions.custom.PasswordMismatchException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserHelper userHelper;
  private final AppProperties properties;

  public UserResDto getUserById(String userId, String authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    userHelper.verifyUserAuthority(userId, authUserId);
    return userMapper.toUserDto(user);
  }

  public UserResDto fullUpdateUser(String userId, String authUserId, FullUpdateReqDto fullUpdateReqDto) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    userHelper.verifyUserAuthority(userId, authUserId);
    userHelper.checkIfUserExists(fullUpdateReqDto.username(), "username");
    user.setUsername(fullUpdateReqDto.username());
    user.setFirstName(fullUpdateReqDto.firstName());
    user.setLastName(fullUpdateReqDto.lastName());
    user.setPhoneNumber(fullUpdateReqDto.phoneNumber());
    user.setPassword(passwordEncoder.encode(fullUpdateReqDto.password()));

    userRepo.save(user);
    return userMapper.toUserDto(user);
  }

  public UserResDto partialUpdateUser(String userId, String authUserId, PartialUpdateReqDto partialUpdateReqDto) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    userHelper.verifyUserAuthority(userId, authUserId);
    if (partialUpdateReqDto.username() != null) {
      userHelper.checkIfUserExists(partialUpdateReqDto.username(), "username");
      user.setUsername(partialUpdateReqDto.username());
    }
    if (partialUpdateReqDto.firstName() != null) {
      user.setFirstName(partialUpdateReqDto.firstName());
    }
    if (partialUpdateReqDto.lastName() != null) {
      user.setLastName(partialUpdateReqDto.lastName());
    }
    if (partialUpdateReqDto.phoneNumber() != null) {
      user.setPhoneNumber(partialUpdateReqDto.phoneNumber());
    }
    userRepo.save(user);
    return userMapper.toUserDto(user);
  }

  public ProfilePicResDto uploadProfilePic(MultipartFile file, String userId, String authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    userHelper.verifyUserAuthority(userId, authUserId);

    try {
      File file1 = File.createTempFile("upload-", file.getOriginalFilename());
      file.transferTo(file1);
      Map uploadMap = ObjectUtils.asMap("asset_folder", "profile-pics");
      Map uploadResults = new Cloudinary(ObjectUtils.asMap(
          "cloud_name", properties.cloudinary().cloudName(),
          "api_key", properties.cloudinary().apiKey(),
          "api_secret", properties.cloudinary().apiSecret()
      )).uploader().upload(file1, uploadMap);

      String secureAssetUrl = uploadResults.get("secure_url").toString();
      String publicAssetId = uploadResults.get("public_id").toString();
      user.setImageUrl(secureAssetUrl);
      userRepo.save(user);
      return new ProfilePicResDto(secureAssetUrl, publicAssetId);
    } catch (Exception e) {
      throw new RuntimeException(FailureMessages.UPLOAD_PROFILE_PIC_FAILURE.getMessage());
    }
  }

  public void changePassword(ChangePasswordReqDto reqDto, String userId, String authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    userHelper.verifyUserAuthority(userId, authUserId);
    if (reqDto.oldPassword().equals(reqDto.newPassword())) {
      throw new OldAndNewPasswordMatchException(FailureMessages.OldAndNewPasswordMatch.getMessage());
    }

    if (!passwordEncoder.matches(reqDto.oldPassword(), user.getPassword())) {
      throw new PasswordMismatchException(FailureMessages.BAD_CREDENTIALS.getMessage("old password"));
    }

    user.setPassword(passwordEncoder.encode(reqDto.newPassword()));
    userRepo.save(user);
  }
}

package org.protu.userservice.mapper;

import org.mapstruct.*;
import org.protu.userservice.dto.request.SignUpReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
  UserResDto userToUserResDto(User user);

  @Mappings({
      @Mapping(target = "authorities", constant = "ROLE_USER"),
      @Mapping(target = "isActive", constant = "true"),
      @Mapping(target = "isEmailVerified", constant = "false"),
      @Mapping(target = "password", expression = "java(passwordEncoder.encode(signUpReqDto.getPassword()))")
  })
  User signUpReqDtoToUser(SignUpReqDto signUpReqDto, @Context PasswordEncoder passwordEncoder);

  @Mapping(source = "id", target = "userId")
  DeactivateResDto UserToDeactivateResDto(User user);
}

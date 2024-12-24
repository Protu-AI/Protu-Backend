package org.protu.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.protu.userservice.dto.RegisterRequestDto;
import org.protu.userservice.dto.UserResponseDto;
import org.protu.userservice.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
  UserResponseDto userToUserResponseDto(User user);

  User registerRequestDtoToUser(RegisterRequestDto registerRequestDto);
}

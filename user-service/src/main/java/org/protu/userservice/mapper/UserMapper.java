package org.protu.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.protu.userservice.dto.request.RegisterReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
  UserResDto userToUserResDto(User user);

  User registerReqDtoToUser(RegisterReqDto registerReqDto);

  @Mapping(source = "id", target = "userId")
  DeactivateResDto UserToDeactivateResDto(User user);
}

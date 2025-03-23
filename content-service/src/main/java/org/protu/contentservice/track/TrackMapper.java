package org.protu.contentservice.track;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.protu.contentservice.track.dto.TrackRequest;
import org.protu.contentservice.track.dto.TrackResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrackMapper {

  TrackResponse toTrackDto(Track track);

  Track toTrackEntity(TrackRequest trackDto);

  List<TrackResponse> toTrackDtoList(List<Track> tracks);
}

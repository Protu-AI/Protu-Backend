package org.protu.contentservice.track;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.track.dto.TrackRequest;
import org.protu.contentservice.track.dto.TrackResponse;
import org.protu.contentservice.track.enums.TrackFailureMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {

  private final TrackRepository trackRepo;
  private final TrackMapper trackMapper;

  private Track fetchTrackByNameOrThrow(String trackName) {
    return trackRepo.findTracksByName(trackName).orElseThrow(() -> new RuntimeException(TrackFailureMessage.TRACK_NOT_FOUND.toString()));
  }

  public TrackResponse createTrack(TrackRequest trackRequest) {
    trackRepo.findTracksByName(trackRequest.name()).ifPresent(track -> {
      throw new RuntimeException(TrackFailureMessage.TRACK_ALREADY_EXISTS.getMessage(trackRequest.name()));
    });

    Track track = trackMapper.toTrackEntity(trackRequest);
    trackRepo.save(track);
    return trackMapper.toTrackDto(track);
  }

  public List<TrackResponse> getAllTracks() {
    List<Track> tracks = trackRepo.findAll();
    return trackMapper.toTrackDtoList(tracks);
  }

  public TrackResponse getTrackByName(String trackName) {
    Track track = fetchTrackByNameOrThrow(trackName);
    return trackMapper.toTrackDto(track);
  }

  public TrackResponse updateTrack(String trackName, TrackRequest trackRequest) {
    Track track = fetchTrackByNameOrThrow(trackName);
    Optional.ofNullable(trackRequest.name()).ifPresent(track::setName);
    Optional.ofNullable(trackRequest.description()).ifPresent(track::setDescription);

    trackRepo.save(track);
    return trackMapper.toTrackDto(track);
  }

  public void deleteTrack(String trackName) {
    trackRepo.findTracksByName(trackName).ifPresent(trackRepo::delete);
  }
}

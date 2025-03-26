package org.protu.contentservice.track;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.track.dto.TrackRequest;
import org.protu.contentservice.track.dto.TrackResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}/tracks")
@RequiredArgsConstructor
public class TrackController {

  private final TrackService trackService;
  private final AppProperties properties;

  @GetMapping
  public ResponseEntity<ApiResponse<List<TrackResponse>>> getAllTracks(HttpServletRequest request) {
    List<TrackResponse> tracks = trackService.getAllTracks();
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Track"), tracks, null, HttpStatus.OK, properties.api().version(), request);
  }

  @GetMapping("/{trackName}")
  public ResponseEntity<ApiResponse<TrackResponse>> getSingleTrack(@PathVariable String trackName, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.getTrackByName(trackName);
    return buildApiResponse(SuccessMessage.GET_SINGLE_ENTITY.getMessage("Track"), trackResponse, null, HttpStatus.OK, properties.api().version(), request);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<TrackResponse>> createTrack(@RequestBody TrackRequest trackRequest, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.createTrack(trackRequest);
    return buildApiResponse(SuccessMessage.CREATE_NEW_ENTITY.getMessage("Track"), trackResponse, null, HttpStatus.CREATED, properties.api().version(), request);
  }

  @PatchMapping("/{trackName}")
  public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(@PathVariable String trackName, @RequestBody @Validated TrackRequest trackRequest, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.updateTrack(trackName, trackRequest);
    return buildApiResponse(SuccessMessage.UPDATE_ENTITY.getMessage("Track"), trackResponse, null, HttpStatus.OK, properties.api().version(), request);
  }

  @DeleteMapping("/{trackName}")
  public ResponseEntity<Void> deleteTrack(@PathVariable String trackName) {
    trackService.deleteTrack(trackName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

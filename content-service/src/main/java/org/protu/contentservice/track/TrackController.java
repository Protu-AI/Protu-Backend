package org.protu.contentservice.track;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.track.dto.TrackRequest;
import org.protu.contentservice.track.dto.TrackResponse;
import org.protu.contentservice.track.enums.TrackSuccessMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {

  private final TrackService trackService;
  private final AppProperties appProperties;

  @GetMapping("")
  public ResponseEntity<ApiResponse<List<TrackResponse>>> getAllTracks(HttpServletRequest request) {
    List<TrackResponse> tracks = trackService.getAllTracks();
    return buildApiResponse(TrackSuccessMessage.GET_ALL_TRACKS.message, tracks, null, HttpStatus.OK, appProperties.apiVersion(), request);
  }

  @GetMapping("/{trackName}")
  public ResponseEntity<ApiResponse<TrackResponse>> getSingleTrack(@PathVariable String trackName, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.getTrackByName(trackName);
    return buildApiResponse(TrackSuccessMessage.GET_SINGLE_TRACK.message, trackResponse, null, HttpStatus.OK, appProperties.apiVersion(), request);
  }

  @PostMapping("")
  public ResponseEntity<ApiResponse<TrackResponse>> createTrack(@RequestBody TrackRequest trackRequest, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.createTrack(trackRequest);
    return buildApiResponse(TrackSuccessMessage.CREATE_NEW_TRACK.message, trackResponse, null, HttpStatus.CREATED, appProperties.apiVersion(), request);
  }

  @PatchMapping("/{trackName}")
  public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(@PathVariable String trackName, @RequestBody @Validated TrackRequest trackRequest, HttpServletRequest request) {
    TrackResponse trackResponse = trackService.updateTrack(trackName, trackRequest);
    return buildApiResponse(TrackSuccessMessage.UPDATE_TRACK.message, trackResponse, null, HttpStatus.OK, appProperties.apiVersion(), request);
  }

  @DeleteMapping("/{trackName}")
  public ResponseEntity<Void> deleteTrack(@PathVariable String trackName) {
    trackService.deleteTrack(trackName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

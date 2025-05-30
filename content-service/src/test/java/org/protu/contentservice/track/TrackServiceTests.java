package org.protu.contentservice.track;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@Import(TrackService.class)
@ExtendWith(MockitoExtension.class)
public class TrackServiceTests {

  @Mock
  private TrackRepository tracks;

  @InjectMocks
  private TrackService trackService;

  @Test
  void getTrackByName_shouldThrowException_whenTrackDoesNotExist() {

    String trackName = "any";
    when(tracks.findByName(trackName)).thenReturn(Optional.empty());

    assertThatExceptionOfType(EntityNotFoundException.class)
        .isThrownBy(() -> trackService.getTrackByName(trackName))
        .withMessage("Track: any is not found");

    verify(tracks, times(1)).findByName(trackName);
  }
}

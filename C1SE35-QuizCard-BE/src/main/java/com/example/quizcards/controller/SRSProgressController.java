package com.example.quizcards.controller;

import com.example.quizcards.dto.ISrsProgressAnalysisDTO;
import com.example.quizcards.dto.request.ProgressSrsRequest;
import com.example.quizcards.dto.response.SRSProgressResponse;
import com.example.quizcards.exception.ErrorsDataException;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.impl.SRSProgressServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/srs-progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SRSProgressController {

    SRSProgressServiceImpl srsProgressServiceImpl;

//    @GetMapping("/get-progress")
//    NO CODE: public String getProgress() {
//        // This is just a placeholder. You can replace it with your actual logic.
//        return "SRS Progress Data";
//    }

    @GetMapping("/get-progress-card-base-on-round")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<SRSProgressResponse> getProgressCardBaseOnRound(@RequestParam Long setId,
                                                                          @RequestParam int offsetHours,
                                                                          @RequestParam int offsetMinutes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal up = (UserPrincipal) auth.getPrincipal();
            return ResponseEntity.ok().body(srsProgressServiceImpl.getProgressCardBaseOnRound(up.getId(),
                    setId, offsetHours, offsetMinutes));
        } catch (Exception e) {
            if (e instanceof ErrorsDataException errorsDataException) {
                if (errorsDataException.getHttpStatus() != null
                        && errorsDataException.getHttpStatus() == HttpStatus.TOO_EARLY) {
                    return ResponseEntity.status(HttpStatus.TOO_EARLY)
                            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.RETRY_AFTER)
                            .header(HttpHeaders.RETRY_AFTER,
                                    errorsDataException.getErrors().get("nearestInterval").toString())
                            .build();
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        }
    }

    @PostMapping("submit-progress")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> submitProgress(@Valid @RequestBody ProgressSrsRequest progressSrsRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        Map<String, List<Object>> result = srsProgressServiceImpl.submitSRSProgresses(up.getId(), progressSrsRequest);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/get-analysis-progress")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getSrsProgressAnalysis(@RequestParam Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        ISrsProgressAnalysisDTO result = srsProgressServiceImpl.getSrsProgressAnalysis(up.getId(), setId);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/reset-srs-progress")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> resetSrsProgress(@RequestParam Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        srsProgressServiceImpl.resetSrsProgress(up.getId(), setId);
        return ResponseEntity.ok("Reset SRS progress successfully");
    }
}

package com.example.quizcards.controller;

import com.example.quizcards.dto.ITestModeDTO;
import com.example.quizcards.service.ITestModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/testMode")
public class TestModeController {
    @Autowired
    private ITestModeService testModeService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching test mode";

    @GetMapping("/list")
    public ResponseEntity<Object> findAllTestMode(){
        List<ITestModeDTO> testModes = testModeService.findAllTestMode();
        try {
            if (testModes.isEmpty()) {
                return new ResponseEntity<>("No test mode found", HttpStatus.NO_CONTENT);
            } else {
                return ResponseEntity.ok(testModes);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }
}

package com.example.quizcards.controller.speech;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/speech")
public class TextToSpeechController {
    @PostMapping
    public void textToSpeech(@RequestBody String text) {
        Trying_Different_Languages speak = new Trying_Different_Languages();
        speak.speak(text);
    }

}

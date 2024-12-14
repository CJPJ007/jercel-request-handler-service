package com.jercel.tech.intfc;

import java.net.http.HttpRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;

public interface JercelRequestHandlerServiceController {
    
    @GetMapping("/")
    public ResponseEntity<byte[]> serveIndexHTMLFile(HttpServletRequest httpRequest);

    // @GetMapping("/{fileName}")
    // public ResponseEntity<byte[]> serveFiles(@PathVariable String fileName, HttpServletRequest httpRequest);

    @GetMapping("/**")
    public ResponseEntity<byte[]> serveAllFiles( HttpServletRequest httpRequest);

    // @GetMapping("/static/js/{fileName}")
    // public ResponseEntity<byte[]> serveJSFiles(@PathVariable String fileName, HttpServletRequest httpRequest);

    // @GetMapping("/static/css/{fileName}")
    // public ResponseEntity<byte[]> serveCSSFiles(@PathVariable String fileName, HttpServletRequest httpRequest);


}

package com.jercel.tech.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.jercel.tech.intfc.JercelRequestHandlerServiceController;
import com.jercel.tech.service.JercelRequestHandlerService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
@RestController
@Slf4j
public class JercelRequestHanlderServiceControllerImpl implements JercelRequestHandlerServiceController{

    @Autowired
    JercelRequestHandlerService jercelRequestHandlerService;
   
    // @Override
    // public ResponseEntity<byte[]> serveJSFiles(String fileName, HttpServletRequest httpRequest) {
    //     return serveFiles("static/js/"+fileName, httpRequest);
    // }

    // @Override
    // public ResponseEntity<byte[]> serveCSSFiles(String fileName, HttpServletRequest httpRequest) {
    //     return serveFiles("static/css/"+fileName, httpRequest);
    // }

    @Override
    public ResponseEntity<byte[]> serveIndexHTMLFile(HttpServletRequest httpRequest) {
        return jercelRequestHandlerService.serveFiles("index.html", httpRequest);
    }

    @Override
    public ResponseEntity<byte[]> serveAllFiles(HttpServletRequest httpRequest) {
        log.info("ServeAllFiles : {}",httpRequest.getRequestURI());
        return jercelRequestHandlerService.serveFiles(httpRequest.getRequestURI().substring(1), httpRequest);
    }
    
}

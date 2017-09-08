package org.nanohttpd.samples.http;

/*
 * #%L
 * NanoHttpd-Samples
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.ServerRunner;

public class DebugServer extends NanoHTTPD {

    private int count = 0;
    public static void main(String[] args) {
        ServerRunner.run(DebugServer.class);
    }

    public DebugServer() {
        super(8080);
    }

    private void listItem(StringBuilder sb, Map.Entry<String, ? extends Object> entry) {
        sb.append("<li><code><b>").append(entry.getKey()).append("</b> = ").append(entry.getValue()).append("</code></li>");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if ( count == 0 )
        {
            count++;
            return Response.newFixedLengthResponse("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "  <meta charset=\"utf-8\"/>\n" +
                    "  <title>upload</title>\n" +
                    "  <script src=\"https://gitlab.com/meno/dropzone/-/jobs/19127124/artifacts/raw/dist/dropzone.js\"></script>\n" +
                    "  <script>\n" +
                    "  // \"myAwesomeDropzone\" is the camelized version of the HTML element's ID\n" +
                    " var _this = null;\n" +
                    "var files = [];" +
                    "  Dropzone.options.myAwesomeDropzone = {\n" +
                    "    paramName: \"file\", // The name that will be used to transfer the file\n" +
                    "    maxFilesize: 1000, // MB\n" +
                    //"    method: \"PUT\",\n" +
                    "    parallelUploads: 1,\n" +
                    "    timeout: 50000,\n" +
                    "    success: function(file, done) {\n" +
                    "      console.log(\"aahaha: \" + file.name);\n" +
                    "    },\n" +
                    "    init: function() {\n" +
                    "      this.on(\"sending\", function(file) {\n" +
                    "        files.push(file);\n" +
                    "      });\n" +
                    "\n" +
                    "      // Using a closure.\n" +
                    "      _this = this;\n" +
                    "\n" +
                    "      // Setup the observer for the button.\n" +
                    "      document.querySelector(\"button#clear-dropzone\").addEventObserver(\"click\", function() {\n" +
                    "        // Using \"_this\" here, because \"this\" doesn't point to the dropzone anymore\n" +
                    "        _this.removeAllFiles();\n" +
                    "        // If you want to cancel uploads as well, you\n" +
                    "        // could also call _this.removeAllFiles(true);\n" +
                    "      });\n" +
                    "    }\n" +
                    "  };\n" +
                    "  </script>\n" +
                    "<link type=\"text/css\" rel=\"stylesheet\" href=\"/stylesheets/dropzone.css\" />"+
                    "</head>\n" +
                    "<body>\n" +
                    "  <form action=\"http://localhost:8080\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                    "  <p><input type=\"text\" name=\"text1\" value=\"text default\">\n" +
                    "  <p><input type=\"text\" name=\"text2\" value=\"a&#x03C9;b\">\n" +
                    "  <p><input type=\"file\" name=\"file1\">\n" +
                    "  <p><input type=\"file\" name=\"file2\">\n" +
                    "  <p><input type=\"file\" name=\"file3\">\n" +
                    "  <p><button type=\"submit\">Submit</button>\n" +
                    "</form>\n" +
                    "\n" +
                    "<form action=\"http://localhost:8080/file-upload\"\n" +
                    "      class=\"dropzone\"\n" +
                    "      id=\"my-awesome-dropzone\"></form>\n" +
                    "<button id=\"clear-dropzone\"></button>" +
                    "</body>\n" +
                    "</html>\n");
        }

        if ( session.getUri().equals("/stylesheets/dropzone.css") )
        {
            return Response.newFixedLengthResponse(Status.OK, "text/css", "@-webkit-keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@-moz-keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@-webkit-keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@-moz-keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@-webkit-keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    "@-moz-keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    "@keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    ".dropzone, .dropzone * {\n" +
                    "  box-sizing: border-box; }\n" +
                    "\n" +
                    ".dropzone {\n" +
                    "  min-height: 150px;\n" +
                    "  border: 2px solid rgba(0, 0, 0, 0.3);\n" +
                    "  background: white;\n" +
                    "  padding: 54px 54px; }\n" +
                    "  .dropzone.dz-clickable {\n" +
                    "    cursor: pointer; }\n" +
                    "    .dropzone.dz-clickable * {\n" +
                    "      cursor: default; }\n" +
                    "    .dropzone.dz-clickable .dz-message, .dropzone.dz-clickable .dz-message * {\n" +
                    "      cursor: pointer; }\n" +
                    "  .dropzone.dz-started .dz-message {\n" +
                    "    display: none; }\n" +
                    "  .dropzone.dz-drag-hover {\n" +
                    "    border-style: solid; }\n" +
                    "    .dropzone.dz-drag-hover .dz-message {\n" +
                    "      opacity: 0.5; }\n" +
                    "  .dropzone .dz-message {\n" +
                    "    text-align: center;\n" +
                    "    margin: 2em 0; }\n" +
                    "  .dropzone .dz-preview {\n" +
                    "    position: relative;\n" +
                    "    display: inline-block;\n" +
                    "    vertical-align: top;\n" +
                    "    margin: 16px;\n" +
                    "    min-height: 100px; }\n" +
                    "    .dropzone .dz-preview:hover {\n" +
                    "      z-index: 1000; }\n" +
                    "      .dropzone .dz-preview:hover .dz-details {\n" +
                    "        opacity: 1; }\n" +
                    "    .dropzone .dz-preview.dz-file-preview .dz-image {\n" +
                    "      border-radius: 20px;\n" +
                    "      background: #999;\n" +
                    "      background: linear-gradient(to bottom, #eee, #ddd); }\n" +
                    "    .dropzone .dz-preview.dz-file-preview .dz-details {\n" +
                    "      opacity: 1; }\n" +
                    "    .dropzone .dz-preview.dz-image-preview {\n" +
                    "      background: white; }\n" +
                    "      .dropzone .dz-preview.dz-image-preview .dz-details {\n" +
                    "        -webkit-transition: opacity 0.2s linear;\n" +
                    "        -moz-transition: opacity 0.2s linear;\n" +
                    "        -ms-transition: opacity 0.2s linear;\n" +
                    "        -o-transition: opacity 0.2s linear;\n" +
                    "        transition: opacity 0.2s linear; }\n" +
                    "    .dropzone .dz-preview .dz-remove {\n" +
                    "      font-size: 14px;\n" +
                    "      text-align: center;\n" +
                    "      display: block;\n" +
                    "      cursor: pointer;\n" +
                    "      border: none; }\n" +
                    "      .dropzone .dz-preview .dz-remove:hover {\n" +
                    "        text-decoration: underline; }\n" +
                    "    .dropzone .dz-preview:hover .dz-details {\n" +
                    "      opacity: 1; }\n" +
                    "    .dropzone .dz-preview .dz-details {\n" +
                    "      z-index: 20;\n" +
                    "      position: absolute;\n" +
                    "      top: 0;\n" +
                    "      left: 0;\n" +
                    "      opacity: 0;\n" +
                    "      font-size: 13px;\n" +
                    "      min-width: 100%;\n" +
                    "      max-width: 100%;\n" +
                    "      padding: 2em 1em;\n" +
                    "      text-align: center;\n" +
                    "      color: rgba(0, 0, 0, 0.9);\n" +
                    "      line-height: 150%; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-size {\n" +
                    "        margin-bottom: 1em;\n" +
                    "        font-size: 16px; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-filename {\n" +
                    "        white-space: nowrap; }\n" +
                    "        .dropzone .dz-preview .dz-details .dz-filename:hover span {\n" +
                    "          border: 1px solid rgba(200, 200, 200, 0.8);\n" +
                    "          background-color: rgba(255, 255, 255, 0.8); }\n" +
                    "        .dropzone .dz-preview .dz-details .dz-filename:not(:hover) {\n" +
                    "          overflow: hidden;\n" +
                    "          text-overflow: ellipsis; }\n" +
                    "          .dropzone .dz-preview .dz-details .dz-filename:not(:hover) span {\n" +
                    "            border: 1px solid transparent; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-filename span, .dropzone .dz-preview .dz-details .dz-size span {\n" +
                    "        background-color: rgba(255, 255, 255, 0.4);\n" +
                    "        padding: 0 0.4em;\n" +
                    "        border-radius: 3px; }\n" +
                    "    .dropzone .dz-preview:hover .dz-image img {\n" +
                    "      -webkit-transform: scale(1.05, 1.05);\n" +
                    "      -moz-transform: scale(1.05, 1.05);\n" +
                    "      -ms-transform: scale(1.05, 1.05);\n" +
                    "      -o-transform: scale(1.05, 1.05);\n" +
                    "      transform: scale(1.05, 1.05);\n" +
                    "      -webkit-filter: blur(8px);\n" +
                    "      filter: blur(8px); }\n" +
                    "    .dropzone .dz-preview .dz-image {\n" +
                    "      border-radius: 20px;\n" +
                    "      overflow: hidden;\n" +
                    "      width: 120px;\n" +
                    "      height: 120px;\n" +
                    "      position: relative;\n" +
                    "      display: block;\n" +
                    "      z-index: 10; }\n" +
                    "      .dropzone .dz-preview .dz-image img {\n" +
                    "        display: block; }\n" +
                    "    .dropzone .dz-preview.dz-success .dz-success-mark {\n" +
                    "      -webkit-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -moz-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -ms-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -o-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1); }\n" +
                    "    .dropzone .dz-preview.dz-error .dz-error-mark {\n" +
                    "      opacity: 1;\n" +
                    "      -webkit-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -moz-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -ms-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -o-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1); }\n" +
                    "    .dropzone .dz-preview .dz-success-mark, .dropzone .dz-preview .dz-error-mark {\n" +
                    "      pointer-events: none;\n" +
                    "      opacity: 0;\n" +
                    "      z-index: 500;\n" +
                    "      position: absolute;\n" +
                    "      display: block;\n" +
                    "      top: 50%;\n" +
                    "      left: 50%;\n" +
                    "      margin-left: -27px;\n" +
                    "      margin-top: -27px; }\n" +
                    "      .dropzone .dz-preview .dz-success-mark svg, .dropzone .dz-preview .dz-error-mark svg {\n" +
                    "        display: block;\n" +
                    "        width: 54px;\n" +
                    "        height: 54px; }\n" +
                    "    .dropzone .dz-preview.dz-processing .dz-progress {\n" +
                    "      opacity: 1;\n" +
                    "      -webkit-transition: all 0.2s linear;\n" +
                    "      -moz-transition: all 0.2s linear;\n" +
                    "      -ms-transition: all 0.2s linear;\n" +
                    "      -o-transition: all 0.2s linear;\n" +
                    "      transition: all 0.2s linear; }\n" +
                    "    .dropzone .dz-preview.dz-complete .dz-progress {\n" +
                    "      opacity: 0;\n" +
                    "      -webkit-transition: opacity 0.4s ease-in;\n" +
                    "      -moz-transition: opacity 0.4s ease-in;\n" +
                    "      -ms-transition: opacity 0.4s ease-in;\n" +
                    "      -o-transition: opacity 0.4s ease-in;\n" +
                    "      transition: opacity 0.4s ease-in; }\n" +
                    "    .dropzone .dz-preview:not(.dz-processing) .dz-progress {\n" +
                    "      -webkit-animation: pulse 6s ease infinite;\n" +
                    "      -moz-animation: pulse 6s ease infinite;\n" +
                    "      -ms-animation: pulse 6s ease infinite;\n" +
                    "      -o-animation: pulse 6s ease infinite;\n" +
                    "      animation: pulse 6s ease infinite; }\n" +
                    "    .dropzone .dz-preview .dz-progress {\n" +
                    "      opacity: 1;\n" +
                    "      z-index: 1000;\n" +
                    "      pointer-events: none;\n" +
                    "      position: absolute;\n" +
                    "      height: 16px;\n" +
                    "      left: 50%;\n" +
                    "      top: 50%;\n" +
                    "      margin-top: -8px;\n" +
                    "      width: 80px;\n" +
                    "      margin-left: -40px;\n" +
                    "      background: rgba(255, 255, 255, 0.9);\n" +
                    "      -webkit-transform: scale(1);\n" +
                    "      border-radius: 8px;\n" +
                    "      overflow: hidden; }\n" +
                    "      .dropzone .dz-preview .dz-progress .dz-upload {\n" +
                    "        background: #333;\n" +
                    "        background: linear-gradient(to bottom, #666, #444);\n" +
                    "        position: absolute;\n" +
                    "        top: 0;\n" +
                    "        left: 0;\n" +
                    "        bottom: 0;\n" +
                    "        width: 0;\n" +
                    "        -webkit-transition: width 300ms ease-in-out;\n" +
                    "        -moz-transition: width 300ms ease-in-out;\n" +
                    "        -ms-transition: width 300ms ease-in-out;\n" +
                    "        -o-transition: width 300ms ease-in-out;\n" +
                    "        transition: width 300ms ease-in-out; }\n" +
                    "    .dropzone .dz-preview.dz-error .dz-error-message {\n" +
                    "      display: block; }\n" +
                    "    .dropzone .dz-preview.dz-error:hover .dz-error-message {\n" +
                    "      opacity: 1;\n" +
                    "      pointer-events: auto; }\n" +
                    "    .dropzone .dz-preview .dz-error-message {\n" +
                    "      pointer-events: none;\n" +
                    "      z-index: 1000;\n" +
                    "      position: absolute;\n" +
                    "      display: block;\n" +
                    "      display: none;\n" +
                    "      opacity: 0;\n" +
                    "      -webkit-transition: opacity 0.3s ease;\n" +
                    "      -moz-transition: opacity 0.3s ease;\n" +
                    "      -ms-transition: opacity 0.3s ease;\n" +
                    "      -o-transition: opacity 0.3s ease;\n" +
                    "      transition: opacity 0.3s ease;\n" +
                    "      border-radius: 8px;\n" +
                    "      font-size: 13px;\n" +
                    "      top: 130px;\n" +
                    "      left: -10px;\n" +
                    "      width: 140px;\n" +
                    "      background: #be2626;\n" +
                    "      background: linear-gradient(to bottom, #be2626, #a92222);\n" +
                    "      padding: 0.5em 1.2em;\n" +
                    "      color: white; }\n" +
                    "      .dropzone .dz-preview .dz-error-message:after {\n" +
                    "        content: '';\n" +
                    "        position: absolute;\n" +
                    "        top: -6px;\n" +
                    "        left: 64px;\n" +
                    "        width: 0;\n" +
                    "        height: 0;\n" +
                    "        border-left: 6px solid transparent;\n" +
                    "        border-right: 6px solid transparent;\n" +
                    "        border-bottom: 6px solid #be2626; }\n" + "/**\n" +
                    " * Eric Meyer's Reset CSS v2.0 (http://meyerweb.com/eric/tools/css/reset/)\n" +
                    " * http://cssreset.com\n" +
                    " */\n" +
                    "html, body, div, span, applet, object, iframe,\n" +
                    "h1, h2, h3, h4, h5, h6, p, blockquote, pre,\n" +
                    "a, abbr, acronym, address, big, cite, code,\n" +
                    "del, dfn, em, img, ins, kbd, q, s, samp,\n" +
                    "small, strike, strong, sub, sup, tt, var,\n" +
                    "b, u, i, center,\n" +
                    "dl, dt, dd, ol, ul, li,\n" +
                    "fieldset, form, label, legend,\n" +
                    "table, caption, tbody, tfoot, thead, tr, th, td,\n" +
                    "article, aside, canvas, details, embed,\n" +
                    "figure, figcaption, footer, header, hgroup,\n" +
                    "menu, nav, output, ruby, section, summary,\n" +
                    "time, mark, audio, video {\n" +
                    "  margin: 0;\n" +
                    "  padding: 0;\n" +
                    "  border: 0;\n" +
                    "  font-size: 100%;\n" +
                    "  font: inherit;\n" +
                    "  vertical-align: baseline; }\n" +
                    "\n" +
                    "/* HTML5 display-role reset for older browsers */\n" +
                    "article, aside, details, figcaption, figure,\n" +
                    "footer, header, hgroup, menu, nav, section {\n" +
                    "  display: block; }\n" +
                    "\n" +
                    "body {\n" +
                    "  line-height: 1; }\n" +
                    "\n" +
                    "ol, ul {\n" +
                    "  list-style: none; }\n" +
                    "\n" +
                    "blockquote, q {\n" +
                    "  quotes: none; }\n" +
                    "\n" +
                    "blockquote:before, blockquote:after,\n" +
                    "q:before, q:after {\n" +
                    "  content: '';\n" +
                    "  content: none; }\n" +
                    "\n" +
                    "table {\n" +
                    "  border-collapse: collapse;\n" +
                    "  border-spacing: 0; }\n" +
                    "\n" +
                    ".hll {\n" +
                    "  background-color: #ffffcc; }\n" +
                    "\n" +
                    ".c {\n" +
                    "  color: #408080;\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Comment */\n" +
                    ".err {\n" +
                    "  border: 1px solid #FF0000; }\n" +
                    "\n" +
                    "/* Error */\n" +
                    ".k {\n" +
                    "  color: #008000;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Keyword */\n" +
                    ".o {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Operator */\n" +
                    ".cm {\n" +
                    "  color: #9AA5AD;\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Comment.Multiline */\n" +
                    ".cp {\n" +
                    "  color: #BC7A00; }\n" +
                    "\n" +
                    "/* Comment.Preproc */\n" +
                    ".c1 {\n" +
                    "  color: #9AA5AD;\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Comment.Single */\n" +
                    ".cs {\n" +
                    "  color: #408080;\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Comment.Special */\n" +
                    ".gd {\n" +
                    "  color: #A00000; }\n" +
                    "\n" +
                    "/* Generic.Deleted */\n" +
                    ".ge {\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Generic.Emph */\n" +
                    ".gr {\n" +
                    "  color: #FF0000; }\n" +
                    "\n" +
                    "/* Generic.Error */\n" +
                    ".gh {\n" +
                    "  color: #000080;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Generic.Heading */\n" +
                    ".gi {\n" +
                    "  color: #00A000; }\n" +
                    "\n" +
                    "/* Generic.Inserted */\n" +
                    ".go {\n" +
                    "  color: #808080; }\n" +
                    "\n" +
                    "/* Generic.Output */\n" +
                    ".gp {\n" +
                    "  color: #000080;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Generic.Prompt */\n" +
                    ".gs {\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Generic.Strong */\n" +
                    ".gu {\n" +
                    "  color: #800080;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Generic.Subheading */\n" +
                    ".gt {\n" +
                    "  color: #0040D0; }\n" +
                    "\n" +
                    "/* Generic.Traceback */\n" +
                    ".kc {\n" +
                    "  color: #008000;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Keyword.Constant */\n" +
                    ".kd {\n" +
                    "  color: #229EFF;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Keyword.Declaration */\n" +
                    ".kn {\n" +
                    "  color: #008000;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Keyword.Namespace */\n" +
                    ".kp {\n" +
                    "  color: #008000; }\n" +
                    "\n" +
                    "/* Keyword.Pseudo */\n" +
                    ".kr {\n" +
                    "  color: #008000;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Keyword.Reserved */\n" +
                    ".kt {\n" +
                    "  color: #B00040; }\n" +
                    "\n" +
                    "/* Keyword.Type */\n" +
                    ".m {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number */\n" +
                    ".s {\n" +
                    "  color: #CB0C6A; }\n" +
                    "\n" +
                    "/* Literal.String */\n" +
                    ".na {\n" +
                    "  color: #C38D00; }\n" +
                    "\n" +
                    "/* Name.Attribute */\n" +
                    ".nb {\n" +
                    "  color: #008000; }\n" +
                    "\n" +
                    "/* Name.Builtin */\n" +
                    ".nc {\n" +
                    "  color: #0000FF;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Name.Class */\n" +
                    ".no {\n" +
                    "  color: #880000; }\n" +
                    "\n" +
                    "/* Name.Constant */\n" +
                    ".nd {\n" +
                    "  color: #AA22FF; }\n" +
                    "\n" +
                    "/* Name.Decorator */\n" +
                    ".ni {\n" +
                    "  color: #999999;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Name.Entity */\n" +
                    ".ne {\n" +
                    "  color: #D2413A;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Name.Exception */\n" +
                    ".nf {\n" +
                    "  color: #0000FF; }\n" +
                    "\n" +
                    "/* Name.Function */\n" +
                    ".nl {\n" +
                    "  color: #A0A000; }\n" +
                    "\n" +
                    "/* Name.Label */\n" +
                    ".nn {\n" +
                    "  color: #0000FF;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Name.Namespace */\n" +
                    ".nt {\n" +
                    "  color: #0081E5;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Name.Tag */\n" +
                    ".nv {\n" +
                    "  color: #19177C; }\n" +
                    "\n" +
                    "/* Name.Variable */\n" +
                    ".ow {\n" +
                    "  color: #AA22FF;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Operator.Word */\n" +
                    ".w {\n" +
                    "  color: #bbbbbb; }\n" +
                    "\n" +
                    "/* Text.Whitespace */\n" +
                    ".mf {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number.Float */\n" +
                    ".mh {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number.Hex */\n" +
                    ".mi {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number.Integer */\n" +
                    ".mo {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number.Oct */\n" +
                    ".sb {\n" +
                    "  color: #BA2121; }\n" +
                    "\n" +
                    "/* Literal.String.Backtick */\n" +
                    ".sc {\n" +
                    "  color: #BA2121; }\n" +
                    "\n" +
                    "/* Literal.String.Char */\n" +
                    ".sd {\n" +
                    "  color: #BA2121;\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "/* Literal.String.Doc */\n" +
                    ".s2 {\n" +
                    "  color: #D50069; }\n" +
                    "\n" +
                    "/* Literal.String.Double */\n" +
                    ".se {\n" +
                    "  color: #BB6622;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Literal.String.Escape */\n" +
                    ".sh {\n" +
                    "  color: #BA2121; }\n" +
                    "\n" +
                    "/* Literal.String.Heredoc */\n" +
                    ".si {\n" +
                    "  color: #BB6688;\n" +
                    "  font-weight: bold; }\n" +
                    "\n" +
                    "/* Literal.String.Interpol */\n" +
                    ".sx {\n" +
                    "  color: #008000; }\n" +
                    "\n" +
                    "/* Literal.String.Other */\n" +
                    ".sr {\n" +
                    "  color: #BB6688; }\n" +
                    "\n" +
                    "/* Literal.String.Regex */\n" +
                    ".s1 {\n" +
                    "  color: #BA2121; }\n" +
                    "\n" +
                    "/* Literal.String.Single */\n" +
                    ".ss {\n" +
                    "  color: #19177C; }\n" +
                    "\n" +
                    "/* Literal.String.Symbol */\n" +
                    ".bp {\n" +
                    "  color: #008000; }\n" +
                    "\n" +
                    "/* Name.Builtin.Pseudo */\n" +
                    ".vc {\n" +
                    "  color: #19177C; }\n" +
                    "\n" +
                    "/* Name.Variable.Class */\n" +
                    ".vg {\n" +
                    "  color: #19177C; }\n" +
                    "\n" +
                    "/* Name.Variable.Global */\n" +
                    ".vi {\n" +
                    "  color: #19177C; }\n" +
                    "\n" +
                    "/* Name.Variable.Instance */\n" +
                    ".il {\n" +
                    "  color: #666666; }\n" +
                    "\n" +
                    "/* Literal.Number.Integer.Long */\n" +
                    ".nx {\n" +
                    "  color: #4C556B; }\n" +
                    "\n" +
                    "#dropzone {\n" +
                    "  margin-bottom: 3rem; }\n" +
                    "\n" +
                    ".dropzone {\n" +
                    "  border: 2px dashed #0087F7;\n" +
                    "  border-radius: 5px;\n" +
                    "  background: white; }\n" +
                    "  .dropzone .dz-message {\n" +
                    "    font-weight: 400; }\n" +
                    "    .dropzone .dz-message .note {\n" +
                    "      font-size: 0.8em;\n" +
                    "      font-weight: 200;\n" +
                    "      display: block;\n" +
                    "      margin-top: 1.4rem; }\n" +
                    "\n" +
                    "*, *:before, *:after {\n" +
                    "  box-sizing: border-box; }\n" +
                    "\n" +
                    "html, body {\n" +
                    "  height: 100%;\n" +
                    "  font-family: Roboto, \"Open Sans\", sans-serif;\n" +
                    "  font-size: 20px;\n" +
                    "  font-weight: 300;\n" +
                    "  line-height: 1.4rem;\n" +
                    "  background: #F3F4F5;\n" +
                    "  color: #646C7F;\n" +
                    "  text-rendering: optimizeLegibility; }\n" +
                    "  @media (max-width: 600px) {\n" +
                    "    html, body {\n" +
                    "      font-size: 18px; } }\n" +
                    "  @media (max-width: 400px) {\n" +
                    "    html, body {\n" +
                    "      font-size: 16px; } }\n" +
                    "\n" +
                    "h1, h2, h3, table th, table th .header {\n" +
                    "  font-size: 1.8rem;\n" +
                    "  color: #0087F7;\n" +
                    "  -webkit-font-smoothing: antialiased;\n" +
                    "  line-height: 2.2rem; }\n" +
                    "\n" +
                    "h1, h2, h3 {\n" +
                    "  margin-top: 2.8rem;\n" +
                    "  margin-bottom: 1.4rem; }\n" +
                    "\n" +
                    "h2 {\n" +
                    "  font-size: 1.4rem; }\n" +
                    "\n" +
                    "h1.anchor, h2.anchor {\n" +
                    "  margin: 0;\n" +
                    "  padding: 0;\n" +
                    "  height: 1px;\n" +
                    "  overflow: hidden;\n" +
                    "  visibility: hidden; }\n" +
                    "\n" +
                    "table th {\n" +
                    "  font-size: 1.4rem;\n" +
                    "  color: #646C7F; }\n" +
                    "\n" +
                    "ul, ol {\n" +
                    "  list-style-position: inside; }\n" +
                    "\n" +
                    "a {\n" +
                    "  color: #0087F7;\n" +
                    "  text-decoration: none; }\n" +
                    "  a:hover {\n" +
                    "    border-bottom: 2px solid #0087F7; }\n" +
                    "\n" +
                    "p {\n" +
                    "  margin: 1.4rem 0; }\n" +
                    "\n" +
                    "strong {\n" +
                    "  font-weight: 400; }\n" +
                    "\n" +
                    "em {\n" +
                    "  font-style: italic; }\n" +
                    "\n" +
                    "code {\n" +
                    "  font-family: Inconsolata, monospace;\n" +
                    "  background: rgba(0, 135, 247, 0.04);\n" +
                    "  padding: 0.2em 0.4em; }\n" +
                    "\n" +
                    ".highlight code, td:first-child code {\n" +
                    "  background: none;\n" +
                    "  padding: 0; }\n" +
                    "\n" +
                    "aside {\n" +
                    "  font-size: 0.8em;\n" +
                    "  color: rgba(0, 0, 0, 0.4); }\n" +
                    "\n" +
                    "hr {\n" +
                    "  border: none;\n" +
                    "  background: none;\n" +
                    "  position: relative;\n" +
                    "  height: 2.8rem; }\n" +
                    "  hr:after {\n" +
                    "    content: \"\";\n" +
                    "    position: absolute;\n" +
                    "    top: 1.4rem;\n" +
                    "    left: 0;\n" +
                    "    right: 0;\n" +
                    "    height: 1px;\n" +
                    "    background: rgba(0, 0, 0, 0.1); }\n" +
                    "\n" +
                    "ul li {\n" +
                    "  list-style-type: disc;\n" +
                    "  padding-top: 0.7rem;\n" +
                    "  padding-bottom: 0.7rem;\n" +
                    "  border-bottom: 1px solid rgba(0, 0, 0, 0.1); }\n" +
                    "  ul li:last-of-type {\n" +
                    "    border: none; }\n" +
                    "\n" +
                    ".highlight {\n" +
                    "  padding: 1.4rem;\n" +
                    "  overflow: auto;\n" +
                    "  background: rgba(100, 108, 128, 0.04);\n" +
                    "  margin-top: 2.8rem;\n" +
                    "  margin-bottom: 2.8rem; }\n" +
                    "\n" +
                    ".bitcoin {\n" +
                    "  overflow: auto; }\n" +
                    "\n" +
                    "blockquote {\n" +
                    "  color: #0087F7;\n" +
                    "  font-size: 1.2rem;\n" +
                    "  line-height: 2rem;\n" +
                    "  -webkit-font-smoothing: antialiased;\n" +
                    "  margin-top: 2.8rem;\n" +
                    "  margin-bottom: 2.8rem; }\n" +
                    "  blockquote a {\n" +
                    "    border-bottom: 1px solid #0087F7; }\n" +
                    "\n" +
                    "body > header {\n" +
                    "  position: relative;\n" +
                    "  padding: 2.8rem 1.4rem;\n" +
                    "  z-index: 10; }\n" +
                    "  body > header .content {\n" +
                    "    opacity: 1;\n" +
                    "    background: #F3F4F5;\n" +
                    "    z-index: 10; }\n" +
                    "    body > header .content > * {\n" +
                    "      max-width: 700px; }\n" +
                    "    body > header .content h1 {\n" +
                    "      margin-bottom: 2.8rem;\n" +
                    "      margin-top: 0; }\n" +
                    "      body > header .content h1 img {\n" +
                    "        max-width: 100%; }\n" +
                    "      body > header .content h1 span {\n" +
                    "        display: none; }\n" +
                    "  @media (min-width: 700px) {\n" +
                    "    body > header #social-buttons {\n" +
                    "      display: inline-block;\n" +
                    "      position: absolute;\n" +
                    "      top: 0.5em;\n" +
                    "      right: 0;\n" +
                    "      opacity: 0.5;\n" +
                    "      -webkit-transition: opacity 0.2s ease;\n" +
                    "      -moz-transition: opacity 0.2s ease;\n" +
                    "      -ms-transition: opacity 0.2s ease;\n" +
                    "      -o-transition: opacity 0.2s ease;\n" +
                    "      transition: opacity 0.2s ease; }\n" +
                    "      body > header #social-buttons:hover {\n" +
                    "        opacity: 1; } }\n" +
                    "  body > header #social-buttons .social-button {\n" +
                    "    display: inline-block; }\n" +
                    "    body > header #social-buttons .social-button.facebook-social-button .fb-like > span {\n" +
                    "      vertical-align: top !important;\n" +
                    "      top: 1px; }\n" +
                    "  body > header .scroll-invitation {\n" +
                    "    margin-top: 2.8rem;\n" +
                    "    margin-bottom: 2.8rem; }\n" +
                    "    body > header .scroll-invitation a {\n" +
                    "      display: block;\n" +
                    "      width: 56px;\n" +
                    "      height: 56px;\n" +
                    "      background: url(\"../images/arrow.svg\") no-repeat; }\n" +
                    "      body > header .scroll-invitation a:hover {\n" +
                    "        text-decoration: none;\n" +
                    "        border: none;\n" +
                    "        background-image: url(\"../images/arrow-hover.svg\"); }\n" +
                    "      body > header .scroll-invitation a span {\n" +
                    "        display: none; }\n" +
                    "  @media (min-width: 700px) {\n" +
                    "    body > header {\n" +
                    "      height: 100vh;\n" +
                    "      margin-bottom: 0; }\n" +
                    "      body > header .content {\n" +
                    "        position: relative;\n" +
                    "        top: 50%;\n" +
                    "        transform: translateY(-50%);\n" +
                    "        -webkit-transform: translateY(-50%);\n" +
                    "        -moz-transform: translateY(-50%); } }\n" +
                    "  @media (min-width: 900px) {\n" +
                    "    body > header {\n" +
                    "      padding-left: 15%; }\n" +
                    "      body > header .content h1 {\n" +
                    "        margin-bottom: 4.2rem; }\n" +
                    "        body > header .content h1 img {\n" +
                    "          width: 550px; }\n" +
                    "      body > header .content h2 {\n" +
                    "        font-size: 1.5em;\n" +
                    "        line-height: 1.4em; } }\n" +
                    "  @media (min-width: 1100px) {\n" +
                    "    body > header {\n" +
                    "      font-size: 1em;\n" +
                    "      line-height: 1.5em; }\n" +
                    "      body > header .content h1 {\n" +
                    "        margin-bottom: 5.6rem; }\n" +
                    "        body > header .content h1 img {\n" +
                    "          width: 700px; }\n" +
                    "      body > header .content > * {\n" +
                    "        max-width: 900px; }\n" +
                    "      body > header h2 {\n" +
                    "        margin-top: 2.8rem;\n" +
                    "        margin-bottom: 2.8rem; }\n" +
                    "      body > header .scroll-invitation {\n" +
                    "        margin-top: 5.6rem; } }\n" +
                    "\n" +
                    "main > nav {\n" +
                    "  position: absolute;\n" +
                    "  top: 0;\n" +
                    "  left: 0;\n" +
                    "  bottom: 0;\n" +
                    "  width: 220px;\n" +
                    "  background: #028AF4;\n" +
                    "  padding: 1.4rem 0;\n" +
                    "  z-index: 200;\n" +
                    "  overflow: auto;\n" +
                    "  display: none; }\n" +
                    "  main > nav.fixed {\n" +
                    "    position: fixed; }\n" +
                    "  main > nav img {\n" +
                    "    margin: 0 0 1.4rem 1.4rem;\n" +
                    "    width: 58px;\n" +
                    "    height: 58px; }\n" +
                    "  main > nav a:not(.logo) {\n" +
                    "    display: block;\n" +
                    "    line-height: 1.4rem;\n" +
                    "    color: rgba(255, 255, 255, 0.9);\n" +
                    "    border: none;\n" +
                    "    padding: 0.7rem 1.4rem;\n" +
                    "    font-size: 0.8rem;\n" +
                    "    -webkit-font-smoothing: subpixel-antialiased; }\n" +
                    "    main > nav a:not(.logo):hover {\n" +
                    "      background: rgba(255, 255, 255, 0.3); }\n" +
                    "  main > nav .sub-sections {\n" +
                    "    height: 0;\n" +
                    "    overflow: hidden;\n" +
                    "    -webkit-transition: height 0.4s ease;\n" +
                    "    -moz-transition: height 0.4s ease;\n" +
                    "    -ms-transition: height 0.4s ease;\n" +
                    "    -o-transition: height 0.4s ease;\n" +
                    "    transition: height 0.4s ease; }\n" +
                    "  main > nav .visible {\n" +
                    "    background: rgba(255, 255, 255, 0.13); }\n" +
                    "    main > nav .visible .sub-sections {\n" +
                    "      display: block; }\n" +
                    "  main > nav a.current {\n" +
                    "    background: #4DADF7; }\n" +
                    "  main > nav .level-0 > a {\n" +
                    "    font-weight: 400; }\n" +
                    "  main > nav .level-1 > a {\n" +
                    "    padding-left: 1.9rem;\n" +
                    "    color: rgba(255, 255, 255, 0.7); }\n" +
                    "\n" +
                    "@media (min-width: 940px) {\n" +
                    "  main {\n" +
                    "    padding-left: 220px; }\n" +
                    "    main > nav {\n" +
                    "      display: block; } }\n" +
                    "form.donate {\n" +
                    "  display: inline-block;\n" +
                    "  vertical-align: bottom;\n" +
                    "  position: relative;\n" +
                    "  top: 0.25em;\n" +
                    "  margin: 0 0em 0 0.2em; }\n" +
                    "\n" +
                    "main {\n" +
                    "  position: relative;\n" +
                    "  z-index: 100; }\n" +
                    "  main section {\n" +
                    "    padding: 1.4rem 1.4rem 2.8rem 1.4rem; }\n" +
                    "    main section:last-of-type {\n" +
                    "      padding-bottom: 8.4rem; }\n" +
                    "    main section h1, main section h2 {\n" +
                    "      margin-top: 0;\n" +
                    "      padding-top: 2.8rem; }\n" +
                    "    main section > * {\n" +
                    "      max-width: 720px;\n" +
                    "      margin-left: auto;\n" +
                    "      margin-right: auto; }\n" +
                    "      main section > *.highlight {\n" +
                    "        max-width: 900px; }\n" +
                    "    main section > table {\n" +
                    "      max-width: 80rem; }\n" +
                    "    main section .embedded-video {\n" +
                    "      position: relative;\n" +
                    "      width: 100%; }\n" +
                    "      main section .embedded-video:after {\n" +
                    "        content: '';\n" +
                    "        padding-top: 56.25%;\n" +
                    "        display: block; }\n" +
                    "      main section .embedded-video iframe {\n" +
                    "        display: block;\n" +
                    "        position: absolute;\n" +
                    "        width: 100%;\n" +
                    "        height: 100%; }\n" +
                    "    main section:nth-child(odd) {\n" +
                    "      background: #F3F4F5; }\n" +
                    "    main section:nth-child(even) {\n" +
                    "      background: #E8E9EC; }\n" +
                    "    main section.news {\n" +
                    "      background: #646C7F;\n" +
                    "      color: white; }\n" +
                    "      main section.news h1, main section.news h2 {\n" +
                    "        color: white;\n" +
                    "        -webkit-font-smoothing: subpixel-antialiased; }\n" +
                    "      main section.news a {\n" +
                    "        color: #C0E3FE;\n" +
                    "        border-color: #C0E3FE; }\n" +
                    "  main .configuration-table-container {\n" +
                    "    max-width: 100%;\n" +
                    "    overflow-x: scroll; }\n" +
                    "  main table {\n" +
                    "    font-size: 0.9rem;\n" +
                    "    margin-top: 1.4rem;\n" +
                    "    margin-bottom: 4.2rem;\n" +
                    "    border: 1px solid #38A0FE;\n" +
                    "    border-bottom: none;\n" +
                    "    background: white; }\n" +
                    "    main table th:first-of-type,\n" +
                    "    main table td:first-of-type {\n" +
                    "      text-align: right; }\n" +
                    "    main table th, main table td {\n" +
                    "      text-align: left;\n" +
                    "      border-bottom: 1px solid #38A0FE;\n" +
                    "      padding: 0.7rem 1.4rem; }\n" +
                    "    main table td:first-of-type, main table th:first-of-type {\n" +
                    "      border-right: 1px solid #38A0FE; }\n" +
                    "    main table a.default-value {\n" +
                    "      display: block;\n" +
                    "      font-weight: normal;\n" +
                    "      color: rgba(0, 88, 160, 0.3);\n" +
                    "      font-size: 0.9em; }\n" +
                    "      main table a.default-value:hover {\n" +
                    "        border: none;\n" +
                    "        color: #0087F7; }\n" +
                    "    main table td:first-of-type {\n" +
                    "      font-weight: bold;\n" +
                    "      color: #0087F7; }\n" +
                    "    main table th.title {\n" +
                    "      text-align: center;\n" +
                    "      padding-top: 2.8rem;\n" +
                    "      padding-bottom: 2.8rem; }\n" +
                    "      main table th.title p {\n" +
                    "        margin-bottom: 0; }\n" +
                    "    main table td.separator {\n" +
                    "      font-weight: normal;\n" +
                    "      text-align: left;\n" +
                    "      color: #646C7F; }\n" +
                    "    main table p {\n" +
                    "      margin: 0; }\n" +
                    "    @media (max-width: 600px) {\n" +
                    "      main table table, main table tbody, main table thead, main table tr, main table td, main table th {\n" +
                    "        display: block; }\n" +
                    "      main table a.default-value {\n" +
                    "        display: inline;\n" +
                    "        margin-left: 0.5em; }\n" +
                    "      main table td, main table th {\n" +
                    "        overflow: auto; }\n" +
                    "        main table td:first-of-type, main table th:first-of-type {\n" +
                    "          text-align: left;\n" +
                    "          border-right: none; }\n" +
                    "      main table td.label {\n" +
                    "        border-bottom-color: rgba(0, 135, 247, 0.15); }\n" +
                    "      main table th.title {\n" +
                    "        padding-top: 1.4rem;\n" +
                    "        padding-bottom: 1.4rem; }\n" +
                    "      main table th:not(.title) {\n" +
                    "        display: none; } }\n" +
                    "\n" +
                    "footer {\n" +
                    "  background: #2D3038;\n" +
                    "  z-index: 5000;\n" +
                    "  position: relative;\n" +
                    "  display: block;\n" +
                    "  padding: 1.4rem 1.4rem 2.8rem 1.4rem;\n" +
                    "  font-size: 0.9rem;\n" +
                    "  color: white; }\n" +
                    "  footer * {\n" +
                    "    color: white; }\n" +
                    "  footer a:hover {\n" +
                    "    border-color: white; }\n" +
                    "  footer > * {\n" +
                    "    max-width: 720px;\n" +
                    "    margin-left: auto;\n" +
                    "    margin-right: auto; }\n" +
                    "  @media (min-width: 720px) {\n" +
                    "    footer .license {\n" +
                    "      text-align: justify; } }\n" +
                    "  footer .logo {\n" +
                    "    margin: 2.8rem 0;\n" +
                    "    width: 270px; }\n" +
                    "\n" +
                    ".for-hire {\n" +
                    "  text-align: center;\n" +
                    "  padding: 1em 2em;\n" +
                    "  background: rgba(255, 255, 255, 0.1);\n" +
                    "  border-radius: 0.3rem;\n" +
                    "  line-height: 1.5em; }\n" +
                    "  .for-hire h1 {\n" +
                    "    padding: 0;\n" +
                    "    margin: 1.5rem 0 3rem; }\n" +
                    "    .for-hire h1 img {\n" +
                    "      max-width: 100%;\n" +
                    "      height: auto; }\n" +
            "/*\n" +
                    " * The MIT License\n" +
                    " * Copyright (c) 2012 Matias Meno <m@tias.me>\n" +
                    " */\n" +
                    "@-webkit-keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@-moz-keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@keyframes passing-through {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30%, 70% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); }\n" +
                    "  100% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(-40px);\n" +
                    "    -moz-transform: translateY(-40px);\n" +
                    "    -ms-transform: translateY(-40px);\n" +
                    "    -o-transform: translateY(-40px);\n" +
                    "    transform: translateY(-40px); } }\n" +
                    "@-webkit-keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@-moz-keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@keyframes slide-in {\n" +
                    "  0% {\n" +
                    "    opacity: 0;\n" +
                    "    -webkit-transform: translateY(40px);\n" +
                    "    -moz-transform: translateY(40px);\n" +
                    "    -ms-transform: translateY(40px);\n" +
                    "    -o-transform: translateY(40px);\n" +
                    "    transform: translateY(40px); }\n" +
                    "  30% {\n" +
                    "    opacity: 1;\n" +
                    "    -webkit-transform: translateY(0px);\n" +
                    "    -moz-transform: translateY(0px);\n" +
                    "    -ms-transform: translateY(0px);\n" +
                    "    -o-transform: translateY(0px);\n" +
                    "    transform: translateY(0px); } }\n" +
                    "@-webkit-keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    "@-moz-keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    "@keyframes pulse {\n" +
                    "  0% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); }\n" +
                    "  10% {\n" +
                    "    -webkit-transform: scale(1.1);\n" +
                    "    -moz-transform: scale(1.1);\n" +
                    "    -ms-transform: scale(1.1);\n" +
                    "    -o-transform: scale(1.1);\n" +
                    "    transform: scale(1.1); }\n" +
                    "  20% {\n" +
                    "    -webkit-transform: scale(1);\n" +
                    "    -moz-transform: scale(1);\n" +
                    "    -ms-transform: scale(1);\n" +
                    "    -o-transform: scale(1);\n" +
                    "    transform: scale(1); } }\n" +
                    ".dropzone, .dropzone * {\n" +
                    "  box-sizing: border-box; }\n" +
                    "\n" +
                    ".dropzone {\n" +
                    "  min-height: 150px;\n" +
                    "  border: 2px solid rgba(0, 0, 0, 0.3);\n" +
                    "  background: white;\n" +
                    "  padding: 20px 20px; }\n" +
                    "  .dropzone.dz-clickable {\n" +
                    "    cursor: pointer; }\n" +
                    "    .dropzone.dz-clickable * {\n" +
                    "      cursor: default; }\n" +
                    "    .dropzone.dz-clickable .dz-message, .dropzone.dz-clickable .dz-message * {\n" +
                    "      cursor: pointer; }\n" +
                    "  .dropzone.dz-started .dz-message {\n" +
                    "    display: none; }\n" +
                    "  .dropzone.dz-drag-hover {\n" +
                    "    border-style: solid; }\n" +
                    "    .dropzone.dz-drag-hover .dz-message {\n" +
                    "      opacity: 0.5; }\n" +
                    "  .dropzone .dz-message {\n" +
                    "    text-align: center;\n" +
                    "    margin: 2em 0; }\n" +
                    "  .dropzone .dz-preview {\n" +
                    "    position: relative;\n" +
                    "    display: inline-block;\n" +
                    "    vertical-align: top;\n" +
                    "    margin: 16px;\n" +
                    "    min-height: 100px; }\n" +
                    "    .dropzone .dz-preview:hover {\n" +
                    "      z-index: 1000; }\n" +
                    "      .dropzone .dz-preview:hover .dz-details {\n" +
                    "        opacity: 1; }\n" +
                    "    .dropzone .dz-preview.dz-file-preview .dz-image {\n" +
                    "      border-radius: 20px;\n" +
                    "      background: #999;\n" +
                    "      background: linear-gradient(to bottom, #eee, #ddd); }\n" +
                    "    .dropzone .dz-preview.dz-file-preview .dz-details {\n" +
                    "      opacity: 1; }\n" +
                    "    .dropzone .dz-preview.dz-image-preview {\n" +
                    "      background: white; }\n" +
                    "      .dropzone .dz-preview.dz-image-preview .dz-details {\n" +
                    "        -webkit-transition: opacity 0.2s linear;\n" +
                    "        -moz-transition: opacity 0.2s linear;\n" +
                    "        -ms-transition: opacity 0.2s linear;\n" +
                    "        -o-transition: opacity 0.2s linear;\n" +
                    "        transition: opacity 0.2s linear; }\n" +
                    "    .dropzone .dz-preview .dz-remove {\n" +
                    "      font-size: 14px;\n" +
                    "      text-align: center;\n" +
                    "      display: block;\n" +
                    "      cursor: pointer;\n" +
                    "      border: none; }\n" +
                    "      .dropzone .dz-preview .dz-remove:hover {\n" +
                    "        text-decoration: underline; }\n" +
                    "    .dropzone .dz-preview:hover .dz-details {\n" +
                    "      opacity: 1; }\n" +
                    "    .dropzone .dz-preview .dz-details {\n" +
                    "      z-index: 20;\n" +
                    "      position: absolute;\n" +
                    "      top: 0;\n" +
                    "      left: 0;\n" +
                    "      opacity: 0;\n" +
                    "      font-size: 13px;\n" +
                    "      min-width: 100%;\n" +
                    "      max-width: 100%;\n" +
                    "      padding: 2em 1em;\n" +
                    "      text-align: center;\n" +
                    "      color: rgba(0, 0, 0, 0.9);\n" +
                    "      line-height: 150%; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-size {\n" +
                    "        margin-bottom: 1em;\n" +
                    "        font-size: 16px; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-filename {\n" +
                    "        white-space: nowrap; }\n" +
                    "        .dropzone .dz-preview .dz-details .dz-filename:hover span {\n" +
                    "          border: 1px solid rgba(200, 200, 200, 0.8);\n" +
                    "          background-color: rgba(255, 255, 255, 0.8); }\n" +
                    "        .dropzone .dz-preview .dz-details .dz-filename:not(:hover) {\n" +
                    "          overflow: hidden;\n" +
                    "          text-overflow: ellipsis; }\n" +
                    "          .dropzone .dz-preview .dz-details .dz-filename:not(:hover) span {\n" +
                    "            border: 1px solid transparent; }\n" +
                    "      .dropzone .dz-preview .dz-details .dz-filename span, .dropzone .dz-preview .dz-details .dz-size span {\n" +
                    "        background-color: rgba(255, 255, 255, 0.4);\n" +
                    "        padding: 0 0.4em;\n" +
                    "        border-radius: 3px; }\n" +
                    "    .dropzone .dz-preview:hover .dz-image img {\n" +
                    "      -webkit-transform: scale(1.05, 1.05);\n" +
                    "      -moz-transform: scale(1.05, 1.05);\n" +
                    "      -ms-transform: scale(1.05, 1.05);\n" +
                    "      -o-transform: scale(1.05, 1.05);\n" +
                    "      transform: scale(1.05, 1.05);\n" +
                    "      -webkit-filter: blur(8px);\n" +
                    "      filter: blur(8px); }\n" +
                    "    .dropzone .dz-preview .dz-image {\n" +
                    "      border-radius: 20px;\n" +
                    "      overflow: hidden;\n" +
                    "      width: 120px;\n" +
                    "      height: 120px;\n" +
                    "      position: relative;\n" +
                    "      display: block;\n" +
                    "      z-index: 10; }\n" +
                    "      .dropzone .dz-preview .dz-image img {\n" +
                    "        display: block; }\n" +
                    "    .dropzone .dz-preview.dz-success .dz-success-mark {\n" +
                    "      -webkit-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -moz-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -ms-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -o-animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      animation: passing-through 3s cubic-bezier(0.77, 0, 0.175, 1); }\n" +
                    "    .dropzone .dz-preview.dz-error .dz-error-mark {\n" +
                    "      opacity: 1;\n" +
                    "      -webkit-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -moz-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -ms-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      -o-animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1);\n" +
                    "      animation: slide-in 3s cubic-bezier(0.77, 0, 0.175, 1); }\n" +
                    "    .dropzone .dz-preview .dz-success-mark, .dropzone .dz-preview .dz-error-mark {\n" +
                    "      pointer-events: none;\n" +
                    "      opacity: 0;\n" +
                    "      z-index: 500;\n" +
                    "      position: absolute;\n" +
                    "      display: block;\n" +
                    "      top: 50%;\n" +
                    "      left: 50%;\n" +
                    "      margin-left: -27px;\n" +
                    "      margin-top: -27px; }\n" +
                    "      .dropzone .dz-preview .dz-success-mark svg, .dropzone .dz-preview .dz-error-mark svg {\n" +
                    "        display: block;\n" +
                    "        width: 54px;\n" +
                    "        height: 54px; }\n" +
                    "    .dropzone .dz-preview.dz-processing .dz-progress {\n" +
                    "      opacity: 1;\n" +
                    "      -webkit-transition: all 0.2s linear;\n" +
                    "      -moz-transition: all 0.2s linear;\n" +
                    "      -ms-transition: all 0.2s linear;\n" +
                    "      -o-transition: all 0.2s linear;\n" +
                    "      transition: all 0.2s linear; }\n" +
                    "    .dropzone .dz-preview.dz-complete .dz-progress {\n" +
                    "      opacity: 0;\n" +
                    "      -webkit-transition: opacity 0.4s ease-in;\n" +
                    "      -moz-transition: opacity 0.4s ease-in;\n" +
                    "      -ms-transition: opacity 0.4s ease-in;\n" +
                    "      -o-transition: opacity 0.4s ease-in;\n" +
                    "      transition: opacity 0.4s ease-in; }\n" +
                    "    .dropzone .dz-preview:not(.dz-processing) .dz-progress {\n" +
                    "      -webkit-animation: pulse 6s ease infinite;\n" +
                    "      -moz-animation: pulse 6s ease infinite;\n" +
                    "      -ms-animation: pulse 6s ease infinite;\n" +
                    "      -o-animation: pulse 6s ease infinite;\n" +
                    "      animation: pulse 6s ease infinite; }\n" +
                    "    .dropzone .dz-preview .dz-progress {\n" +
                    "      opacity: 1;\n" +
                    "      z-index: 1000;\n" +
                    "      pointer-events: none;\n" +
                    "      position: absolute;\n" +
                    "      height: 16px;\n" +
                    "      left: 50%;\n" +
                    "      top: 50%;\n" +
                    "      margin-top: -8px;\n" +
                    "      width: 80px;\n" +
                    "      margin-left: -40px;\n" +
                    "      background: rgba(255, 255, 255, 0.9);\n" +
                    "      -webkit-transform: scale(1);\n" +
                    "      border-radius: 8px;\n" +
                    "      overflow: hidden; }\n" +
                    "      .dropzone .dz-preview .dz-progress .dz-upload {\n" +
                    "        background: #333;\n" +
                    "        background: linear-gradient(to bottom, #666, #444);\n" +
                    "        position: absolute;\n" +
                    "        top: 0;\n" +
                    "        left: 0;\n" +
                    "        bottom: 0;\n" +
                    "        width: 0;\n" +
                    "        -webkit-transition: width 300ms ease-in-out;\n" +
                    "        -moz-transition: width 300ms ease-in-out;\n" +
                    "        -ms-transition: width 300ms ease-in-out;\n" +
                    "        -o-transition: width 300ms ease-in-out;\n" +
                    "        transition: width 300ms ease-in-out; }\n" +
                    "    .dropzone .dz-preview.dz-error .dz-error-message {\n" +
                    "      display: block; }\n" +
                    "    .dropzone .dz-preview.dz-error:hover .dz-error-message {\n" +
                    "      opacity: 1;\n" +
                    "      pointer-events: auto; }\n" +
                    "    .dropzone .dz-preview .dz-error-message {\n" +
                    "      pointer-events: none;\n" +
                    "      z-index: 1000;\n" +
                    "      position: absolute;\n" +
                    "      display: block;\n" +
                    "      display: none;\n" +
                    "      opacity: 0;\n" +
                    "      -webkit-transition: opacity 0.3s ease;\n" +
                    "      -moz-transition: opacity 0.3s ease;\n" +
                    "      -ms-transition: opacity 0.3s ease;\n" +
                    "      -o-transition: opacity 0.3s ease;\n" +
                    "      transition: opacity 0.3s ease;\n" +
                    "      border-radius: 8px;\n" +
                    "      font-size: 13px;\n" +
                    "      top: 130px;\n" +
                    "      left: -10px;\n" +
                    "      width: 140px;\n" +
                    "      background: #be2626;\n" +
                    "      background: linear-gradient(to bottom, #be2626, #a92222);\n" +
                    "      padding: 0.5em 1.2em;\n" +
                    "      color: white; }\n" +
                    "      .dropzone .dz-preview .dz-error-message:after {\n" +
                    "        content: '';\n" +
                    "        position: absolute;\n" +
                    "        top: -6px;\n" +
                    "        left: 64px;\n" +
                    "        width: 0;\n" +
                    "        height: 0;\n" +
                    "        border-left: 6px solid transparent;\n" +
                    "        border-right: 6px solid transparent;\n" +
                    "        border-bottom: 6px solid #be2626; }\n");
        }
        Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Debug Server</title></head>");
        sb.append("<body>");
        sb.append("<h1>Debug Server</h1>");

        sb.append("<p><blockquote><b>URI</b> = ").append(String.valueOf(session.getUri())).append("<br />");

        sb.append("<b>Method</b> = ").append(String.valueOf(session.getMethod())).append("</blockquote></p>");

        sb.append("<h3>Headers</h3><p><blockquote>").append(toString(session.getHeaders())).append("</blockquote></p>");

        sb.append("<h3>Parms</h3><p><blockquote>").append(toString(session.getParms())).append("</blockquote></p>");

        sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").append(toString(decodedQueryParameters)).append("</blockquote></p>");

        try {
            Map<String, String> files = new HashMap<String, String>();
            session.parseBody(files);
            Set<String> fileSet = files.keySet();
            for ( String file : fileSet )
            {
                System.out.println("File " + files.get(file) + ": " + new File(files.get(file)).length());
                Path src = Paths.get(files.get(file));
                Files.copy(src, Paths.get("/home/turok/Downloads", src.getFileName().toString()+"1"));
            }
            sb.append("<h3>Files</h3><p><blockquote>").append(toString(files)).append("</blockquote></p>");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sb.append("</body>");
        sb.append("</html>");
        return Response.newFixedLengthResponse(sb.toString());
    }

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
            listItem(sb, entry);
        }
        sb.append("</ul>");
        return sb.toString();
    }
}

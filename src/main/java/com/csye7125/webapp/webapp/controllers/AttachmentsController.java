package com.csye7125.webapp.webapp.controllers;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttachmentsController {

//    @PostMapping(path = "/v1/attachments", produces = "application/json")
//    public ResponseEntity<String> uploadAttachments(@RequestHeader HttpHeaders headers, HttpServletRequest request) {
//        String authorization = headers.getFirst("Authorization");
//
//        String contentType = request.getContentType();
//        byte[] pictureBA = new byte[0];
//
//            InputStream pictureIS = null;
//            try {
//                pictureIS = request.getInputStream();
//                pictureBA = IOUtils.toByteArray(pictureIS);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new JSONObject().put("message","Image file is corrupted").toString());
//            }
//        String name = "file.txt";
//        String originalFileName = "file.txt";
//        String userHeader = request.getHeader("Authorization");
//        MultipartFile file = new MockMultipartFile(
//                name,
//                originalFileName,
//                contentType,
//                pictureBA
//        );
//
//        Image i = null;
//        JSONObject resp = null;
//        User u = userService.getUser(tokens[0]);
//        try{
//            System.out.println(u.getId());
//            i = imageService.getImageMetaData(u.getId());
//            imageStoreService.deleteFile(i.getFile_name());
//            resp = imageStoreService.uploadFile(file, tokens[0]);
//            i.setFile_name(resp.getString("filename"));
//            i.setUrl(resp.getString("url"));
//            imageService.saveImageMetaData(i);
//            return ResponseEntity.status(HttpStatus.CREATED).body(commonUtilsService.getImageAsJSON(i).toString());
//        } catch(NotFoundException e){
//            resp = imageStoreService.uploadFile(file, tokens[0]);
//            i = new Image();
//            i.setId(UUID.randomUUID());
//            i.setFile_name(resp.getString("filename"));
//            i.setUrl(resp.getString("url"));
//            i.setUser_id(u.getId());
//            i.setUpload_date(new Date());
//            imageService.saveImageMetaData(i);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(commonUtilsService.getImageAsJSON(i).toString());
//        }
//    }
}

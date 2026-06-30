package com.payment.controller;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payment.service.EncryptionService;

@RestController
@RequestMapping("/sec")
public class EncryptAndDecryptController {

	@Autowired
	private EncryptionService ser;
	
	@PostMapping("/encode")
	public ResponseEntity<String> enrypt(@RequestBody String data){
		return ResponseEntity.ok(ser.encrypt(data));
	}
	
	@PostMapping("/decode")
	public ResponseEntity<String> decrypt(@RequestBody HashMap<String, String> e){
		return ResponseEntity.ok(ser.decrypt(e.get("encrypt")));
	}

}

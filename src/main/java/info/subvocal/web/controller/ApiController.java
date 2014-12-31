package info.subvocal.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *  Public API REST endpoints
 */
@RestController
@RequestMapping(value = "/api/v1.0", produces = "application/json")
public class ApiController {

    @ResponseBody
    @RequestMapping(value = "_ping", method = GET)
    public ResponseEntity<Boolean> ping() {
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
}

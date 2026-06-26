package com.example.musicbackend;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final Table loginTable;

    public AuthController(DynamoDB dynamoDB) {
        this.loginTable = dynamoDB.getTable("login");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = clean(body.get("email"));
        String password = clean(body.get("password"));

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.message("email or password is invalid"));
        }

        Item user = loginTable.getItem("email", email);

        if (user == null || !password.equals(user.getString("password"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.message("email or password is invalid"));
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getString("email"));
        userData.put("user_name", user.getString("user_name"));

        Map<String, Object> response = new HashMap<>();
        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = clean(body.get("email"));
        String userName = clean(body.get("user_name"));
        String password = clean(body.get("password"));

        if (email.isEmpty() || userName.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.message("Please complete all fields"));
        }

        Item newUser = new Item()
                .withPrimaryKey("email", email)
                .withString("user_name", userName)
                .withString("password", password);

        try {
            loginTable.putItem(new PutItemSpec()
                    .withItem(newUser)
                    .withConditionExpression("attribute_not_exists(email)"));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.message("Registration successful"));

        } catch (ConditionalCheckFailedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.message("The email already exists"));
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

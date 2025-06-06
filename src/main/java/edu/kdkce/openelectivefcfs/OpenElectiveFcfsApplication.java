package edu.kdkce.openelectivefcfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class OpenElectiveFcfsApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(OpenElectiveFcfsApplication.class, args);
    }

}
//TODO: Check save methods in repositories for ID value generation as its not being generated automatically.
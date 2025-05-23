package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.model.StudentPrincipal;
import edu.kdkce.openelectivefcfs.src.model.User;
import edu.kdkce.openelectivefcfs.src.model.UserPrincipal;
import edu.kdkce.openelectivefcfs.src.repository.StudentRepository;
import edu.kdkce.openelectivefcfs.src.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    public UserServiceImplementation(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String processedEmail = email.trim().toLowerCase();
        // Optional logging
        System.out.println("Attempting login with: " + processedEmail);

        User user = userRepository.findByEmail(processedEmail).orElse(null);
        if(user != null) {
            System.out.println("Authenticated as User");
            return new UserPrincipal(user);
        }

        Student student = studentRepository.findByEmail(processedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("Authenticated as Student");
        return new StudentPrincipal(student);
    }
}

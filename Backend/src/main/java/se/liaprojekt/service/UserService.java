package se.liaprojekt.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import se.liaprojekt.dto.GraphResponse;
import se.liaprojekt.dto.UserResponse;
import se.liaprojekt.model.User;
import se.liaprojekt.repository.UserRepository;

import java.util.*;

@Service
@AllArgsConstructor
public class UserService {
    private final GraphService graphService;
    UserRepository userRepository;


    public List<UserResponse> getAllUsers() {
        List<GraphResponse> graphResponseList = graphService.getUsers();
        updateFromGraphAPI(graphResponseList);

        List<UserResponse> userResponseList = new ArrayList<>();
        for (GraphResponse graphResponse : graphResponseList) {
            Optional<User> optionalUser = userRepository.findByEntraId(graphResponse.id());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                UserResponse userResponse = new UserResponse(
                        user.getId(),
                        graphResponse.displayName(),
                        graphResponse.givenName(),
                        graphResponse.surname(),
                        graphResponse.mail()
                );
                userResponseList.add(userResponse);
            }
        }
        return userResponseList;
    }


    private void updateFromGraphAPI(List<GraphResponse> graphResponseList) {
        //Get all users in database end put their unique entraId in a set
        List<User> usersInDatabaseList = userRepository.findAll();
        Set<String> usersInDataBaseSet = new HashSet<>();
        for (User user : usersInDatabaseList) {
            usersInDataBaseSet.add(user.getEntraId());
        }

        List<User> usersToSave = new ArrayList<>();
        for (GraphResponse graphResponse : graphResponseList) {
            //Add only if database doesn't already contain this entraId
            if (!usersInDataBaseSet.contains(graphResponse.id())) {
                usersToSave.add(graphResponseToUser(graphResponse));
            }
        }

        userRepository.saveAll(usersToSave);

    }

    private User graphResponseToUser(GraphResponse graphResponse) {
        return User.builder()
                .entraId(graphResponse.id())
                .build();
    }
}

package com.example.quizcards.repository;

import com.example.quizcards.entities.PushNotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPushNotificationSubscriptionRepository extends JpaRepository<PushNotificationSubscription, Long> {

//    @Query(value = """
//                  SELECT exists (
//                      SELECT 1
//                      FROM PushNotificationSubscription pns
//                     WHERE pns.user.userId = :user_id
//                       AND pns.endpoint = :endpoint
//                       AND pns.keys = :key
//                  ) as result
//            """)
//    boolean existsByUserIdWithEndpointAndKey(@Param("user_id") Long userId,
//                                             @Param("endpoint") String endpoint,
//                                             @Param("key") String key);

    boolean existsByUser_UserIdAndEndpointAndKeys(Long userId, String endpoint, String keys);


    List<PushNotificationSubscription> findByUser_UserId(Long userId);
}

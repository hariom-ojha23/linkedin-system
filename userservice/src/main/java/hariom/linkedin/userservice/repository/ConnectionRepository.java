package hariom.linkedin.userservice.repository;

import hariom.linkedin.userservice.entity.Connection;
import hariom.linkedin.userservice.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<Connection> findBySenderIdAndStatus(Long senderId, ConnectionStatus status);

}

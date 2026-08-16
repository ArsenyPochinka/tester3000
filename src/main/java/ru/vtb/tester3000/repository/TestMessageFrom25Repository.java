package ru.vtb.tester3000.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vtb.tester3000.entity.TestMessageFrom25Entity;

import java.util.Collection;
import java.util.List;

public interface TestMessageFrom25Repository extends JpaRepository<TestMessageFrom25Entity, String> {

    List<TestMessageFrom25Entity> findAllByOrderByTestCodeAsc();

    List<TestMessageFrom25Entity> findByTestCodeIn(Collection<String> testCodes);
}
